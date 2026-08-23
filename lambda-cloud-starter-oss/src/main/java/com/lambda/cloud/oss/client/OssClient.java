package com.lambda.cloud.oss.client;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.lambda.autoconfig.OssProperties;
import com.lambda.cloud.oss.enums.AccessPolicyType;
import com.lambda.cloud.oss.enums.OssType;
import com.lambda.cloud.oss.enums.PolicyType;
import com.lambda.cloud.oss.exception.OssException;
import com.lambda.cloud.oss.model.OssObject;
import com.lambda.cloud.oss.model.PartTag;
import com.lambda.cloud.oss.model.UploadObjectResult;
import com.lambda.cloud.oss.policy.MinIOPolicyBuilder;
import com.lambda.cloud.oss.service.OssService;
import com.lambda.cloud.oss.upload.MultipartUploadStateManager;
import com.lambda.cloud.oss.util.ValidationUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.BucketCannedACL;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/**
 * OSS 客户端实现
 * 提供对象存储的核心操作功能
 *
 * <p>该类实现了 {@link OssService} 接口，基于 AWS SDK for Java v2（S3）提供统一的对象存储操作。
 * 支持多种 OSS 类型：MinIO、阿里云 OSS、腾讯云 COS、七牛云等。
 *
 * <p>主要功能：
 * <ul>
 *   <li>文件上传（支持字节数组、输入流、文件对象）</li>
 *   <li>分片上传（用于大文件，支持 Redis 和内存两种状态管理）</li>
 *   <li>文件下载（支持获取对象、输出到流）</li>
 *   <li>文件删除</li>
 *   <li>预签名 URL 生成</li>
 *   <li>存储桶管理（创建、设置策略）</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 通过 OssClientManager 获取客户端
 * OssClient client = ossClientManager.get("default");
 *
 * // 上传文件
 * UploadObjectResult result = client.upload(inputStream, "path/to/file.txt", "text/plain");
 *
 * // 下载文件
 * try (FileOutputStream fos = new FileOutputStream("local.txt")) {
 *     client.outStream("path/to/file.txt", fos);
 * }
 *
 * // 生成预签名 URL（有效期 1 小时）
 * String url = client.getPrivateUrl("path/to/file.txt", 3600);
 * }</pre>
 *
 * @author jpjoo
 * @see OssService
 * @see com.lambda.cloud.oss.manager.OssClientManager
 * @since 2025.1.1
 */
@Slf4j
@SuppressFBWarnings(value = {"EI_EXPOSE_REP2"})
public class OssClient implements OssService {

    /**
     * 默认区域（MinIO 等 S3 兼容服务未配置 region 时使用，签名必需）
     */
    private static final String DEFAULT_REGION = "us-east-1";

    private final OssProperties.Config config;

    private final S3Client client;

    private final S3Presigner presigner;

    @Setter
    private MultipartUploadStateManager multipartUploadStateManager;

    public OssClient(OssProperties.Config config) {
        this.config = config;
        this.client = buildS3Client();
        this.presigner = buildPresigner();
    }

    private S3Client buildS3Client() {
        OssProperties.Config.ClientConfig httpClientConfig = config.getHttpClientConfig();

        ApacheHttpClient.Builder httpClient = ApacheHttpClient.builder()
                .maxConnections(httpClientConfig.getMaxConnections())
                .connectionTimeout(Duration.ofMillis(httpClientConfig.getConnectionTimeout()))
                .socketTimeout(Duration.ofMillis(httpClientConfig.getSocketTimeout()))
                .connectionMaxIdleTime(Duration.ofMillis(httpClientConfig.getConnectionMaxIdleMillis()));
        // connectionTTL <= 0 表示不限制，与 SDK 1.x 语义一致
        if (httpClientConfig.getConnectionTTL() > 0) {
            httpClient.connectionTimeToLive(Duration.ofMillis(httpClientConfig.getConnectionTTL()));
        }

        ClientOverrideConfiguration.Builder overrideConfig = ClientOverrideConfiguration.builder();
        // requestTimeout 映射为单次尝试超时，clientExecutionTimeout 映射为整次调用超时（0 表示无限制）
        if (httpClientConfig.getRequestTimeout() > 0) {
            overrideConfig.apiCallAttemptTimeout(Duration.ofMillis(httpClientConfig.getRequestTimeout()));
        }
        if (httpClientConfig.getClientExecutionTimeout() > 0) {
            overrideConfig.apiCallTimeout(Duration.ofMillis(httpClientConfig.getClientExecutionTimeout()));
        }

        if (config.getIsHttps() && !StrUtil.startWith(config.getEndpoint(), "https")) {
            log.error("Endpoint 配置不正确，https 已开启！");
        }

        // forcePathStyle 保持与 SDK 1.x 的 path-style 访问行为一致（MinIO 必需）
        return S3Client.builder()
                .region(Region.of(resolveRegion()))
                .endpointOverride(URI.create(config.getEndpoint()))
                .credentialsProvider(buildCredentialsProvider())
                .httpClientBuilder(httpClient)
                .overrideConfiguration(overrideConfig.build())
                .forcePathStyle(true)
                .build();
    }

