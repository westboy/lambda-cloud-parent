# Lambda Cloud OSS Starter

基于Spring Boot的OSS统一接入模块，支持多种对象存储服务。

## 功能特性

- 统一API接入多种OSS服务
- 支持多客户端配置
- 文件上传、下载、删除
- 分片上传大文件
- 生成预签名URL
- 设置存储桶权限策略

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.lambda</groupId>
    <artifactId>lambda-cloud-starter-oss</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 2. 配置说明

```yaml
lambda:
  oss:
    enabled: true # 是否启用自动配置(默认true)
    clients:
      - name: default # 客户端名称
        type: MINIO # 服务类型: MINIO|ALIYUN|QCLOUD|QINIU|OTHER
        endpoint: http://your-oss-endpoint
        accessKey: your-access-key
        secretKey: your-secret-key
        region: your-region # 可选
        bucket: your-bucket-name # 存储桶名称
        isHttps: false # 是否使用HTTPS
        policyType: READ_WRITE # MinIO专用: READ|WRITE|READ_WRITE
        accessPolicy: PRIVATE # MinIO专用: PRIVATE|PUBLIC|CUSTOM
      - name: backup # 可配置多个客户端
        type: ALIYUN
        endpoint: http://backup-endpoint
        accessKey: backup-key
        secretKey: backup-secret
```

### 3. 使用示例

```java
@Autowired
private OssClientManager ossClientManager;

// 上传文件
public void uploadFile() {
    InputStream inputStream = new FileInputStream("test.txt");
    String objectKey = "test-folder/test.txt";
    ossClientManager.get("default").upload(objectKey, inputStream);
}


// 使用特定客户端
public void useSpecificClient() {
    OssClient backupClient = ossClientManager.get("default ");
    backupClient.upload("backup/test.txt", inputStream);
}
```

## 注意事项

1. 配置前缀为"lambda.oss"
2. 必须配置bucket名称
3. 使用HTTPS时需要设置isHttps=true
4. MinIO专用配置(policyType/accessPolicy)仅对MINIO类型有效
5. 分片上传建议用于大文件(>100MB)
6. 预签名URL需设置合理的过期时间
7. 多客户端配置时需区分name属性
8. 支持的OSS类型: MINIO, ALIYUN, QCLOUD, QINIU, OTHER
9. 支持的访问策略: PRIVATE, PUBLIC, CUSTOM
10. 支持的策略类型: READ, WRITE, READ_WRITE
