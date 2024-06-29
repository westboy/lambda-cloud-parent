package com.jingfang.cloud.minio.client;

import com.google.common.collect.Multimap;
import io.minio.*;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Part;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * MultipartMinioClient
 *
 * @author jpjoo
 */
public class MultipartMinioClient extends MinioAsyncClient {

    public MultipartMinioClient(MinioAsyncClient client) {
        super(client);
    }

    /**
     * 创建分片上传请求
     *
     * @param bucket           存储桶
     * @param region           区域
     * @param objectName       对象名
     * @param headers          消息头
     * @param extraQueryParams 额外查询参数
     */
    public String multipartUpload(String bucket, String region, String objectName, Multimap<String, String> headers, Multimap<String, String> extraQueryParams)
            throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InternalException,
            XmlParserException, InterruptedException, ExecutionException {
        CompletableFuture<CreateMultipartUploadResponse> response = this.createMultipartUploadAsync(bucket, region, objectName, headers, extraQueryParams);
        return response.get().result().uploadId();
    }

    /**
     * 分片上传
     *
     * @param bucketName       Name of the bucket.
     * @param region           Region of the bucket (Optional).
     * @param objectName       Object name in the bucket.
     * @param data             Object data must be InputStream, RandomAccessFile, byte[] or String.
     * @param length           Length of object data.
     * @param uploadId         Upload ID.
     * @param partNumber       Part number.
     * @param extraHeaders     Extra headers for request (Optional).
     * @param extraQueryParams Extra query parameters for request (Optional).
     * @throws IOException
     * @throws XmlParserException
     * @throws NoSuchAlgorithmException
     * @throws InternalException
     * @throws InsufficientDataException
     * @throws InvalidKeyException
     */
    public CompletableFuture<UploadPartResponse> uploadPartAsync(String bucketName, String region, String objectName, Object data,
                                                                 long length, String uploadId, int partNumber, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams)
            throws InvalidKeyException, InsufficientDataException, InternalException, NoSuchAlgorithmException, XmlParserException, IOException {
        return super.uploadPartAsync(bucketName, region, objectName, data, length, uploadId, partNumber, extraHeaders, extraQueryParams);
    }


    /**
     * 完成分片上传，执行合并文件
     *
     * @param bucketName       存储桶
     * @param region           区域
     * @param objectName       对象名
     * @param uploadId         上传ID
     * @param parts            分片
     * @param extraHeaders     额外消息头
     * @param extraQueryParams 额外查询参数
     */
    @Override
    public CompletableFuture<ObjectWriteResponse> completeMultipartUploadAsync(String bucketName, String region, String objectName, String uploadId, Part[] parts, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams)
            throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return super.completeMultipartUploadAsync(bucketName, region, objectName, uploadId, parts, extraHeaders, extraQueryParams);
    }

    /**
     * 查询分片数据
     *
     * @param bucketName       存储桶
     * @param region           区域
     * @param objectName       对象名
     * @param uploadId         上传ID
     * @param extraHeaders     额外消息头
     * @param extraQueryParams 额外查询参数
     */
    @Override
    public CompletableFuture<ListPartsResponse> listPartsAsync(String bucketName, String region, String objectName, Integer maxParts, Integer partNumberMarker, String uploadId, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws InsufficientDataException, InternalException, InvalidKeyException, IOException, NoSuchAlgorithmException, XmlParserException {
        return super.listPartsAsync(bucketName, region, objectName, maxParts, partNumberMarker, uploadId, extraHeaders, extraQueryParams);
    }
}