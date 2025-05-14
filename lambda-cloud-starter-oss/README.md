### lambda-cloud-starter-oss 项目介绍及使用说明

#### 简介
`lambda-cloud-starter-oss` 是一个对象存储服务（OSS）模块，主要用于在微服务架构中实现文件的存储和访问。它包含了OSS的自动配置、文件上传、文件下载等功能，以简化开发者在项目中对对象存储的管理。

#### 主要功能
1. **OSS自动配置**：模块中包含了Spring Boot的自动配置功能，可以自动读取配置文件中的OSS信息并进行配置。
2. **文件上传**：支持将文件上传到OSS，可以将本地文件或输入流上传到指定的存储桶。
3. **文件下载**：提供文件下载功能，可以从OSS下载文件到本地或输出流。

#### 项目依赖
该项目依赖于多个库来实现其功能，包括：
- `aliyun-java-sdk-oss`：提供阿里云OSS的基本功能。
- `minio`：提供MinIO的OSS功能，支持本地模式和集群模式。

#### 使用方式
要在你的项目中使用 `lambda-cloud-starter-oss`，你需要在项目的pom文件中添加以下依赖：
```xml
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>lambda-cloud-starter-oss</artifactId>
    <version>${project.parent.version}</version>
</dependency>
```

其中`${project.groupId}`和`${project.parent.version}`应替换为实际的项目信息。

#### 配置示例
在项目的配置文件（如application.yaml）中，可以配置OSS信息：
```yaml
oss:
  endpoint: http://localhost:9000
  access-key: your_access_key
  secret-key: your_secret_key
  bucket-name: your_bucket_name
```

通过上述配置和依赖添加，你可以在项目中使用`lambda-cloud-starter-oss`提供的对象存储功能，简化文件存储和访问的配置和使用。