    private S3Presigner buildPresigner() {
        return S3Presigner.builder()
                .region(Region.of(resolveRegion()))
                .endpointOverride(buildEndpointUri())
                .credentialsProvider(buildCredentialsProvider())
                .serviceConfiguration(
                        S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    private StaticCredentialsProvider buildCredentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(config.getAccessKey(), config.getSecretKey()));
    }

    /**
     * 解析 endpoint 为完整 URI
     *
     * <p>AWS SDK v2 的 endpointOverride 要求 URI 必须包含协议（scheme），
     * 此处兼容省略协议的配置写法：缺省时按 is-https 补全，与 SDK 1.x 行为对齐
     */
    private URI buildEndpointUri() {
        String endpoint = config.getEndpoint().trim();
        if (!StrUtil.startWithIgnoreCase(endpoint, "http://") && !StrUtil.startWithIgnoreCase(endpoint, "https://")) {
            endpoint = (Boolean.TRUE.equals(config.getIsHttps()) ? "https://" : "http://") + endpoint;
        }
        return URI.create(endpoint);
    }

    private String resolveRegion() {
        return StrUtil.isNotBlank(config.getRegion()) ? config.getRegion() : DEFAULT_REGION;
    }

    /**
     * 创建存储桶
     *
     * <p>如果存储桶已存在，不会重复创建
     * <p>仅 MinIO 类型的 OSS 支持此操作
     * <p>自动设置访问策略（根据配置）
     *
     * @throws OssException 如果创建失败
     */
    @Override
    public void createBucket() {
        if (OssType.MINIO.equals(config.getType())) {
            try {
                String bucketName = config.getBucket();
                if (bucketExists(bucketName)) {
                    log.debug("存储桶已存在: {}", bucketName);
                    return;
                }
                CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .acl(getBucketAcl())
                        .build();
                client.createBucket(createBucketRequest);
                client.putBucketPolicy(PutBucketPolicyRequest.builder()
                        .bucket(bucketName)
                        .policy(getPolicy(bucketName, getAccessPolicy().getPolicyType()))
                        .build());
                log.info("存储桶创建成功: {}", bucketName);
            } catch (S3Exception e) {
                throw new OssException(String.format("创建存储桶失败: %s, 错误码: %s", config.getBucket(), errorCode(e)), e);
            } catch (Exception e) {
                throw new OssException("创建存储桶异常: " + config.getBucket(), e);
            }
        }
    }

    private boolean bucketExists(String bucketName) {
        try {
            client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        }
    }

    /**
     * 上传文件（字节数组）
     *
     * @param data        文件数据
     * @param objectKey   对象键
     * @param contentType 内容类型
     * @return 上传结果
     * @throws IllegalArgumentException 参数校验失败
     * @throws OssException             上传失败
     */
    @Override
    public UploadObjectResult upload(byte[] data, String objectKey, String contentType) {
        ValidationUtils.validateNotNull(data, "data");
        return upload(new ByteArrayInputStream(data), objectKey, contentType);
    }

