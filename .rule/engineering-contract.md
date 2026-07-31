# Lambda Cloud 工程契约（Engineering Contract）

> 本文件定义 Lambda Cloud 基础框架仓库的可执行工程条款。每条以 **MUST**（强制）/ **MUST NOT**（禁止）/ **SHOULD**（推荐）标注等级，等级定义见 [project-charter.md](project-charter.md) §3.1。
> 冲突时：禁止条款 > 强制条款 > 宪章解释 > `AGENTS.md`（仅入口）。

## 1. 依赖与版本治理契约

### 1.1 Parent / BOM 统一版本（MUST）

- 本仓 `lambda-cloud-parent` 自身即 parent POM（`com.lambda.cloud:lambda-cloud-parent:2026.1.1-SNAPSHOT`，`packaging=pom`），供下游继承。
- 依赖版本由根 `pom.xml` 的 properties 统一声明，经 `lambda-cloud-starter-dependencies`（BOM）对外管理。技术栈：Spring Boot 4.1.0、Spring Cloud 2025.1.2、Spring Cloud Alibaba 2025.1.0.0、JDK 21。
- 模块 POM 仅声明 `groupId`/`artifactId`，不声明 `version`（由 BOM 统一）。

### 1.2 硬编码版本禁令（MUST NOT）

- 不得在 starter 模块 POM 中硬编码任何依赖 `<version>`。
- 不得引入未经 BOM 管理的第三方依赖版本。

### 1.3 SNAPSHOT 构建前置（MUST）

- 本仓 parent 与 BOM 均为 `2026.1.1-SNAPSHOT`，下游（如 `lambda-fusion-*`）依赖前必须先把本仓安装到本地仓库：`mvn clean install`，否则 SNAPSHOT 解析失败。

## 2. 模块独立性契约

### 2.1 独立可用（MUST）

- 每个 `lambda-cloud-starter-*` 应可被独立引入、最小依赖，避免 starter 间强耦合。
- 可选依赖（`optional=true`）是刻意的，缺失时必须**优雅降级**。

### 2.2 改强依赖禁令（MUST NOT）

- 在未核对条件装配逻辑前，不得把可选依赖改成强依赖。
- 不得引入会导致 starter 在缺失某依赖时启动失败的强依赖。

## 3. `lambda-cloud-core` 能力提供契约

### 3.1 基础模型提供（MUST）

`lambda-cloud-core` 是基础库，提供以下能力，下游与本仓 starter 必须复用，不得重复实现：

- **基类**：`BaseDO`（含审计字段 `createUser`/`createTime`/`updateUser`/`updateTime`）、`BaseDTO<T>`（含 `toEntity()`）、`BaseVO<T>`（含 `fromEntity()`）、`BasePageDTO`、`BaseEnum`。
- **转换**：`@AutoConverter` + `ConverterResolver` + `ConvertFunctions`（见 §4）。
- **工具**：`Constants`（全局常量：认证、日期、JSON）、`HmacGenerator`（HMAC-SHA256）、`TypeConverter`/`ClassTypeUtils`（安全类型转换）、`Assert`（流式校验）。
- **异常**：`ErrorModel`/`ErrorCode` 标准化错误响应、Feign 异常族（`FeignAccessDeniedException` 等）。

### 3.2 复用禁令（MUST NOT）

- 不得重复实现基类、转换器、常量、工具、异常体系。
- 不得在 starter 中自造与 core 重复的能力。

## 4. 对象转换契约

### 4.1 自动转换（MUST）

- 使用 `@AutoConverter(target = XxxEntity.class)` 标注 DTO（`isReverse=false`，DTO -> Entity）与 VO（`isReverse=true`，Entity -> VO）。
- `AutoConverterProcessor`（`lambda-cloud-processor`）在编译期生成 MapStruct 接口；运行时经 `ConverterResolver` 定位、`ConvertFunctions` 提供公共类型转换（JSON、日期、数字）。

### 4.2 转换禁令（MUST NOT）

- 不得手写实体↔DTO↔VO 转换方法（与 `@AutoConverter`/`ConverterResolver` 重复）。
- 不得绕过编译期处理器手工维护 MapStruct 接口。

## 5. 注解处理器契约

### 5.1 处理器装配（MUST）

`lambda-cloud-processor` 提供两个编译期处理器：

- `AutoConverterProcessor`：扫描 `@AutoConverter` 生成 MapStruct 转换接口（见 §4）。
- `PermissionProcessor`：扫描权限注解生成 API 权限元数据（`ApiPermissionMetadata` / `PermissionFileMetadata`），供权限 starter 消费。

