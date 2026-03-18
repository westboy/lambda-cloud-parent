# Lambda Cloud Processor

`lambda-cloud-processor` 是 Lambda Cloud 的编译期注解处理器模块，当前包含两类处理器：

- `AutoConverterProcessor`：为 `@AutoConverter` 生成 MapStruct Converter 接口。
- `PermissionProcessor`：在编译期扫描控制器权限注解并生成接口权限元数据 JSON。

## 模块定位

- 将对象转换规则前移到编译期，减少手写转换器成本。
- 将接口权限元数据前移到编译期，支持运行期按静态元数据进行鉴权或上报。

## 目录结构（src/main）

```text
src/main/java/com/lambda/cloud/processor/
├─ converter/
│  └─ AutoConverterProcessor.java
└─ permission/
   ├─ PermissionProcessor.java
   ├─ config/ProcessorConfig.java
   ├─ extractor/MetadataExtractor.java
   ├─ model/
   │  ├─ ApiPermissionMetadata.java
   │  └─ PermissionFileMetadata.java
   └─ scanner/AnnotationScanner.java

src/main/resources/META-INF/services/
└─ javax.annotation.processing.Processor
```

SPI 注册项：

```text
com.lambda.cloud.processor.permission.PermissionProcessor
com.lambda.cloud.processor.converter.AutoConverterProcessor
```

## 依赖与打包说明

### 依赖方式

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-processor</artifactId>
    <scope>provided</scope>
</dependency>
```

### 打包行为

- `pom.xml` 对 `resources` 做了 `META-INF/**/*` 排除。
- `maven-resources-plugin` 在 `prepare-package` 阶段执行 `copy-resources`，将资源复制到 `target/classes`。
- 目的是确保处理器 SPI 文件在最终构件中可用。

## AutoConverterProcessor

### 触发条件

- `@SupportedAnnotationTypes`：`com.lambda.cloud.core.annotation.AutoConverter`
- 仅处理 `ElementKind.CLASS`

### 生成目标

- 在源类同包生成 `{SourceSimpleName}Converter` 接口。
- 生成接口带 `@Mapper` 注解，固定配置：
  - `componentModel = "spring"`
  - `nullValuePropertyMappingStrategy = IGNORE`
  - `nullValueCheckStrategy = ALWAYS`
  - `unmappedTargetPolicy = IGNORE`
- `uses` 默认包含 `ConvertFunctions.class`，并追加 `@AutoConverter(uses=...)` 配置。
- `@AutoConverter(config=...)` 存在时透传到 `@Mapper(config=...)`。

### 继承接口规则

- `converter` 未指定时，默认继承 `BaseConverter`：
  - `isReverse=false`：`BaseConverter<Source, Target>`
  - `isReverse=true`：`BaseConverter<Target, Source>`
- `converter` 指定时，直接继承指定接口。

### 字段映射收集顺序

1. `@AutoConverter(fieldMappings = ...)`
2. 类级别 `@FieldMapping`
3. 类级别 `@FieldMappings`
4. 字段级别 `@FieldMapping` / `@FieldMappings`

收集到映射后，处理器会为 `convertTo` 生成显式方法并附加 `@Mapping` 注解。

### 支持透传的 FieldMapping 属性

- `target`
- `source`
- `ignore`
- `dateFormat`
- `numberFormat`
- `locale`
- `expression`
- `defaultExpression`
- `defaultValue`
- `qualifiedByName`
- `conditionExpression`
- `conditionQualifiedByName`
- `qualifiedBy`
- `conditionQualifiedBy`

## PermissionProcessor

### 生命周期

- `init`：
  - 初始化 `Filer`、`Messager`、`ObjectMapper`。
  - 读取编译参数到 `ProcessorConfig`。
  - 初始化 `MetadataExtractor`、`AnnotationScanner`。
  - 清空轮次收集容器 `collectedPermissions`。
- `process`：
  - 非结束轮：扫描并收集权限元数据。
  - 结束轮：统一写出 JSON。

### 扫描入口

- 控制器来源：
  - `@RestController`
  - `@Controller`
- 方法来源：
  - 当前类 + 父类方法。
  - 通过 `Elements#overrides` 去重覆盖关系。
- 方法入选条件：
  - 需存在路由注解：`@RequestMapping` 或 `@Get/Post/Put/Delete/PatchMapping`。

### 元数据提取行为（以当前实现为准）

- 处理器最终调用 `MetadataExtractor.extract(controller, method)`。
- `MetadataExtractor.extract` 的首个判定是：
  - 仅当类或方法存在 `@SaCheckPermission` 时才返回元数据。
  - 仅有 `@SaCheckRole` 或 `@SaCheckLogin` 的方法不会被输出。
- 路径规则：
  - 类路径与方法路径合并，统一为单斜杠路径。
  - 空路径默认 `/`。
- HTTP Method 规则：
  - 优先 HTTP 映射注解。
  - 其次 `@RequestMapping(method=...)`。
  - 默认 `GET`。
- 描述与分组：
  - 描述取 `@Operation(summary)`。
  - 分组取类上的 `@Tag(name)`。
- 废弃标记：
  - `@Deprecated` 或 `@Operation(deprecated=true)`。
- 权限提取规则：
  - 优先取类上的 `@SaCheckPermission`。
  - 若类上无权限注解，再取方法上的 `@SaCheckPermission`。
  - 当前实现不会合并“类 + 方法”权限。

### 输出文件

- 写入位置：`CLASS_OUTPUT + permission.output.path`
- 默认路径：`META-INF/permissions/api-permissions.json`
- 输出模型：`PermissionFileMetadata`
  - `version`（默认 `1.0.0`）
  - `generatedAt`
  - `module`
  - `basePackage`
  - `totalApis`
  - `apis`

### 编译参数

支持读取的编译参数：

```text
permission.enabled
permission.output.path
permission.output.format
permission.base.package
permission.module.name
permission.include.patterns
permission.exclude.patterns
```

当前实际生效：

- 已生效：
  - `permission.enabled`
  - `permission.output.path`
  - `permission.base.package`（写入输出）
  - `permission.module.name`（写入输出）
- 已读取但当前未参与过滤/分支：
  - `permission.output.format`
  - `permission.include.patterns`
  - `permission.exclude.patterns`

## Maven 配置示例

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>com.lambda.cloud</groupId>
                <artifactId>lambda-cloud-processor</artifactId>
                <version>${lambda.cloud.version}</version>
            </path>
        </annotationProcessorPaths>
        <compilerArgs>
            <arg>-Apermission.enabled=true</arg>
            <arg>-Apermission.output.path=META-INF/permissions/api-permissions.json</arg>
            <arg>-Apermission.module.name=${project.artifactId}</arg>
            <arg>-Apermission.base.package=com.lambda.fusion</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

## 当前实现边界

- `AutoConverterProcessor` 不根据 `BaseDTO/BaseVO` 自动推断方向，方向只由 `isReverse` 控制。
- `PermissionProcessor` 当前未使用 `basePackage/includePatterns/excludePatterns` 做扫描过滤。
- `ApiPermissionMetadata` 中 `tags`、`module` 字段当前流程未赋值。
