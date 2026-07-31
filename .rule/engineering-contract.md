# Lambda Cloud 工程契约（Engineering Contract）

> 本文件定义 Lambda Cloud 基础框架**仓内维护者**必须遵守的可执行条款。
> 本仓是能力提供方：产出 parent POM、BOM、`lambda-cloud-starter-*` 与 `lambda-cloud-core`/`lambda-cloud-processor`，供下游（如 `lambda-fusion-*`）依赖。
> 条款聚焦“在**本仓内**开发/维护框架时必须怎么做”；**下游如何使用这些能力**见 `README.md`「架构与核心能力」，下游仓另有其工程契约。
> 每条以 **MUST**（强制）/ **MUST NOT**（禁止）/ **SHOULD**（推荐）标注，等级定义见 [project-charter.md](project-charter.md) §3.1。
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

- 本仓 parent 与 BOM 均为 `2026.1.1-SNAPSHOT`，下游依赖前必须先把本仓安装到本地仓库：`mvn clean install`。

### 1.4 版本升级（SHOULD）

- 升级依赖版本须在根 `pom.xml` properties 统一修改，并在 BOM 同步；不得在子模块单独覆盖版本。

## 2. 模块独立性契约

### 2.1 独立可用（MUST）

- 每个 `lambda-cloud-starter-*` 应可被独立引入、最小依赖，避免 starter 间强耦合。
- 可选依赖（`optional=true`）是刻意的，缺失时必须**优雅降级**。

### 2.2 改强依赖禁令（MUST NOT）

- 在未核对条件装配逻辑前，不得把可选依赖改成强依赖。
- 不得引入会导致 starter 在缺失某依赖时启动失败的强依赖。

## 3. starter 自动配置规范契约

> 本节是本仓最核心的规则：每个 starter 都是一个自动配置单元。

### 3.1 三件套结构（MUST）