### 5.2 重编译要求（MUST）

- 修改 `@AutoConverter`、`@FieldMapping`、协议注解或权限注解后，必须执行 `mvn clean compile` 以重新生成产物。

### 5.3 禁令（MUST NOT）

- 不得手写或手工修改处理器生成的产物。
- 不得在业务模块自行引入与父 POM 冲突的注解处理器版本。

## 6. starter 自动配置规范契约

### 6.1 三件套结构（MUST）

每个 starter 遵循 Spring Boot 3.x+ 自动配置：

1. 自动配置入口统一位于 `com.lambda.autoconfig` 包，标注 `@AutoConfiguration`，注册在 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`（每行一个类，**文件末尾无空行**）。
2. 条件 Bean 装配放在自动配置类中；业务实现放在 `com.lambda.cloud.<domain>` 或 `com.lambda.security` 等业务包。
3. 配置属性类标注 `@ConfigurationProperties`，前缀见 §7。

### 6.2 条件装配（MUST）

- 功能启停用 `@ConditionalOnProperty`，并提供 `matchIfMissing` 默认值以向后兼容。
- Bean 覆盖用 `@ConditionalOnMissingBean`；依赖存在性用 `@ConditionalOnClass`；Web 类型用 `@ConditionalOnWebApplication`。
- 扩展点优先定义为可覆盖接口（如 `EntityMetaFiller`、`TenantHandler`、`HmacClientService`、`ServerBootstrapConfigurationCustomizer`、`ChannelPipelineConfigurationCustomizer`）。

### 6.3 自动配置禁令（MUST NOT）

- 不得在业务域包内放置 `@AutoConfiguration` 入口（必须统一在 `com.lambda.autoconfig`）。
- 不得遗漏 `AutoConfiguration.imports` 注册或末尾留空行。
- 不得添加无条件 Bean（应经条件装配）。

## 7. 配置属性命名契约

### 7.1 命名规则（MUST）

- 配置属性使用 **kebab-case**（如 `lambda.security.form.enabled`）。
- 前缀统一以 `lambda.` 或 `mybatis-plus.` 起头。

### 7.2 starter 前缀表（MUST）

| starter | 前缀 |
|---|---|
| security | `lambda.security.*`（`form.enabled`/`verify.sms.enabled`/`hmac.enabled`/`third-party.*.enabled`） |
| oss | `lambda.oss.*` |
| liquibase | `lambda.liquibase.*` |
| netty | `lambda.netty.*` |
| cache | `lambda.cache.*` |
| sse | `lambda.sse.*` |
| mybatis | `mybatis-plus.*`（`tenant.enabled`/`encrypt.enabled`/`encrypt.key`） |

### 7.3 禁令（MUST NOT）

- 不得散落使用 `@Value` 注入本应由 `@ConfigurationProperties` 管理的配置。
- 不得使用非 kebab-case 的属性名。

## 8. MyBatis 增强提供方契约

### 8.1 SQL 注入器（MUST）

`lambda-cloud-starter-mybatis` 的 `LambdaSqlInjector` 为所有 mapper 注入：`insertAll()`（批量插入）、`selectByCode()`/`updateByCode()`/`deleteByCode()`（基于 `@TableCodeField` 字段的 CRUD）、`exists()`（存在性检查）。Mapper 继承 `LambdaBaseMapper`。

### 8.2 字段加密（MUST）

- 字段级透明加密用 `AesEncryptHandler`：`@TableField(typeHandler = AesEncryptHandler.class)`。
- 开关：`mybatis-plus.encrypt.enabled`，密钥：`mybatis-plus.encrypt.key`。

### 8.3 多租户（MUST）

- `TenantLineInnerInterceptor` 自动按租户字段过滤；`TenantHandler` 提供租户 ID。
- 开关：`mybatis-plus.tenant.enabled`；与加密兼容。

### 8.4 自动填充与拦截器顺序（MUST）

- `GlobalMetaObjectHandler` 经可插拔 `EntityMetaFiller` 自动填充审计字段。
- 拦截器链顺序：Order 9 租户 handler -> Order 10 租户 line interceptor（TenantLineInnerInterceptor）-> Order 20 分页（PaginationInnerInterceptor）-> Order Short.MAX_VALUE 租户表达式拦截器，不得随意打乱。

### 8.5 多数据库（SHOULD）

- `DatabaseIdProvider` 检测 MySQL/Oracle/PostgreSQL/H2/DM，启用 mapper 中数据库特定 SQL。

## 9. 安全认证提供方契约

### 9.1 多登录类型（MUST）

`lambda-cloud-starter-security` 基于 Sa-Token，支持多登录类型（`loginUser` 标准用户、`hmac` API 签名、自定义），经 `StpLogicUtils` 支持。

### 9.2 认证策略条件装配（MUST）

四种策略均条件装配，互不依赖：

1. 表单登录（`lambda.security.form.enabled`）：`FormAuthenticationProcessingFilter`、`FormLockingStrategy`（Redis 防爆破）、`StandardPasswordEncoder`、`FormLogoutFilter`。
2. 短信/验证码（`lambda.security.verify.enabled`）：`CaptchaVerifyCodeGenerateImpl`、`RedisCaptchaStore`、`SmsVerifyCodeGenerateImpl`（限流）、`RedisSmsVerifyCodeStore`。
3. HMAC（`lambda.security.hmac.enabled`）：`HmacAuthenticationProcessingFilter`、`HmacClientService`（AppId+Timestamp+Signature，时间戳防重放）。
4. 第三方登录（`lambda.security.third-party.*.enabled`）：如 `WxMaLoginProvider`，可扩展。

### 9.3 防护与授权（MUST）

- XSS：`XSSDefendFilter`（OWASP ESAPI，可配信任域白名单）。
- 授权：方法级 `@RequiresAuth`/`@RequiresPermission`/`@RequiresRole`；同源校验 `SaSameUtil`（CSRF）。

### 9.4 禁令（MUST NOT）

- 不得绕过 `StpLogicUtils` 自行解析 Sa-Token 上下文。
- 不得在业务层重复实现认证过滤器/编码器。

## 10. Netty 协议引擎提供方契约

### 10.1 注解驱动协议定义（MUST）

`lambda-cloud-starter-netty` 用注解定义二进制协议：

- `@ProtocolPayload(frameType=, crcAlgorithm=)` 标注消息类。
- `@ProtocolField(order=, length=, dataType=, ...)` 标注字段，支持 UINT8/16/32/64、HEX/ASCII/BCD、LIST、CP56Time2a、加密字段、组合字段。

### 10.2 引擎与访问优化（MUST）

- `ProtocolEngine<T>` 提供 `parse`/`serialize`/`validate`。
- 字段访问优先 `ByteCodeFieldAccessor`（ASM 字节码，零反射），回退 `ReflectionFieldAccessor`，由 `FieldAccessorFactory` 选择。
- CRC：`CrcAlgorithm`（`Crc16Algorithm` Modbus 等），序列化自动计算、解析时校验。

### 10.3 服务与定制（MUST）

- `NettyServer` 实现 `SmartLifecycle`，支持 EPOLL（Linux）/NIO。
- 扩展点：`ServerBootstrapConfigurationCustomizer`、`ChannelPipelineConfigurationCustomizer`、`NettyChannelInitializer`。

### 10.4 禁令（MUST NOT）

- 不得在业务层重复实现二进制编解码（须复用 `ProtocolEngine`）。
- 不得绕过 `FieldAccessorFactory` 手写字段访问。

## 11. 协议业务 starter 契约

### 11.1 复用引擎（MUST）

`lambda-cloud-starter-ocpp`、`lambda-cloud-starter-t645`、`lambda-cloud-starter-ykc` 等协议业务 starter 必须基于 `lambda-cloud-starter-netty` 的 `ProtocolEngine` 实现编解码，不得重复造协议引擎。

### 11.2 测试要求（MUST）

- 协议编解码改动必须跑对应模块测试：`mvn -pl lambda-cloud-starter-<proto> test`（ykc/netty/t645 均有测试）。

## 12. 跨服务能力契约（Dubbo / Feign / Nacos）

### 12.1 条件装配（MUST）

- `lambda-cloud-starter-dubbo`/`-feign`/`-nacos` 的所有自动配置均以 `@ConditionalOnClass` 判断对应依赖是否在 classpath。
- 缺失依赖时必须优雅降级，不得启动失败。

### 12.2 禁令（MUST NOT）

- 不得在缺少对应依赖的环境下强装配跨服务 Bean。

## 13. 数据库迁移（Liquibase）提供方契约

### 13.1 聚合机制（MUST）

`lambda-cloud-starter-liquibase` 自动聚合 classpath 中所有 `META-INF/db/changelogs/lambda-*-changelog.xml`（正则 `lambda-\w*-changelog.xml`），经 master changelog 执行。

- **`lambda-datasource-changelog.xml` 被强制最先执行**，`lambda-additional-changelog.xml` 最后执行。
- 命名必须严格匹配该模式，否则不会被加载。

### 13.2 禁令（MUST NOT）

- 不得使用不匹配 `lambda-\w*-changelog.xml` 的命名。
- 带连字符的子 changelog 须由父 changelog `<include>` 引入（filter 用全匹配，不含连字符）。

## 14. 静态检查契约（Spotless + SpotBugs）

### 14.1 已绑定的检查（MUST）

父 POM 在 **`compile`** 阶段强制执行两道关卡，`mvn compile` 即会在违规时失败：

- **Spotless - Palantir Java Format**（v2.67.0，`PALANTIR` 风格，`formatJavadoc=false`）。自动修复：`mvn spotless:apply`；仅检查：`mvn spotless:check`。
- **SpotBugs**（4.10.3.0，compile 阶段 `check`，使用 `${project.basedir}/spotbugs-exclude.xml`）。

### 14.2 完成标准（MUST）

- 所有 Java 代码必须已符合 Palantir 规范，否则构建失败。
- 提交前 `mvn compile`（含 Spotless + SpotBugs）必须通过。

### 14.3 禁令（MUST NOT）

- 不得绕过 Spotless/SpotBugs 提交（禁止 `--no-verify` 类绕过）。
- 不得手动编辑由 antrun 重新生成的 `lombok.config`（`addLombokGeneratedAnnotation=true`，排除覆盖率）。

## 15. 测试契约

### 15.1 运行约定（MUST）

- 测试运行在 classpath 上而非 module path（父 POM 设置 `surefire.useModulePath=false`）；遇到 JPMS 相关测试失败时据此排查。
- 改动协议引擎/编解码逻辑时，必须执行对应模块测试：`mvn -pl lambda-cloud-starter-netty test`、`mvn -pl lambda-cloud-starter-ykc test`、`mvn -pl lambda-cloud-starter-t645 test`、`mvn -pl lambda-cloud-starter-iotdb test`。

### 15.2 覆盖率（SHOULD）

- 覆盖率报告：`mvn clean verify`，打开 `target/site/jacoco/index.html`。
- 鼓励为 core/security/mybatis 补充测试。

### 15.3 禁令（MUST NOT）

- 不得提交无断言或仅“实现陪跑”的无效测试。
- 不得用反射访问 private 成员维持脆弱测试。

## 16. 安全红线契约

### 16.1 安全禁令（MUST NOT）

- 不得硬编码依赖版本、密钥、Token。
- 不得提交含真实凭据的配置文件。
- 不得引入绕过鉴权、绕过多租户隔离、绕过字段加密的实现。
- 不得在日志中输出敏感信息（密钥、Token、完整凭据）。

## 17. Git 代码提交规范契约

### 17.1 提交信息格式（MUST）

使用 [Conventional Commits](https://www.conventionalcommits.org/) 风格 `<type>(<scope>): <描述>`：

- **type**：`feat`、`fix`、`refactor`、`style`、`docs`、`chore`、`test`、`perf`。
- **scope**（可选）：受影响模块，如 `core`、`mybatis`、`security`、`netty`、`liquibase`，或完整模块名 `lambda-cloud-starter-xxx`。
- **描述**：中英文均可，祈使语气、简明扼要。

### 17.2 提交粒度与检查（MUST）

- 原子提交；规则文件变更独立提交，不与业务代码混。
- 提交前 `mvn compile`（或对应模块 `mvn verify`）通过。

### 17.3 禁令（MUST NOT）

- 禁止不带描述的裸 `fix`/`refactor` 提交。
- 禁止 `--no-verify` 绕过检查、禁止 force push 主分支。

## 18. 代码组织与命名契约

### 18.1 包结构（MUST）

- 自动配置入口统一在 `com.lambda.autoconfig`（业务域包不得放 `@AutoConfiguration`）。
- 基础库 `com.lambda.cloud.core.*`；业务域 `com.lambda.cloud.<domain>`（mybatis/netty/iotdb 等）；安全域 `com.lambda.security.*`；注解处理器 `com.lambda.cloud.processor.*`。详见 [package-structure.md](package-structure.md)。

### 18.2 命名规则（MUST）

- 自动配置 `*AutoConfiguration`；属性 `*Properties`；实体 `*DO`（继承 `BaseDO`）；DTO `*DTO`（继承 `BaseDTO`）；VO `*VO`（继承 `BaseVO`）；Mapper 继承 `LambdaBaseMapper`。

### 18.3 禁令（MUST NOT）

- 不得在 `com.lambda.autoconfig` 之外放自动配置入口。
- 不得自造与 core 基类重复的模型体系。
