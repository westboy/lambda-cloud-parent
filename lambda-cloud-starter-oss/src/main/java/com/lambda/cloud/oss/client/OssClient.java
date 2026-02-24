package com.lambda.cloud.oss.client;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.HttpMethod;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.lambda.autoconfig.OssProperties;
import com.lambda.cloud.oss.enums.AccessPolicyType;
import com.lambda.cloud.oss.enums.OssType;
import com.lambda.cloud.oss.enums.PolicyType;
import com.lambda.cloud.oss.exception.OssException;
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
import java.net.URL;
import java.util.Date;
import java.util.List;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * OSS 客户端实现
 * 提供对象存储的核心操作功能
 * 
 * <p>该类实现了 {@link OssService} 接口，基于 AWS S3 SDK 提供统一的对象存储操作。
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
 * @author westboy
 * @author jpjoo
 * @since 2025.1.1
 * @see OssService
 * @see com.lambda.cloud.oss.manager.OssClientManager
 */
@Slf4j
@SuppressFBWarnings(value = {"EI_EXPOSE_REP2"})
public class OssClient implements OssService {

    private final OssProperties.Config config;

    private final AmazonS3 client;

    @Setter
    private MultipartUploadStateManager multipartUploadStateManager;

    public OssClient(OssProperties.Config config) {
        this.config = config;
        this.client = buildAmazonS3();
    }

    private AmazonS3 buildAmazonS3() {
        AwsClientBuilder.EndpointConfiguration endpointConfig =
                new AwsClientBuilder.EndpointConfiguration(config.getEndpoint(), config.getRegion());
        AWSCredentials credentials = new BasicAWSCredentials(config.getAccessKey(), config.getSecretKey());
        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        ClientConfiguration clientConfig = new ClientConfiguration();
        if (config.getIsHttps()) {
            if (!StrUtil.startWith(config.getEndpoint(), "https")) {
                log.error("Endpoint 配置不正确，https 已开启！");
            }
            clientConfig.setProtocol(Protocol.HTTPS);
        } else {
            clientConfig.setProtocol(Protocol.HTTP);
        }

        clientConfig.setConnectionTimeout(config.getHttpClientConfig().getConnectionTimeout());
        clientConfig.setSocketTimeout(config.getHttpClientConfig().getSocketTimeout());
        clientConfig.setMaxConnections(config.getHttpClientConfig().getMaxConnections());
        clientConfig.setRequestTimeout(config.getHttpClientConfig().getRequestTimeout());
        clientConfig.setClientExecutionTimeout(config.getHttpClientConfig().getClientExecutionTimeout());
        clientConfig.setConnectionTTL(config.getHttpClientConfig().getConnectionTTL());
        clientConfig.setConnectionMaxIdleMillis(config.getHttpClientConfig().getConnectionMaxIdleMillis());

        AmazonS3ClientBuilder build = AmazonS3Client.builder()
                .withEndpointConfiguration(endpointConfig)
                .withClientConfiguration(clientConfig)
                .withCredentials(credentialsProvider)
                .disableChunkedEncoding();
        if (OssType.MINIO.name().equalsIgnoreCase(config.getType())) {
            build.enablePathStyleAccess();
        }
        return build.build();
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
        if (OssType.MINIO.name().equalsIgnoreCase(config.getType())) {
            try {
                String bucketName = config.getBucket();
                if (client.doesBucketExistV2(bucketName)) {
                    log.debug("存储桶已存在: {}", bucketName);
                    return;
                }
                CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
                AccessPolicyType accessPolicy = getAccessPolicy();
                createBucketRequest.setCannedAcl(accessPolicy.getAcl());
                client.createBucket(createBucketRequest);
                client.setBucketPolicy(bucketName, getPolicy(bucketName, accessPolicy.getPolicyType()));
                log.info("存储桶创建成功: {}", bucketName);
            } catch (AmazonServiceException e) {
                throw new OssException(
                    String.format("创建存储桶失败: %s, 错误码: %s", config.getBucket(), e.getErrorCode()), 
                    e
                );
            } catch (Exception e) {
                throw new OssException("创建存储桶异常: " + config.getBucket(), e);
            }
        }
    }