1. 自动配置入口统一位于 `com.lambda.autoconfig` 包，标注 `@AutoConfiguration`，注册在 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`（每行一个类，**文件末尾无空行**）。
2. 条件 Bean 装配放在自动配置类中；业务实现放在 `com.lambda.cloud.<domain>` 或 `com.lambda.security` 等业务包。
3. 配置属性类标注 `@ConfigurationProperties`，前缀见 §5。

### 3.2 条件装配（MUST）

- 功能启停用 `@ConditionalOnProperty`，并提供 `matchIfMissing` 默认值以向后兼容。
- Bean 覆盖用 `@ConditionalOnMissingBean`；依赖存在性用 `@ConditionalOnClass`；Web 类型用 `@ConditionalOnWebApplication`。
- 扩展点优先定义为可覆盖接口（如 `EntityMetaFiller`、`TenantHandler`、`HmacClientService`、`ServerBootstrapConfigurationCustomizer`、`ChannelPipelineConfigurationCustomizer`）。

### 3.3 自动配置禁令（MUST NOT）

- 不得在业务域包内放置 `@AutoConfiguration` 入口（必须统一在 `com.lambda.autoconfig`）。
- 不得遗漏 `AutoConfiguration.imports` 注册或末尾留空行。
- 不得添加无条件 Bean（应经条件装配）。

## 4. core 与 processor 维护契约

### 4.1 core 纯基础库（MUST）

- `lambda-cloud-core` 为纯基础库，**不得**提供自动配置、不得有 `@AutoConfiguration`/`AutoConfiguration.imports`、不得引入业务依赖。
- core 的基类（`BaseDO`/`BaseDTO`/`BaseVO`/`BasePageDTO`/`BaseEnum`）、转换设施（`@AutoConverter`/`ConverterResolver`）、工具（`Constants`/`HmacGenerator`/`TypeConverter`/`Assert`）、异常体系（`ErrorModel`/`ErrorCode`）变更须**保持向后兼容**，破坏性变更须评估下游影响。

### 4.2 注解处理器维护（MUST）

- `lambda-cloud-processor` 的 `AutoConverterProcessor`（`@AutoConverter` -> MapStruct）与 `PermissionProcessor`（API 权限元数据）产物由编译期生成。
- 改动处理器源码或注解定义后，必须 `mvn clean compile` 验证产物正确生成。

### 4.3 处理器禁令（MUST NOT）

- 不得手写或手工修改处理器生成的产物。
- 不得在业务模块自行引入与父 POM 冲突的注解处理器版本。

## 5. 配置属性命名契约

### 5.1 命名规则（MUST）

- 配置属性使用 **kebab-case**（如 `lambda.security.form.enabled`）。
- 前缀统一以 `lambda.` 或 `mybatis-plus.` 起头；新增 starter 属性前缀须在 README 属性前缀表登记。

### 5.2 starter 前缀表（MUST，已有的前缀不得随意改名）

| starter | 前缀 |
|---|---|
| security | `lambda.security.*` |
| oss | `lambda.oss.*` |
| liquibase | `lambda.liquibase.*` |
| netty | `lambda.netty.*` |
| cache | `lambda.cache.*` |
| sse | `lambda.sse.*` |
| mybatis | `mybatis-plus.*` |

### 5.3 禁令（MUST NOT）

- 不得散落使用 `@Value` 注入本应由 `@ConfigurationProperties` 管理的配置。
- 不得使用非 kebab-case 的属性名。

## 6. 各 starter 维护契约

> 本节规定维护各 starter 时须遵守的提供方规则；下游如何使用见 README。

### 6.1 MyBatis 增强（MUST）

- `LambdaSqlInjector` 新增 SQL 注入方法须注册到 `injector/method/` 并经注入器启用，不得散落在 mapper 外定义。
- 拦截器链顺序（Order 9 租户 handler -> Order 10 租户 line interceptor -> Order 20 分页 -> Order `Short.MAX_VALUE` 租户表达式拦截器）**不得随意打乱**；新增拦截器须明确 Order 值。
- 字段加密（`AesEncryptHandler`）、多租户（`TenantLineInnerInterceptor`+`TenantHandler`）、自动填充（`GlobalMetaObjectHandler`+`EntityMetaFiller`）的扩展点接口保持稳定，破坏性变更须评估下游影响。

### 6.2 Netty 与协议业务 starter（MUST）

- `lambda-cloud-starter-ocpp`/`-t645`/`-ykc` 等协议业务 starter 必须**复用** `lambda-cloud-starter-netty` 的 `ProtocolEngine`，不得重复实现编解码。
- 改动协议引擎（`ProtocolEngine`/`@ProtocolPayload`/`@ProtocolField`/`ByteCodeFieldAccessor`）须保证向后兼容，并跑 `mvn -pl lambda-cloud-starter-netty test` 及对应协议 starter 测试。

### 6.3 跨服务 starter（MUST）

- `lambda-cloud-starter-dubbo`/`-feign`/`-nacos` 的自动配置均须以 `@ConditionalOnClass` 判断对应依赖是否在 classpath；缺失依赖时必须优雅降级，不得启动失败。

### 6.4 Liquibase 聚合机制（MUST）

- `lambda-cloud-starter-liquibase` 维护 changelog 聚合逻辑（正则 `lambda-\w*-changelog.xml`、`lambda-datasource-changelog.xml` 最先、`lambda-additional-changelog.xml` 最后）；改动聚合逻辑须保证向下兼容，不得破坏下游既有 changelog 的加载顺序。

## 7. 静态检查契约（Spotless + SpotBugs）

### 7.1 已绑定的检查（MUST）

父 POM 在 **`compile`** 阶段强制执行两道关卡，`mvn compile` 即会在违规时失败：

- **Spotless - Palantir Java Format**（v2.67.0，`PALANTIR` 风格，`formatJavadoc=false`）。自动修复：`mvn spotless:apply`；仅检查：`mvn spotless:check`。
- **SpotBugs**（4.10.3.0，compile 阶段 `check`，使用 `${project.basedir}/spotbugs-exclude.xml`）。

### 7.2 完成标准（MUST）

- 所有 Java 代码必须已符合 Palantir 规范，否则构建失败。
- 提交前 `mvn compile`（含 Spotless + SpotBugs）必须通过。

### 7.3 禁令（MUST NOT）

- 不得绕过 Spotless/SpotBugs 提交（禁止 `--no-verify` 类绕过）。
- 不得手动编辑由 antrun 重新生成的 `lombok.config`（`addLombokGeneratedAnnotation=true`，排除覆盖率）。

## 8. 测试契约

### 8.1 运行约定（MUST）

- 测试运行在 classpath 上而非 module path（父 POM 设置 `surefire.useModulePath=false`）；遇到 JPMS 相关测试失败时据此排查。
- 改动协议引擎/编解码逻辑时，必须执行对应模块测试：`mvn -pl lambda-cloud-starter-netty test`、`mvn -pl lambda-cloud-starter-ykc test`、`mvn -pl lambda-cloud-starter-t645 test`、`mvn -pl lambda-cloud-starter-iotdb test`。

### 8.2 覆盖率（SHOULD）

- 覆盖率报告：`mvn clean verify`，打开 `target/site/jacoco/index.html`。
- 鼓励为 core/security/mybatis 补充测试。

### 8.3 禁令（MUST NOT）

- 不得提交无断言或仅“实现陪跑”的无效测试。
- 不得用反射访问 private 成员维持脆弱测试。

## 9. 安全红线契约

### 9.1 安全禁令（MUST NOT）

- 不得硬编码依赖版本、密钥、Token。
- 不得提交含真实凭据的配置文件。
- 不得在日志中输出敏感信息（密钥、Token、完整凭据）。
- 不得引入绕过鉴权、绕过多租户隔离、绕过字段加密的框架层实现。

## 10. Git 代码提交规范契约

### 10.1 提交信息格式（MUST）

使用 [Conventional Commits](https://www.conventionalcommits.org/) 风格 `<type>(<scope>): <描述>`：

- **type**：`feat`、`fix`、`refactor`、`style`、`docs`、`chore`、`test`、`perf`。
- **scope**（可选）：受影响模块，如 `core`、`mybatis`、`security`、`netty`、`liquibase`，或完整模块名 `lambda-cloud-starter-xxx`。
- **描述**：中英文均可，祈使语气、简明扼要。

### 10.2 提交粒度与检查（MUST）

- 原子提交；规则文件变更独立提交，不与业务代码混。
- 提交前 `mvn compile`（或对应模块 `mvn verify`）通过。

### 10.3 禁令（MUST NOT）

- 禁止不带描述的裸 `fix`/`refactor` 提交。
- 禁止 `--no-verify` 绕过检查、禁止 force push 主分支。

## 11. 代码组织与命名契约

### 11.1 包结构（MUST）

- 自动配置入口统一在 `com.lambda.autoconfig`（业务域包不得放 `@AutoConfiguration`）。
- 基础库 `com.lambda.cloud.core.*`；业务域 `com.lambda.cloud.<domain>`（mybatis/netty/iotdb 等）；安全域 `com.lambda.security.*`（历史包名，见 [package-structure.md](package-structure.md) §5）；注解处理器 `com.lambda.cloud.processor.*`。详见 [package-structure.md](package-structure.md)。

### 11.2 命名规则（MUST）

- 自动配置 `*AutoConfiguration`；属性 `*Properties`；实体 `*DO`（继承 `BaseDO`）；DTO `*DTO`（继承 `BaseDTO`）；VO `*VO`（继承 `BaseVO`）；Mapper 继承 `LambdaBaseMapper`；注解处理器 `*Processor`。

### 11.3 禁令（MUST NOT）

- 不得在 `com.lambda.autoconfig` 之外放自动配置入口。
- 不得自造与 core 基类重复的模型体系。
