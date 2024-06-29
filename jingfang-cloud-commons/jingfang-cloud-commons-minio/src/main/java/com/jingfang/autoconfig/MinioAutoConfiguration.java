package com.jingfang.autoconfig;

import com.jingfang.cloud.minio.client.MultipartMinioClient;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioAsyncClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;


@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MinioProperties.class)
public class MinioAutoConfiguration {

    private MinioProperties minioProperties;

    @Autowired
    public void setMinioProperties(MinioProperties minioProperties) {
        this.minioProperties = minioProperties;
    }

    @Bean
    public MultipartMinioClient minioAsyncClient() {
        return new MultipartMinioClient(MinioAsyncClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getAccessSecret())
                .build());
    }

    @PostConstruct
    public void createBucket(MultipartMinioClient minioAsyncClient) {
        try {
            minioAsyncClient.bucketExists(BucketExistsArgs
                            .builder()
                            .bucket(minioProperties.getBucket())
                            .build())
                    .thenAcceptAsync(created -> {
                        if (!created) {
                            try {
                                minioAsyncClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucket()).build());
                            } catch (Exception e) {
                                log.error("Failed to make bucket", e);
                            }
                        }
                    });
        } catch (Exception e) {
            log.error("Error creating bucket", e);
        }
    }
}
