package com.jingfang.cloud.oss.client;


import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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
import com.jingfang.autoconfig.OssProperties;
import com.jingfang.cloud.core.exception.IllegalStateException;
import com.jingfang.cloud.oss.enums.AccessPolicyType;
import com.jingfang.cloud.oss.enums.OssType;
import com.jingfang.cloud.oss.enums.PolicyType;
import com.jingfang.cloud.oss.exception.OssException;
import com.jingfang.cloud.oss.model.UploadObjectResult;
import com.jingfang.cloud.oss.model.UploadPartTag;
import com.jingfang.cloud.redis.utils.RedisUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;

/**
 * OssClient
 *
 * @author westboy
 */
public class OssClient {

    private final OssProperties.Config config;

    private final AmazonS3 client;

    public OssClient(OssProperties.Config config) {
        this.config = config;
        try {
            AwsClientBuilder.EndpointConfiguration endpointConfig =
                    new AwsClientBuilder.EndpointConfiguration(config.getEndpoint(), config.getRegion());

            AWSCredentials credentials = new BasicAWSCredentials(config.getAccessKey(), config.getSecretKey());
            AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
            ClientConfiguration clientConfig = new ClientConfiguration();
            if (config.getIsHttps()) {
                if (!StrUtil.startWith(config.getEndpoint(), "https")) {
                    throw new IllegalStateException("Endpoint 配置不正确，https 已开启！");
                }
                clientConfig.setProtocol(Protocol.HTTPS);
            } else {
                clientConfig.setProtocol(Protocol.HTTP);
            }
            AmazonS3ClientBuilder build = AmazonS3Client.builder()
                    .withEndpointConfiguration(endpointConfig)
                    .withClientConfiguration(clientConfig)
                    .withCredentials(credentialsProvider)
                    .disableChunkedEncoding();
            if (OssType.MINIO.name().equalsIgnoreCase(config.getType())) {
                build.enablePathStyleAccess();
            }
            this.client = build.build();

            createBucket();
        } catch (Exception e) {
            throw new OssException("OSS 初始化异常！", e);
        }
    }

    public void createBucket() {
        try {
            String bucketName = config.getBucket();
            if (client.doesBucketExistV2(bucketName)) {
                return;
            }
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
            AccessPolicyType accessPolicy = getAccessPolicy();
            createBucketRequest.setCannedAcl(accessPolicy.getAcl());
            client.createBucket(createBucketRequest);
            client.setBucketPolicy(bucketName, getPolicy(bucketName, accessPolicy.getPolicyType()));
        } catch (Exception e) {
            throw new OssException("创建存储桶异常！", e);
        }
    }

    public UploadObjectResult upload(byte[] data, String dest, String contentType) {
        return upload(new ByteArrayInputStream(data), dest, contentType);
    }