    /**
     * 上传文件（字节数组）
     * 
     * @param data 文件数据
     * @param objectKey 对象键
     * @param contentType 内容类型
     * @return 上传结果
     * @throws IllegalArgumentException 参数校验失败
     * @throws OssException 上传失败
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
     * @param objectKey 对象键
     * @param contentType 内容类型
     * @return 上传结果
     * @throws IllegalArgumentException 参数校验失败
     * @throws OssException 上传失败
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
            byte[] bytes = null;
            
            if (inputStream instanceof ByteArrayInputStream) {
                contentLength = inputStream.available();
            } else {
                // 对于非 ByteArrayInputStream，需要读取到内存
                // TODO: 未来可以优化为流式上传，避免大文件 OOM
                bytes = IoUtil.readBytes(inputStream);
                inputStream = new ByteArrayInputStream(bytes);
                contentLength = bytes.length;
            }
            
            // 创建元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(contentLength);
            
            // 创建上传请求
            PutObjectRequest putObjectRequest = new PutObjectRequest(config.getBucket(), objectKey, inputStream, metadata);
            putObjectRequest.setCannedAcl(getAccessPolicy().getAcl());
            
            // 执行上传
            client.putObject(putObjectRequest);
            
            log.debug("文件上传成功: {}", objectKey);
            
            // 构建返回结果
            return UploadObjectResult.builder()
                    .url(buildObjectUrl(objectKey))
                    .key(objectKey)
                    .build();
                    
        } catch (AmazonS3Exception e) {
            throw new OssException(
                String.format("上传文件失败: %s, 错误码: %s, 错误信息: %s", 
                    objectKey, e.getErrorCode(), e.getErrorMessage()), 
                e
            );
        } catch (Exception e) {
            throw new OssException("上传文件失败: " + objectKey, e);
        }
    }

    /**
     * 分片上传
     * 
     * @param file 文件对象
     * @param objectKey 对象键
     * @param partNumber 当前分片号（从 1 开始）
     * @param partTotalNumber 总分片数
     * @throws IllegalArgumentException 参数校验失败
     * @throws OssException 上传失败
     */
    @Override
    public void uploadPart(File file, String objectKey, int partNumber, int partTotalNumber) {
        uploadPart(file, "application/octet-stream", objectKey, partNumber, partTotalNumber);
    }

    /**
     * 分片上传
     * 
     * @param file 文件对象
     * @param contentType 内容类型
     * @param objectKey 对象键
     * @param partNumber 当前分片号（从 1 开始）
     * @param partTotalNumber 总分片数
     * @throws IllegalArgumentException 参数校验失败
     * @throws OssException 上传失败
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
            throw new IllegalStateException(
                "MultipartUploadStateManager 未注入，无法使用分片上传功能！" +
                "请确保已配置 Redis 或使用内存状态管理器。"
            );
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
                InitiateMultipartUploadRequest initRequest =
                        new InitiateMultipartUploadRequest(config.getBucket(), objectKey);
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(contentType);
                initRequest.withObjectMetadata(metadata);
                InitiateMultipartUploadResult initResponse = client.initiateMultipartUpload(initRequest);
                uploadId = initResponse.getUploadId();
                multipartUploadStateManager.saveUploadId(stateKey, uploadId);
                log.debug("初始化分片上传: objectKey={}, uploadId={}", objectKey, uploadId);
            }

            // 获取已上传的分片标签
            List<PartETag> partETags = multipartUploadStateManager.getPartETags(stateKey);
            if (partETags == null) {
                partETags = new java.util.ArrayList<>();
            }

            // 上传当前分片
            UploadPartRequest uploadRequest = new UploadPartRequest()
                    .withBucketName(config.getBucket())
                    .withKey(objectKey)
                    .withUploadId(uploadId)
                    .withPartNumber(partNumber)
                    .withFile(file)
                    .withPartSize(file.length());

            UploadPartResult uploadResult = client.uploadPart(uploadRequest);
            partETags.add(uploadResult.getPartETag());
            
            log.debug("分片上传成功: objectKey={}, part={}/{}", objectKey, partNumber, partTotalNumber);

            // 如果是最后一个分片，完成上传
            if (partNumber == partTotalNumber) {
                CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(
                        config.getBucket(), objectKey, uploadId, partETags);
                client.completeMultipartUpload(compRequest);
                multipartUploadStateManager.deleteState(stateKey);
                log.info("分片上传完成: objectKey={}, totalParts={}", objectKey, partTotalNumber);
            } else {
                // 保存分片标签
                multipartUploadStateManager.savePartETags(stateKey, partETags);
            }

        } catch (AmazonS3Exception e) {
            throw new OssException(
                String.format("分片上传失败: %s, part=%d/%d, 错误码: %s", 
                    objectKey, partNumber, partTotalNumber, e.getErrorCode()), 
                e
            );
        } catch (Exception e) {
            throw new OssException(
                String.format("分片上传失败: %s, part=%d/%d", objectKey, partNumber, partTotalNumber), 
                e
            );
        }
    }

    /**
     * 上传文件（文件对象）
     * 
     * @param file 文件对象
     * @param objectKey 对象键
     * @return 上传结果
     * @throws IllegalArgumentException 参数校验失败
     * @throws OssException 上传失败
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
            PutObjectRequest putObjectRequest = new PutObjectRequest(config.getBucket(), objectKey, file);
            putObjectRequest.setCannedAcl(getAccessPolicy().getAcl());
            client.putObject(putObjectRequest);
            
            log.debug("文件上传成功: {}", objectKey);
            
            return UploadObjectResult.builder()
                    .url(buildObjectUrl(objectKey))
                    .key(objectKey)
                    .build();
                    
        } catch (AmazonS3Exception e) {
            throw new OssException(
                String.format("上传文件失败: %s, 错误码: %s", objectKey, e.getErrorCode()), 
                e
            );
        } catch (Exception e) {
            throw new OssException("上传文件失败: " + objectKey, e);
        }
    }

    /**
     * 删除文件
     * 
     * @param objectKey 对象键
     * @throws IllegalArgumentException 参数校验失败
     * @throws OssException 删除失败
     */
    @Override
    public void delete(String objectKey) {
        ValidationUtils.validateObjectKey(objectKey);
        
        try {
            client.deleteObject(config.getBucket(), objectKey);
            log.debug("文件删除成功: {}", objectKey);
        } catch (AmazonS3Exception e) {
            throw new OssException(
                String.format("删除文件失败: %s, 错误码: %s", objectKey, e.getErrorCode()), 
                e
            );
        } catch (Exception e) {
            throw new OssException("删除文件失败: " + objectKey, e);
        }
    }

