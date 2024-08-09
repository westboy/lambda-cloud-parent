package com.jingfang.cloud.minio;

import com.jingfang.cloud.minio.client.MultipartMinioClient;
import io.minio.MinioAsyncClient;
import io.minio.ObjectWriteResponse;
import io.minio.UploadObjectArgs;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.XmlParserException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MinioTest {

    public MultipartMinioClient minioAsyncClient() {
        return new MultipartMinioClient(MinioAsyncClient.builder()
                .endpoint("http://1.92.120.86:9000")
                .credentials("UTazyrHyOrhi20c4UhGJ", "KfRPFcNIUSPyRCs79XQLvHExME8oTdQ5biaonrYu")
                .build());
    }


    public static void main(String[] args) {
        MinioTest minioTest = new MinioTest();
        try (MultipartMinioClient minioClient = minioTest.minioAsyncClient()) {
            try {
                CompletableFuture<ObjectWriteResponse> minio1 = minioClient.uploadObject(UploadObjectArgs.builder()
                        .bucket("test1")
                        .filename("C:\\Users\\jpjoo\\.calis-metadata-ddc\\windows_amd_x64\\rclone.zip")
                        .object("RCLONE.ZIP")
                        .build());
                ObjectWriteResponse objectWriteResponse = minio1.get();
                System.out.println(objectWriteResponse);
            } catch (InsufficientDataException | InvalidKeyException | InternalException | IOException |
                     NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
