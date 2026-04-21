# lambda-cloud-starter-oss

`lambda-cloud-starter-oss` 提供统一 OSS 接入能力，基于 AWS S3 SDK 适配 MinIO/阿里云 OSS/腾讯云 COS/七牛及其他 S3 兼容服务。

## 模块定位

- 统一对象存储客户端接入，屏蔽不同云厂商差异。
- 支持多客户端并存，按名称路由调用。
- 提供上传、下载、删除、预签名 URL、分片上传等通用能力。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
├─ OssAutoConfiguration.java
└─ OssProperties.java

src/main/java/com/lambda/cloud/oss/
├─ client/OssClient.java
├─ manager/OssClientManager.java
├─ service/OssService.java
├─ upload/
│  ├─ MultipartUploadStateManager.java
│  └─ impl/
│     ├─ RedisMultipartUploadStateManager.java
│     └─ InMemoryMultipartUploadStateManager.java
├─ policy/MinIOPolicyBuilder.java
├─ util/ValidationUtils.java
├─ enums/
│  ├─ OssType.java
│  ├─ AccessPolicyType.java
│  └─ PolicyType.java
└─ exception/OssException.java

src/main/resources/META-INF/spring/
└─ org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

自动装配注册项：

```text
com.lambda.autoconfig.OssAutoConfiguration
```

## 自动装配机制

### OssAutoConfiguration

核心行为：

- 绑定配置前缀：`lambda.oss`。
- 创建 `MultipartUploadStateManager`：
  - 存在 `RedisHelper` -> `RedisMultipartUploadStateManager`
  - 否则 -> `InMemoryMultipartUploadStateManager`
- 创建 `OssClientManager`，遍历 `lambda.oss.clients` 初始化每个 `OssClient`。
- 每个客户端初始化时会：
  - 注入分片状态管理器
  - 执行 `createBucket()`（仅 MINIO 生效）
  - 注册到 `OssClientManager`

## 配置模型

配置前缀：`lambda.oss`

### clients[]（必填）

每个客户端配置项：

- `name`：客户端名，正则 `^[a-zA-Z0-9_-]+$`
- `type`：`MINIO|ALIYUN|QCLOUD|QINIU|OTHER`
- `endpoint`：S3 兼容端点
- `accessKey` / `secretKey`
- `bucket`：3-63 位 DNS 风格小写命名
- `region`：可选
- `isHttps`：默认 `false`
- `accessPolicy`：默认 `private`，支持 `private|public|custom`
- `cdn`：可选，上传结果 URL 可走 CDN 域名
- `enablePathStyleAccess`：默认 `false`
- `httpClientConfig.*`：
  - `connectionTimeout` 默认 `10000`
  - `socketTimeout` 默认 `50000`
  - `maxConnections` 默认 `50`
  - `requestTimeout` 默认 `0`
  - `clientExecutionTimeout` 默认 `0`
  - `connectionTTL` 默认 `-1`
  - `connectionMaxIdleMillis` 默认 `60000`

## 配置示例

```yaml
lambda:
  oss:
    clients:
      - name: default
        type: MINIO
        endpoint: http://127.0.0.1:9000
        accessKey: your-access-key
        secretKey: your-secret-key
        bucket: demo-bucket
        isHttps: false
        accessPolicy: private
        enablePathStyleAccess: true
        httpClientConfig:
          connectionTimeout: 10000
          socketTimeout: 50000
          maxConnections: 50
          requestTimeout: 0
          clientExecutionTimeout: 0
          connectionTTL: -1
          connectionMaxIdleMillis: 60000
      - name: backup
        type: ALIYUN
        endpoint: http://oss-cn-hangzhou.aliyuncs.com
        accessKey: your-ak
        secretKey: your-sk
        bucket: backup-bucket
        isHttps: false
```

## 安全建议

- 不要在代码仓库中提交 `accessKey/secretKey` 等敏感信息，建议通过环境变量、密钥管理系统或配置中心加密能力注入。
- 生产环境建议使用最小权限的 AK/SK，并结合 `accessPolicy` 控制访问范围。

## 核心调用方式

### 客户端管理

```java
@Autowired
private OssClientManager ossClientManager;

OssClient client = ossClientManager.get("default");
OssClient defaultClient = ossClientManager.getDefault();
boolean exists = ossClientManager.exists("backup");
```

说明：

- `getDefault()` 固定读取名为 `"default"` 的客户端，不是“第一个客户端”。
- `get(name)` 不存在时会抛 `OssException`。

### 上传与下载

```java
UploadObjectResult result = client.upload(inputStream, "docs/a.txt", "text/plain");
client.outStream("docs/a.txt", outputStream);
client.delete("docs/a.txt");
String url = client.getPrivateUrl("docs/a.txt", 3600);
```

### 分片上传

```java
for (int part = 1; part <= totalParts; part++) {
    client.uploadPart(filePart, "application/zip", "archive/a.zip", part, totalParts);
}
```

说明：

- `partNumber` 必须从 `1` 开始，且 `partNumber <= partTotalNumber`。
- 最后一个分片会触发 `completeMultipartUpload`。
- 状态 key 规则：`objectKey + ":" + partTotalNumber`。

## 关键实现行为

- `OssClient` 统一通过 `AmazonS3ClientBuilder` 构建客户端。
- `MINIO` 或 `enablePathStyleAccess=true` 时启用 path-style 访问。
- `upload(InputStream, ...)` 对非 `ByteArrayInputStream` 会先读入内存再上传。
- `createBucket()` 仅在 `type=MINIO` 时执行。
- `accessPolicy` 解析大小写敏感，非法值会触发运行时异常。

## 异常与校验

- 参数错误：`IllegalArgumentException`
- OSS 调用错误：`OssException`（包装底层异常）
- 启动期配置错误：`@ConfigurationProperties + @Validated` 会直接阻断启动

`ValidationUtils` 额外约束：

- `objectKey` 不能以 `/` 开头，不能包含 `//`，长度 ≤ 1024
- `expirationSeconds` 范围 `1~604800`（最长 7 天）

## 依赖说明

关键依赖（见 `pom.xml`）：

- `com.amazonaws:aws-java-sdk-s3`
- `com.lambda.cloud:lambda-cloud-starter-redis`
- `com.lambda.cloud:lambda-cloud-core`

## 当前实现约束

- 本模块当前没有 `lambda.oss.enabled` 开关，配置存在即参与装配。
- `OssClientManager` 使用静态缓存，跨上下文测试需注意清理。
- 分片上传状态按 `objectKey:partTotal` 维度管理，同 key 并发上传需业务侧规避冲突。
- 分片上传每次 `UploadPartRequest` 直接使用 `file.length()` 作为分片大小，需确保传入的确是“分片文件”而非整文件。
- `upload(InputStream, ...)` 对大流会读入内存，超大文件建议使用分片上传。
