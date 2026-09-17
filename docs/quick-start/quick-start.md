# Lambda Cloud 快速开始

本指南用于完成 `lambda-cloud-parent` 的首次本地构建，并将 parent、BOM、core、processor 和各 starter 安装到 Maven 本地仓库，供下游项目依赖。

## 一、环境准备

| 依赖 | 版本 | 说明 |
|---|---|---|
| JDK | 21+ | 执行 `mvn -v` 确认 Maven 使用的 Java 版本 |
| Maven | 3.6+ | 本仓库没有 Maven Wrapper，使用系统 `mvn` |
| Maven settings | 仓库自带 [assets/maven/settings.xml](../../assets/maven/settings.xml) | **强烈推荐**：提供阿里云镜像 + 私服只读账号，首次构建可直接拉取已发布的 `lambda-cloud-processor`，免去下文「注解处理器自举」 |

## 二、首次构建

### 方式一：使用仓库自带 settings.xml（首次安装推荐）

仓库自带 [assets/maven/settings.xml](../../assets/maven/settings.xml)，已配置阿里云公共镜像与私服 `rdc-releases`/`rdc-snapshots` 的公开只读账号（`pub-user`，仅用于拉取依赖，不含发布权限），因此首次构建可一次通过，**无需改动任何 POM**：

```bash
mvn -s assets/maven/settings.xml clean install
```



> **温馨提示：** 也可以把该文件直接复制到 `~/.m2/settings.xml`（Windows：`%USERPROFILE%\.m2\settings.xml`），之后直接执行 `mvn clean install`。


### 方式二：直接构建安装

首次在本地构建安装，此时本地仓库里也没有 `lambda-cloud-core` / `lambda-cloud-processor`，直接 `mvn clean install` 会报找不到 `com.lambda.cloud:lambda-cloud-processor`的错误。

这是因为根 `pom.xml` 已在 `maven-compiler-plugin` 的 `annotationProcessorPaths` 中配置 `lambda-cloud-processor`，而该处理器自身又依赖 `lambda-cloud-core`。首次构建时需要先完成一次自举：

1. 打开仓库根目录的 `pom.xml`，在 `maven-compiler-plugin` → `annotationProcessorPaths` 中，临时注释 `lambda-cloud-processor` 对应的 `<path>`：

   ```xml
   <!--
   <path>
       <groupId>com.lambda.cloud</groupId>
       <artifactId>lambda-cloud-processor</artifactId>
       <version>${lambda-cloud.version}</version>
   </path>
   -->
   ```

2. 保持该配置处于注释状态，在仓库根目录执行：

   ```bash
   mvn -pl lambda-cloud-processor -am clean install
   ```

   `-am` 会先构建并安装 `lambda-cloud-processor` 所依赖的 `lambda-cloud-core`，然后安装 processor。

3. 恢复第一步注释的 `<path>`，再执行全量构建：

   ```bash
   mvn clean install
   ```

> 优先用方式一避免自举，临时注释只用于完成首次安装，不要提交该 POM 改动。

## 三、后续构建

首次构建成功后，后续直接在仓库根目录构建即可：

```bash
mvn clean install
```

常用命令：

```bash
mvn clean install -DskipTests           # 跳过测试构建
mvn -pl <module> -am clean install      # 构建指定模块及其依赖
mvn spotless:apply                      # 自动格式化
mvn test                                # 运行全部测试
mvn clean verify                        # 测试并生成 JaCoCo 覆盖率报告
```

构建成功后，下游项目即可引用 `com.lambda.cloud:lambda-cloud-parent:2026.1.1-SNAPSHOT` 和 `lambda-cloud-starter-dependencies` BOM。更多模块与能力说明见项目根目录的 [README](../../README.md)。