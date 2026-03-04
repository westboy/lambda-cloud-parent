# Lambda Cloud Processor

`lambda-cloud-processor` 是编译期注解处理器模块，当前 `src/main` 内实际包含 2 个处理器：

- `AutoConverterProcessor`：为 `@AutoConverter` 生成 MapStruct Converter 接口
- `PermissionProcessor`：扫描 Controller 与权限注解，生成接口权限 JSON

## 模块结构（基于 src/main）

```text
src/main/java/com/lambda/cloud/processor/
├─ converter/AutoConverterProcessor.java
└─ permission/
   ├─ PermissionProcessor.java
   ├─ config/ProcessorConfig.java
   ├─ extractor/MetadataExtractor.java
   ├─ model/
   │  ├─ ApiPermissionMetadata.java
   │  └─ PermissionFileMetadata.java
   └─ scanner/AnnotationScanner.java
src/main/resources/META-INF/services/javax.annotation.processing.Processor
```

SPI 注册文件当前包含：

```text
com.lambda.cloud.processor.permission.PermissionProcessor
com.lambda.cloud.processor.converter.AutoConverterProcessor
```

## 依赖

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-processor</artifactId>
    <scope>provided</scope>
</dependency>
```

## AutoConverterProcessor 行为说明

### 触发条件

- 仅处理 `@AutoConverter` 标注的 `class`
- 注解类型：`com.lambda.cloud.core.annotation.AutoConverter`

### 生成接口基础规则

- 接口名：`{源类名}Converter`
- 包名：与源类同包
- 固定添加 `@Mapper` 属性：
  - `componentModel = "spring"`
  - `nullValuePropertyMappingStrategy = IGNORE`
  - `nullValueCheckStrategy = ALWAYS`
  - `unmappedTargetPolicy = IGNORE`
- `uses` 总会包含 `com.lambda.cloud.core.convert.ConvertFunctions.class`
- 如果 `@AutoConverter(config = X.class)` 存在，则写入 `@Mapper(config = X.class)`

### 继承接口规则（以当前处理器实现为准）

- `converter` 未指定（默认 `Void.class`）时：
  - `isReverse = false`：`BaseConverter<Source, Target>`，`convertTo(Source source) -> Target`
  - `isReverse = true`：`BaseConverter<Target, Source>`，`convertTo(Target source) -> Source`
- `converter` 指定时：
  - 直接继承 `converter` 指定接口

### 字段映射来源

处理器会按以下来源收集 `FieldMapping`，并全部转换为 `@Mapping`：

1. `@AutoConverter(fieldMappings = {...})`
2. 类上的 `@FieldMapping`
3. 类上的 `@FieldMappings`
4. 字段上的 `@FieldMapping` / `@FieldMappings`

### 何时生成显式 `convertTo` 方法

- 仅当收集到至少一个字段映射时，处理器会在生成接口中显式声明 `convertTo(...)` 并附加全部 `@Mapping`
- 未收集到字段映射时，不额外声明 `convertTo(...)`，由父接口抽象方法与 MapStruct 处理

### FieldMapping 支持写入的属性

`target`、`source`、`ignore`、`dateFormat`、`numberFormat`、`locale`、`expression`、`defaultExpression`、`defaultValue`、`qualifiedByName`、`conditionExpression`、`conditionQualifiedByName`、`qualifiedBy`、`conditionQualifiedBy`

## PermissionProcessor 行为说明

### 生命周期与轮次

- `init` 阶段：
  - 初始化 `Filer`、`Messager`、`ObjectMapper`
  - 读取编译参数到 `ProcessorConfig`
  - 初始化 `MetadataExtractor`、`AnnotationScanner`
  - 清空 `collectedPermissions`
- `process` 阶段：
  - 非结束轮：扫描 Controller 并收集接口元数据
  - 结束轮：统一写出 JSON 文件，然后清空收集列表

### 扫描范围与方法选择

- Controller 类来源：
  - `@RestController`
  - `@Controller`
- 方法入选条件：
  - 方法存在 Spring 路由注解之一：`@RequestMapping` / `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` / `@PatchMapping`
- 方法遍历包含父类方法，并通过 `Elements#overrides` 去重

### 元数据提取规则

- 路径：类路径 + 方法路径，使用 `/` 规范化并去重斜杠
- HTTP Method：
  - 优先 `@GetMapping/@PostMapping/...`
  - 否则取 `@RequestMapping(method=...)` 第一个值
  - 都没有时默认 `"GET"`
- 权限：类 + 方法上的 `@SaCheckPermission` 合并去重
- 角色：类 + 方法上的 `@SaCheckRole` 合并去重
- 权限逻辑优先级：
  1. 方法 `@SaCheckRole.mode`
  2. 方法 `@SaCheckPermission.mode`
  3. 类 `@SaCheckRole.mode`
  4. 类 `@SaCheckPermission.mode`
  5. 默认 `"AND"`
- 认证要求：
  - 有 `@SaCheckLogin` 则为 `true`
  - 否则只要存在权限或角色约束也为 `true`
  - 否则为 `false`
- 描述：`@Operation(summary)`
- 分组：`@Tag(name)`
- 废弃：
  - `@Deprecated`，或
  - `@Operation(deprecated = true)`

### 输出文件

- 输出位置：`CLASS_OUTPUT` + `permission.output.path`
- 默认路径：`META-INF/permissions/api-permissions.json`
- JSON 根对象字段：
  - `version`（固定写入 `1.0.0`）
  - `generatedAt`（`Instant.now().toString()`）
  - `module`（`permission.module.name`）
  - `basePackage`（`permission.base.package`）
  - `totalApis`
  - `apis`

## 编译参数（代码已读取）

```text
permission.enabled
permission.output.path
permission.output.format
permission.base.package
permission.module.name
permission.include.patterns
permission.exclude.patterns
```

其中当前 `src/main` 代码中的实际使用状态如下：

- 已直接参与流程：
  - `permission.enabled`
  - `permission.output.path`
  - `permission.base.package`（写入输出 JSON）
  - `permission.module.name`（写入输出 JSON）
- 已读取但当前未用于扫描/输出分支控制：
  - `permission.output.format`
  - `permission.include.patterns`
  - `permission.exclude.patterns`

## Maven 编译参数示例

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

## 当前代码事实边界

- `AutoConverterProcessor` 当前没有基于 `BaseDTO/BaseVO` 继承关系自动反转泛型，反转仅由 `isReverse` 控制
- `PermissionProcessor` 当前没有使用 `include/exclude/basePackage` 做扫描过滤
- `ApiPermissionMetadata` 中的 `tags`、`module` 字段在当前提取流程中未赋值