    public UploadObjectResult upload(InputStream inputStream, String dest, String contentType) {
        if (!(inputStream instanceof ByteArrayInputStream)) {
            inputStream = new ByteArrayInputStream(IoUtil.readBytes(inputStream));
        }
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(inputStream.available());
            PutObjectRequest putObjectRequest = new PutObjectRequest(config.getBucket(), dest, inputStream, metadata);
            putObjectRequest.setCannedAcl(getAccessPolicy().getAcl());
            client.putObject(putObjectRequest);
            return UploadObjectResult.builder().url(config.getEndpoint() + "/" + dest).key(dest).build();
        } catch (Exception e) {
            throw new OssException("文件上传异常！", e);
        }

    }

    public void uploadPart(File file, String dest, int partNumber, int partTotalNumber) {
        try {

            String KEY = "s3-upload:part-" + dest + "-" + partTotalNumber;

            String uploadId = (String) RedisUtils.me().hGet(KEY, "uploadId");

            if (uploadId == null) {
                InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(config.getBucket(), dest);
                InitiateMultipartUploadResult initResponse = client.initiateMultipartUpload(initRequest);
                uploadId = initResponse.getUploadId();
                RedisUtils.me().hPut(KEY, "uploadId", uploadId);
            }

            String partETags = (String) RedisUtils.me().hGet(KEY, "partETags");

            UploadPartTag uploadPartTag;

            if (StrUtil.isNotEmpty(partETags)) {
                uploadPartTag = JSONUtil.toBean(partETags, UploadPartTag.class);
            } else {
                uploadPartTag = new UploadPartTag();
            }

            UploadPartRequest uploadRequest = new UploadPartRequest()
                    .withBucketName(config.getBucket())
                    .withKey(dest)
                    .withUploadId(uploadId)
                    .withPartNumber(partNumber)
                    .withFile(file)
                    .withPartSize(file.length());

            UploadPartResult uploadResult = client.uploadPart(uploadRequest);

            uploadPartTag.addPartETag(uploadResult.getPartETag());

            if (partNumber == partTotalNumber) {
                CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(config.getBucket(), dest,
                        uploadId, uploadPartTag.getPartETags());
                client.completeMultipartUpload(compRequest);
                RedisUtils.me().delete(KEY);
            } else {
                RedisUtils.me().hPut(KEY, "partETags", JSONUtil.toJsonStr(uploadPartTag));
            }

        } catch (Exception e) {
            throw new OssException("上传文件失败！", e);
        }
    }

    public UploadObjectResult upload(File file, String dest) {
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(config.getBucket(), dest, file);
            putObjectRequest.setCannedAcl(getAccessPolicy().getAcl());
            client.putObject(putObjectRequest);
        } catch (Exception e) {
            throw new OssException("上传文件失败！", e);
        }
        return UploadObjectResult.builder().url(config.getEndpoint() + "/" + dest).key(dest).build();
    }

    public void delete(String dest) {
        try {
            client.deleteObject(config.getBucket(), dest);
        } catch (Exception e) {
            throw new OssException("文件删除失败！", e);
        }
    }

    public S3Object getObject(String dest) {
        try {
            return client.getObject(config.getBucket(), dest);
        } catch (Exception e) {
            throw new OssException("文件获取失败！", e);
        }
    }

    public void outStream(String dest, OutputStream outputStream) {
        try {
            S3Object object = client.getObject(config.getBucket(), dest);
            IoUtil.copy(object.getObjectContent(), outputStream);
            IoUtil.close(object);
            IoUtil.close(outputStream);
        } catch (Exception e) {
            throw new OssException("文件删除失败！", e);
        }
    }


    /**
     * 获取私有URL链接
     *
     * @param objectKey 对象KEY
     * @param second    授权时间
     */
    public String getPrivateUrl(String objectKey, Integer second) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(config.getBucket(), objectKey)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(new Date(System.currentTimeMillis() + 1000L * second));
        URL url = client.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    /**
     * 获取当前桶权限类型
     *
     * @return 当前桶权限类型code
     */
    public AccessPolicyType getAccessPolicy() {
        return AccessPolicyType.getByType(config.getAccessPolicy());
    }


    private static String getPolicy(String bucketName, PolicyType policyType) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n\"Statement\": [\n{\n\"Action\": [\n");
        if (policyType == PolicyType.WRITE) {
            builder.append("\"s3:GetBucketLocation\",\n\"s3:ListBucketMultipartUploads\"\n");
        } else if (policyType == PolicyType.READ_WRITE) {
            builder.append("\"s3:GetBucketLocation\",\n\"s3:ListBucket\",\n\"s3:ListBucketMultipartUploads\"\n");
        } else {
            builder.append("\"s3:GetBucketLocation\"\n");
        }
        builder.append("],\n\"Effect\": \"Allow\",\n\"Principal\": \"*\",\n\"Resource\": \"arn:aws:s3:::");
        builder.append(bucketName);
        builder.append("\"\n},\n");
        if (policyType == PolicyType.READ) {
            builder.append("{\n\"Action\": [\n\"s3:ListBucket\"\n],\n\"Effect\": \"Deny\",\n\"Principal\": \"*\",\n\"Resource\": \"arn:aws:s3:::");
            builder.append(bucketName);
            builder.append("\"\n},\n");
        }
        builder.append("{\n\"Action\": ");
        switch (policyType) {
            case WRITE:
                builder.append("[\n\"s3:AbortMultipartUpload\",\n\"s3:DeleteObject\",\n\"s3:ListMultipartUploadParts\",\n\"s3:PutObject\"\n],\n");
                break;
            case READ_WRITE:
                builder.append("[\n\"s3:AbortMultipartUpload\",\n\"s3:DeleteObject\",\n\"s3:GetObject\",\n\"s3:ListMultipartUploadParts\",\n\"s3:PutObject\"\n],\n");
                break;
            default:
                builder.append("\"s3:GetObject\",\n");
                break;
        }
        builder.append("\"Effect\": \"Allow\",\n\"Principal\": \"*\",\n\"Resource\": \"arn:aws:s3:::");
        builder.append(bucketName);
        builder.append("/*\"\n}\n],\n\"Version\": \"2012-10-17\"\n}\n");
        return builder.toString();
    }

}