    /**
     * 上传文件（输入流）
     *
     * @param inputStream 文件输入流
     * @param objectKey   对象键
     * @param contentType 内容类型
     * @return 上传结果
     * @throws IllegalArgumentException 参数校验失败
     * @throws OssException             上传失败
     */
    @Override
    public UploadObjectResult upload(InputStream inputStream, String objectKey, String contentType) {
        // 参数校验
        ValidationUtils.validateInputStream(inputStream);
        ValidationUtils.validateObjectKey(objectKey);
        ValidationUtils.validateContentType(contentType);

        try {
            // 计算内容长度
            long contentLength;
            byte[] bytes;

            if (inputStream instanceof ByteArrayInputStream) {
                contentLength = inputStream.available();
            } else {
                // 对于非 ByteArrayInputStream，需要读取到内存
                // TODO: 未来可以优化为流式上传，避免大文件 OOM
                bytes = IoUtil.readBytes(inputStream);
                inputStream = new ByteArrayInputStream(bytes);
                contentLength = bytes.length;
            }

            // 创建上传请求并执行
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(config.getBucket())
                    .key(objectKey)
                    .contentType(contentType)
                    .acl(getObjectAcl())
                    .build();
            client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));

            // 构建返回结果
            return UploadObjectResult.builder()
                    .url(buildPublicUrl(objectKey))
                    .key(objectKey)
                    .build();

        } catch (S3Exception e) {
            throw new OssException(
                    String.format("上传文件失败: %s, 错误码: %s, 错误信息: %s", objectKey, errorCode(e), e.getMessage()), e);
        } catch (Exception e) {
            throw new OssException("上传文件失败: " + objectKey, e);
        }
    }

    /**
     * 分片上传
     *
     * @param file            文件对象
     * @param objectKey       对象键
     * @param partNumber      当前分片号（从 1 开始）
     * @param partTotalNumber 总分片数
     * @throws IllegalArgumentException 参数校验失败
     * @throws OssException             上传失败
     */
    @Override
    public void uploadPart(File file, String objectKey, int partNumber, int partTotalNumber) {
        uploadPart(file, "application/octet-stream", objectKey, partNumber, partTotalNumber);
    }

    /**
     * 分片上传
     *
     * @param file            文件对象
     * @param contentType     内容类型
     * @param objectKey       对象键
     * @param partNumber      当前分片号（从 1 开始）
     * @param partTotalNumber 总分片数
     * @throws IllegalArgumentException 参数校验失败
     * @throws OssException             上传失败
     */
    @Override
    public void uploadPart(File file, String contentType, String objectKey, int partNumber, int partTotalNumber) {
        // 参数校验
        ValidationUtils.validateNotNull(file, "file");
        ValidationUtils.validateContentType(contentType);
        ValidationUtils.validateObjectKey(objectKey);
        ValidationUtils.validatePartNumbers(partNumber, partTotalNumber);

        if (!file.exists()) {
            throw new IllegalArgumentException("文件不存在: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("不是有效的文件: " + file.getAbsolutePath());
        }

        // 检查状态管理器是否已注入
        if (multipartUploadStateManager == null) {
            throw new IllegalStateException("MultipartUploadStateManager 未注入，无法使用分片上传功能！" + "请确保已配置 Redis 或使用内存状态管理器。");
        }

        try {
            String stateKey = objectKey + ":" + partTotalNumber;

            // 第一个分片时，清理可能存在的旧数据
            if (partNumber == 1 && multipartUploadStateManager.exists(stateKey)) {
                multipartUploadStateManager.deleteState(stateKey);
                log.debug("清理旧的分片上传状态: {}", stateKey);
            }

            // 获取或初始化上传 ID
            String uploadId = multipartUploadStateManager.getUploadId(stateKey);
            if (uploadId == null) {
                CreateMultipartUploadRequest initRequest = CreateMultipartUploadRequest.builder()
                        .bucket(config.getBucket())
                        .key(objectKey)
                        .contentType(contentType)
                        .build();
                uploadId = client.createMultipartUpload(initRequest).uploadId();
                multipartUploadStateManager.saveUploadId(stateKey, uploadId);
                log.debug("初始化分片上传: objectKey={}, uploadId={}", objectKey, uploadId);
            }

            // 获取已上传的分片标签
            List<PartTag> partTags = multipartUploadStateManager.getPartETags(stateKey);
            if (partTags == null) {
                partTags = new ArrayList<>();
            }

            // 上传当前分片
            UploadPartRequest uploadRequest = UploadPartRequest.builder()
                    .bucket(config.getBucket())
                    .key(objectKey)
                    .uploadId(uploadId)
                    .partNumber(partNumber)
                    .contentLength(file.length())
                    .build();
            UploadPartResponse uploadResult = client.uploadPart(uploadRequest, RequestBody.fromFile(file));
            partTags.add(new PartTag(partNumber, uploadResult.eTag()));

            log.debug("分片上传成功: objectKey={}, part={}/{}", objectKey, partNumber, partTotalNumber);

            // 如果是最后一个分片，完成上传
            if (partNumber == partTotalNumber) {
                List<CompletedPart> completedParts = partTags.stream()
                        .map(tag -> CompletedPart.builder()
                                .partNumber(tag.getPartNumber())
                                .eTag(tag.getETag())
                                .build())
                        .toList();
                CompleteMultipartUploadRequest compRequest = CompleteMultipartUploadRequest.builder()
                        .bucket(config.getBucket())
                        .key(objectKey)
                        .uploadId(uploadId)
                        .multipartUpload(CompletedMultipartUpload.builder()
                                .parts(completedParts)
                                .build())
                        .build();
                client.completeMultipartUpload(compRequest);
                multipartUploadStateManager.deleteState(stateKey);
                log.info("分片上传完成: objectKey={}, totalParts={}", objectKey, partTotalNumber);
            } else {
                // 保存分片标签
                multipartUploadStateManager.savePartETags(stateKey, partTags);
            }

        } catch (S3Exception e) {
            throw new OssException(
                    String.format(
                            "分片上传失败: %s, part=%d/%d, 错误码: %s", objectKey, partNumber, partTotalNumber, errorCode(e)),
                    e);
        } catch (Exception e) {
            throw new OssException(String.format("分片上传失败: %s, part=%d/%d", objectKey, partNumber, partTotalNumber), e);
        }
    }

    /**
     * 上传文件（文件对象）
     *
     * @param file      文件对象
     * @param objectKey 对象键
     * @return 上传结果
     * @throws IllegalArgumentException 参数校验失败
     * @throws OssException             上传失败
     */
    @Override
    public UploadObjectResult upload(File file, String objectKey) {
        ValidationUtils.validateNotNull(file, "file");
        ValidationUtils.validateObjectKey(objectKey);

        if (!file.exists()) {
            throw new IllegalArgumentException("文件不存在: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("不是有效的文件: " + file.getAbsolutePath());
        }

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(config.getBucket())
                    .key(objectKey)
                    .acl(getObjectAcl())
                    .build();
            client.putObject(putObjectRequest, RequestBody.fromFile(file));

            log.debug("文件上传成功: {}", objectKey);

            return UploadObjectResult.builder()
                    .url(buildPublicUrl(objectKey))
                    .key(objectKey)
                    .build();

        } catch (S3Exception e) {
            throw new OssException(String.format("上传文件失败: %s, 错误码: %s", objectKey, errorCode(e)), e);
        } catch (Exception e) {
            throw new OssException("上传文件失败: " + objectKey, e);
        }
    }

    /**
     * 删除文件
     *
     * @param objectKey 对象键
     * @throws IllegalArgumentException 参数校验失败
     * @throws OssException             删除失败
     */
    @Override
    public void delete(String objectKey) {
        ValidationUtils.validateObjectKey(objectKey);

        try {
            client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(config.getBucket())
                    .key(objectKey)
                    .build());
            log.debug("文件删除成功: {}", objectKey);
        } catch (S3Exception e) {
            throw new OssException(String.format("删除文件失败: %s, 错误码: %s", objectKey, errorCode(e)), e);
        } catch (Exception e) {
            throw new OssException("删除文件失败: " + objectKey, e);
        }
    }

    /**
     * 获取文件对象
     *
     * @param objectKey 对象键
     * @return OSS 对象（调用者负责关闭）
     * @throws IllegalArgumentException 参数校验失败
     * @throws OssException             获取失败
     */
    @Override
    public OssObject getObject(String objectKey) {
        ValidationUtils.validateObjectKey(objectKey);

        try {
            ResponseInputStream<GetObjectResponse> stream = client.getObject(GetObjectRequest.builder()
                    .bucket(config.getBucket())
                    .key(objectKey)
                    .build());
            try {
                GetObjectResponse response = stream.response();
                return OssObject.builder()
                        .bucket(config.getBucket())
                        .key(objectKey)
                        .contentType(response.contentType())
                        .contentLength(response.contentLength())
                        .content(stream)
                        .build();
            } catch (RuntimeException e) {
                stream.close();
                throw e;
            }
        } catch (S3Exception e) {
            throw new OssException(String.format("获取文件失败: %s, 错误码: %s", objectKey, errorCode(e)), e);
        } catch (Exception e) {
            throw new OssException("获取文件失败: " + objectKey, e);
        }
    }

    /**
     * 下载文件到输出流
     *
     * @param objectKey    对象键
     * @param outputStream 输出流
     * @throws IllegalArgumentException 参数校验失败
     * @throws OssException             下载失败
     */
    @Override
    public void outStream(String objectKey, OutputStream outputStream) {
        ValidationUtils.validateObjectKey(objectKey);
        ValidationUtils.validateNotNull(outputStream, "outputStream");

        try (ResponseInputStream<GetObjectResponse> stream = client.getObject(GetObjectRequest.builder()
                .bucket(config.getBucket())
                .key(objectKey)
                .build())) {

            IoUtil.copy(stream, outputStream);
            outputStream.flush();

            log.debug("文件下载成功: {}", objectKey);

        } catch (S3Exception e) {
            throw new OssException(String.format("下载文件失败: %s, 错误码: %s", objectKey, errorCode(e)), e);
        } catch (Exception e) {
            throw new OssException("下载文件失败: " + objectKey, e);
        }
    }

    /**
     * 获取私有 URL 链接
     *
     * @param objectKey         对象键
     * @param expirationSeconds 授权时间（秒）
     * @return 预签名 URL
     * @throws IllegalArgumentException 参数校验失败
     * @throws OssException             生成失败
     */
    @Override
    public String getPrivateUrl(String objectKey, Integer expirationSeconds) {
        ValidationUtils.validateObjectKey(objectKey);
        ValidationUtils.validateExpirationSeconds(expirationSeconds);

        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(expirationSeconds))
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(config.getBucket())
                            .key(objectKey)
                            .build())
                    .build();
            String url = presigner.presignGetObject(presignRequest).url().toString();

            log.debug("生成预签名 URL: objectKey={}, expiration={}s", objectKey, expirationSeconds);

            return url;
        } catch (S3Exception e) {
            throw new OssException(String.format("生成预签名 URL 失败: %s, 错误码: %s", objectKey, errorCode(e)), e);
        } catch (Exception e) {
            throw new OssException("生成预签名 URL 失败: " + objectKey, e);
        }
    }

    /**
     * 获取当前桶权限类型
     *
     * @return 当前桶权限类型
     */
    @Override
    public AccessPolicyType getAccessPolicy() {
        return AccessPolicyType.getByType(config.getAccessPolicy());
    }

    /**
     * 获取对象级 ACL（对应 SDK 1.x CannedAccessControlList 映射）
     *
     * @return 对象 ACL
     */
    private ObjectCannedACL getObjectAcl() {
        return switch (getAccessPolicy()) {
            case PRIVATE -> ObjectCannedACL.PRIVATE;
            case PUBLIC, CUSTOM -> ObjectCannedACL.PUBLIC_READ;
        };
    }

    /**
     * 获取桶级 ACL（对应 SDK 1.x CannedAccessControlList 映射）
     *
     * @return 桶 ACL
     */
    private BucketCannedACL getBucketAcl() {
        return switch (getAccessPolicy()) {
            case PRIVATE -> BucketCannedACL.PRIVATE;
            case PUBLIC, CUSTOM -> BucketCannedACL.PUBLIC_READ;
        };
    }

    /**
     * 提取 S3 异常错误码
     *
     * @param e S3 异常
     * @return 错误码（无法解析时返回 HTTP 状态码）
     */
    private static String errorCode(S3Exception e) {
        return e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : String.valueOf(e.statusCode());
    }

    /**
     * 构建对象 URL
     *
     * @param objectKey 对象键
     * @return 完整的对象 URL
     */
    private String buildPublicUrl(String objectKey) {
        if (StrUtil.isEmpty(config.getCdn())) {
            String endpoint = config.getEndpoint();
            if (!endpoint.endsWith("/")) {
                endpoint += "/";
            }
            return endpoint + config.getBucket() + "/" + objectKey;
        }
        String endpoint = config.getCdn();
        if (!endpoint.endsWith("/")) {
            endpoint += "/";
        }
        return endpoint + objectKey;
    }

    private static String getPolicy(String bucketName, PolicyType policyType) {
        return MinIOPolicyBuilder.buildPolicy(bucketName, policyType);
    }
}
