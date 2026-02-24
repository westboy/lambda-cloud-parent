package com.lambda.cloud.oss.enums;

/**
 * OSS 类型枚举
 * 
 * @author jpjoo
 */
public enum OssType {
    /**
     * MinIO 对象存储
     */
    MINIO,
    
    /**
     * 阿里云 OSS
     */
    ALIYUN,
    
    /**
     * 腾讯云 COS
     */
    QCLOUD,
    
    /**
     * 七牛云
     */
    QINIU,
    
    /**
     * 其他兼容 S3 协议的对象存储
     */
    OTHER
}