    /**
     * 获取文件对象
     * 
     * @param objectKey 对象键
     * @return S3 对象
     * @throws IllegalArgumentException 参数校验失败
     * @throws OssException 获取失败
     */
    @Override
    public S3Object getObject(String objectKey) {
        ValidationUtils.validateObjectKey(objectKey);
        
        try {
            return client.getObject(config.getBucket(), objectKey);
        } catch (AmazonS3Exception e) {
            throw new OssException(
                String.format("获取文件失败: %s, 错误码: %s", objectKey, e.getErrorCode()), 
                e
            );
        } catch (Exception e) {
            throw new OssException("获取文件失败: " + objectKey, e);
        }
    }

    /**
     * 下载文件到输出流
     * 
     * @param objectKey 对象键
     * @param outputStream 输出流
     * @throws IllegalArgumentException 参数校验失败
     * @throws OssException 下载失败
     */
    @Override
    public void outStream(String objectKey, OutputStream outputStream) {
        ValidationUtils.validateObjectKey(objectKey);
        ValidationUtils.validateNotNull(outputStream, "outputStream");
        
        try (S3Object s3Object = client.getObject(config.getBucket(), objectKey);
             S3ObjectInputStream s3is = s3Object.getObjectContent()) {
            
            IoUtil.copy(s3is, outputStream);
            outputStream.flush();
            
            log.debug("文件下载成功: {}", objectKey);
            
        } catch (AmazonS3Exception e) {
            throw new OssException(
                String.format("下载文件失败: %s, 错误码: %s", objectKey, e.getErrorCode()), 
                e
            );
        } catch (Exception e) {
            throw new OssException("下载文件失败: " + objectKey, e);
        }
    }

    /**
     * 获取私有 URL 链接
     *
     * @param objectKey 对象键
     * @param expirationSeconds 授权时间（秒）
     * @return 预签名 URL
     * @throws IllegalArgumentException 参数校验失败
     * @throws OssException 生成失败
     */
    @Override
    public String getPrivateUrl(String objectKey, Integer expirationSeconds) {
        ValidationUtils.validateObjectKey(objectKey);
        ValidationUtils.validateExpirationSeconds(expirationSeconds);
        
        try {
            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
                            config.getBucket(), objectKey)
                    .withMethod(HttpMethod.GET)
                    .withExpiration(new Date(System.currentTimeMillis() + 1000L * expirationSeconds));
            URL url = client.generatePresignedUrl(generatePresignedUrlRequest);
            
            log.debug("生成预签名 URL: objectKey={}, expiration={}s", objectKey, expirationSeconds);
            
            return url.toString();
        } catch (AmazonS3Exception e) {
            throw new OssException(
                String.format("生成预签名 URL 失败: %s, 错误码: %s", objectKey, e.getErrorCode()), 
                e
            );
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
     * 构建对象 URL
     * 
     * @param objectKey 对象键
     * @return 完整的对象 URL
     */
    private String buildObjectUrl(String objectKey) {
        String endpoint = config.getEndpoint();
        if (!endpoint.endsWith("/")) {
            endpoint += "/";
        }
        return endpoint + config.getBucket() + "/" + objectKey;
    }

    private static String getPolicy(String bucketName, PolicyType policyType) {
        return MinIOPolicyBuilder.buildPolicy(bucketName, policyType);
    }
}
