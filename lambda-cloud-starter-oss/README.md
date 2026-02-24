# Lambda Cloud OSS Starter

基于Spring Boot的OSS统一接入模块，支持多种对象存储服务。

## 功能特性

- 统一API接入多种OSS服务
- 支持多客户端配置
- 文件上传、下载、删除
- 分片上传大文件
- 生成预签名URL
- 设置存储桶权限策略
- 完善的参数校验和异常处理
- 自动资源管理，防止资源泄漏
- 灵活的状态管理（支持 Redis 和内存两种实现）
- 配置自动校验（JSR-303）

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
      - name: default # 客户端名称（必填，只能包含字母、数字、下划线和连字符）
        type: MINIO # 服务类型: MINIO|ALIYUN|QCLOUD|QINIU|OTHER（必填）
        endpoint: http://your-oss-endpoint # OSS 服务端点（必填）
        accessKey: your-access-key # 访问密钥 ID（必填）
        secretKey: your-secret-key # 访问密钥（必填）
        region: your-region # 区域（可选）
        bucket: your-bucket-name # 存储桶名称（必填，3-63个字符，符合 DNS 命名规范）
        isHttps: false # 是否使用 HTTPS（必填）
        accessPolicy: private # 访问策略: private|public|custom（可选）
        httpClientConfig: # HTTP 客户端配置（可选）
          connectionTimeout: 10000 # 连接超时（毫秒，不小于 1000）
          socketTimeout: 50000 # Socket 超时（毫秒，不小于 1000）
          maxConnections: 50 # 最大连接数（不小于 1）
          requestTimeout: 0 # 请求超时（毫秒，0 表示无限制）
          clientExecutionTimeout: 0 # 客户端执行超时（毫秒，0 表示无限制）
          connectionTTL: -1 # 连接 TTL（毫秒，-1 表示无限制）
          connectionMaxIdleMillis: 60000 # 连接最大空闲时间（毫秒）
      - name: backup # 可配置多个客户端
        type: ALIYUN
        endpoint: http://backup-endpoint
        accessKey: backup-key
        secretKey: backup-secret
        bucket: backup-bucket
        isHttps: false
```

**配置校验说明**：
- 所有标记为"必填"的字段在启动时会自动校验
- 如果配置不符合要求，应用启动时会抛出异常并提供清晰的错误信息
- 存储桶名称必须符合 DNS 命名规范（小写字母、数字、连字符，3-63个字符）

### 3. 使用示例

#### 基本使用

```java
@Autowired
private OssClientManager ossClientManager;

// 上传文件
public void uploadFile() throws FileNotFoundException {
    InputStream inputStream = new FileInputStream("test.txt");
    String objectKey = "test-folder/test.txt";
    ossClientManager.get("default").upload(inputStream, objectKey, "text/plain");
}

// 使用默认客户端（第一个注册的客户端）
public void useDefaultClient() throws FileNotFoundException {
    OssClient client = ossClientManager.getDefault();
    InputStream inputStream = new FileInputStream("test.txt");
    client.upload(inputStream, "test.txt", "text/plain");
}

// 使用特定客户端
public void useSpecificClient() throws FileNotFoundException {
    OssClient backupClient = ossClientManager.get("backup");
    InputStream inputStream = new FileInputStream("backup.txt");
    backupClient.upload(inputStream, "backup/test.txt", "text/plain");
}

// 安全获取客户端（不存在返回 null）
public void safeGetClient() {
    OssClient client = ossClientManager.getOrNull("non-existent");
    if (client != null) {
        // 使用客户端
    }
}

// 检查客户端是否存在
public void checkClient() {
    if (ossClientManager.exists("backup")) {
        OssClient client = ossClientManager.get("backup");
        // 使用客户端
    }
}

// 获取所有客户端名称
public void listClients() {
    Set<String> clientNames = ossClientManager.getClientNames();
    log.info("可用的 OSS 客户端: {}", clientNames);
}
```

#### 文件下载

```java
// 下载文件
public void downloadFile() throws IOException {
    OssClient client = ossClientManager.get("default");
    try (FileOutputStream fos = new FileOutputStream("downloaded.txt")) {
        client.outStream("test-folder/test.txt", fos);
    }
}

// 获取文件对象
public void getFileObject() {
    OssClient client = ossClientManager.get("default");
    S3Object object = client.getObject("test-folder/test.txt");
    // 使用文件对象
}
```

#### 预签名 URL

```java
// 生成预签名URL（有效期1小时）
public String getPresignedUrl() {
    OssClient client = ossClientManager.get("default");
    return client.getPrivateUrl("test-folder/test.txt", 3600);
}
```

#### 分片上传

```java
// 分片上传大文件
public void uploadLargeFile() {
    OssClient client = ossClientManager.get("default");
    File largeFile = new File("large-file.zip");
    
    // 假设分为 3 个分片
    int totalParts = 3;
    for (int i = 1; i <= totalParts; i++) {
        client.uploadPart(largeFile, "application/zip", "large-file.zip", i, totalParts);
    }
}
```

**注意**：
- 分片上传需要 Redis 支持（如果 Redis 不可用，会自动使用内存实现，仅适用于单机环境）
- 分片上传状态会在 Redis 中保存 24 小时

## 注意事项

1. 配置前缀为"lambda.oss"
2. 必须配置bucket名称
3. 使用HTTPS时需要设置isHttps=true
4. 分片上传支持 Redis 和内存两种状态管理方式
   - Redis 可用时自动使用 Redis 实现（适用于分布式环境）
   - Redis 不可用时自动使用内存实现（仅适用于单机环境）
5. 分片上传建议用于大文件(>100MB)
6. 预签名URL需设置合理的过期时间（最长7天）
7. 多客户端配置时需区分name属性
8. 支持的OSS类型: MINIO, ALIYUN, QCLOUD, QINIU, OTHER
9. 支持的访问策略: private, public, custom
10. objectKey 不能以斜杠开头，不能包含连续斜杠
11. 配置会在启动时自动校验，不符合要求会抛出异常

## 异常处理

所有方法都会进行参数校验，如果参数无效会抛出 `IllegalArgumentException`。
OSS 操作失败会抛出 `OssException`，包含详细的错误信息和错误码。

```java
try {
    client.upload(inputStream, objectKey, contentType);
} catch (IllegalArgumentException e) {
    // 参数校验失败
    log.error("参数错误: {}", e.getMessage());
} catch (OssException e) {
    // OSS 操作失败
    log.error("OSS 操作失败: {}", e.getMessage(), e);
}
```
