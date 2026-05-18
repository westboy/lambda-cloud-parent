---
name: "lambda-cloud-storage"
description: "Lambda Cloud 对象存储配置指南。当用户需要集成MinIO、阿里云OSS、腾讯云COS等对象存储服务时调用。"
---

# Lambda Cloud 对象存储

## 模块引入

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-oss</artifactId>
</dependency>
```

## 配置

配置前缀: `lambda.oss`

```yaml
lambda:
  oss:
    clients:
      - name: default
        type: MINIO
        endpoint: http://127.0.0.1:9000
        access-key: minioadmin
        secret-key: minioadmin
        bucket: my-bucket
        is-https: false
        access-policy: private
        enable-path-style-access: true
        cdn: https://cdn.your-domain.com
        http-client-config:
          connection-timeout: 10000
          socket-timeout: 50000
          max-connections: 50
      - name: backup
        type: ALIYUN
        endpoint: http://oss-cn-hangzhou.aliyuncs.com
        access-key: your-ak
        secret-key: your-sk
        bucket: backup-bucket
        region: cn-hangzhou
```

### 配置项说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `name` | 客户端名 | 必填 |
| `type` | 存储类型 | `MINIO\|ALIYUN\|QCLOUD\|QINIU\|OTHER` |
| `endpoint` | 端点地址 | 必填 |
| `access-key` | 访问密钥 | 必填 |
| `secret-key` | 秘密密钥 | 必填 |
| `bucket` | 存储桶名 | 必填 |
| `region` | 区域 | 可选 |
| `is-https` | 是否 HTTPS | `false` |
| `access-policy` | 访问策略 | `private` |
| `enable-path-style-access` | 路径风格访问 | `false` |
| `cdn` | CDN 域名 | 可选 |
| `http-client-config.connection-timeout` | 连接超时(ms) | `10000` |
| `http-client-config.socket-timeout` | Socket超时(ms) | `50000` |
| `http-client-config.max-connections` | 最大连接数 | `50` |
| `http-client-config.request-timeout` | 请求超时(ms) | `0` |
| `http-client-config.client-execution-timeout` | 客户端执行超时(ms) | `0` |
| `http-client-config.connection-ttl` | 连接TTL(ms) | `-1` |
| `http-client-config.connection-max-idle-millis` | 连接最大空闲时间(ms) | `60000` |

## 使用方式

### OssClientManager 方法列表

| 方法 | 说明 |
|------|------|
| `getDefault()` | 获取默认客户端 (name="default") |
| `get(String)` | 获取指定客户端 |
| `getOrNull(String)` | 安全获取客户端 (不存在返回 null) |
| `register(String, OssClient)` | 注册客户端 |
| `remove(String)` | 移除客户端 |
| `clear()` | 清空所有客户端 |
| `exists(String)` | 检查客户端是否存在 |
| `getClientNames()` | 获取所有客户端名称 |
| `size()` | 获取客户端数量 |
| `isEmpty()` | 检查是否为空 |

### 获取客户端

```java
@Service
public class FileService {

    @Autowired
    private OssClientManager ossClientManager;

    public void uploadFile() {
        // 获取默认客户端 (name="default")
        OssClient defaultClient = ossClientManager.getDefault();

        // 获取指定客户端
        OssClient backupClient = ossClientManager.get("backup");

        // 判断客户端是否存在
        boolean exists = ossClientManager.exists("backup");
    }
}
```

### 上传文件

```java
public UploadObjectResult upload(MultipartFile file) throws IOException {
    OssClient client = ossClientManager.getDefault();

    // 上传
    UploadObjectResult result = client.upload(
        file.getInputStream(),
        "docs/" + file.getOriginalFilename(),
        file.getContentType()
    );

    // 结果
    String url = result.getUrl();        // 访问 URL
    String key = result.getKey();        // 对象键
    return result;
}
```

### 下载文件

```java
public void download(String objectName, HttpServletResponse response) throws IOException {
    OssClient client = ossClientManager.getDefault();

    response.setContentType("application/octet-stream");
    response.setHeader("Content-Disposition", "attachment; filename=" + objectName);

    client.outStream(objectName, response.getOutputStream());
}
```

### 删除文件

```java
public void delete(String objectName) {
    OssClient client = ossClientManager.getDefault();
    client.delete(objectName);
}
```

### 预签名 URL

```java
public String getPresignedUrl(String objectName) {
    OssClient client = ossClientManager.getDefault();

    // 生成预签名 URL (有效期 3600 秒)
    String url = client.getPrivateUrl(objectName, 3600);
    return url;
}
```

## 分片上传

支持大文件分片上传, 状态管理自动选择:
- 存在 `RedisHelper` → `RedisMultipartUploadStateManager`
- 否则 → `InMemoryMultipartUploadStateManager`

```java
public void multipartUpload(InputStream inputStream, String objectName) {
    OssClient client = ossClientManager.getDefault();

    // 分片上传 (自动管理状态)
    UploadObjectResult result = client.multipartUpload(inputStream, objectName);
}
```

## MinIO 策略构建

```java
@Bean
public MinIOPolicyBuilder minIOPolicyBuilder() {
    return new MinIOPolicyBuilder();
}
```

## 完整示例

```java
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private OssClientManager ossClientManager;

    @PostMapping("/upload")
    public UploadObjectResult upload(@RequestParam("file") MultipartFile file) throws IOException {
        OssClient client = ossClientManager.getDefault();

        String objectName = "uploads/" + UUID.randomUUID() + "/" + file.getOriginalFilename();
        return client.upload(file.getInputStream(), objectName, file.getContentType());
    }

    @GetMapping("/download/{objectName}")
    public void download(@PathVariable String objectName, HttpServletResponse response) throws IOException {
        OssClient client = ossClientManager.getDefault();
        client.outStream(objectName, response.getOutputStream());
    }

    @DeleteMapping("/{objectName}")
    public void delete(@PathVariable String objectName) {
        OssClient client = ossClientManager.getDefault();
        client.delete(objectName);
    }

    @GetMapping("/url/{objectName}")
    public String getUrl(@PathVariable String objectName) {
        OssClient client = ossClientManager.getDefault();
        return client.getPrivateUrl(objectName, 3600);
    }
}
```

## 最佳实践

1. **敏感信息**: 不要硬编码 access-key/secret-key, 使用环境变量或配置中心
2. **存储桶命名**: 3-63 位 DNS 风格小写命名
3. **CDN 加速**: 配置 `cdn` 字段使用 CDN 域名访问
4. **多客户端**: 按业务场景使用不同客户端 (如 default=主存储, backup=备份)
5. **文件命名**: 使用 UUID 或业务规则生成唯一文件名, 避免冲突
6. **访问策略**: 生产环境使用 `private`, 通过预签名 URL 访问
