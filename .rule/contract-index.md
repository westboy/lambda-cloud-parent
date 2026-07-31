# 工程契约索引（条款索引 / 关键词索引）

> 本文件是 [engineering-contract.md](engineering-contract.md) 的导航索引：A 段按主题定位条款，B 段按关键词检索。
> 本仓是能力提供方，索引聚焦“仓内维护规则”；下游如何使用这些能力见 `README.md`「架构与核心能力」。

## A. 条款索引（按主题）

- 依赖/版本治理：工程契约 §1（SNAPSHOT 前置 §1.3 / 版本升级 §1.4）
- 模块独立性（独立可用 / optional 优雅降级）：工程契约 §2
- starter 自动配置规范（三件套 / 条件装配 / 禁令）：工程契约 §3
- core 与 processor 维护（core 纯基础库 / 向后兼容 / 处理器产物不手写）：工程契约 §4
- 配置属性命名（kebab-case / 前缀表）：工程契约 §5
- 各 starter 维护（MyBatis 注入器与拦截器顺序 / Netty 协议复用 / 跨服务条件装配 / Liquibase 聚合）：工程契约 §6（§6.1 MyBatis / §6.2 Netty 与协议 / §6.3 跨服务 / §6.4 Liquibase）
- 静态检查（Spotless + SpotBugs）：工程契约 §7
- 测试（useModulePath / 协议测试 / jacoco）：工程契约 §8
- 安全红线：工程契约 §9
- Git 代码提交规范：工程契约 §10（格式 §10.1 / 粒度与检查 §10.2 / 禁令 §10.3）
- 代码组织与命名：工程契约 §11
- **标准包分层结构：package-structure.md**

## B. 关键词索引（用于检索）

- 禁止硬编码版本 / parent POM / BOM / `lambda-cloud-starter-dependencies` / SNAPSHOT 前置 install / Spring Boot 4.1.0 / Spring Cloud 2025.1.2 / 版本升级须在 properties 统一
- 模块独立 / `optional=true` / 优雅降级 / 最小依赖 / 禁止改强依赖
- `@AutoConfiguration` / `AutoConfiguration.imports` / 末尾无空行 / `com.lambda.autoconfig` / 业务域包不得放自动配置入口
- `@ConditionalOnProperty` / `matchIfMissing` / `@ConditionalOnMissingBean` / `@ConditionalOnClass` / `@ConditionalOnWebApplication` / 扩展点接口优先
- `lambda-cloud-core` 纯基础库 / 不得有自动配置 / 不得引业务依赖 / 基类变更向后兼容（`BaseDO`/`BaseDTO`/`BaseVO`/`BaseEnum`）
- `lambda-cloud-processor` / `AutoConverterProcessor` / `PermissionProcessor` / 编译期生成 / 改处理器须 `mvn clean compile` / 禁止手写产物
- kebab-case / `lambda.security.*` / `lambda.oss.*` / `lambda.liquibase.*` / `lambda.netty.*` / `lambda.cache.*` / `lambda.sse.*` / `mybatis-plus.*` / 禁止 `@Value` 散落
- `LambdaSqlInjector` / 新增注入方法须注册 / `LambdaBaseMapper` / 拦截器顺序 Order 9/10/20/`Short.MAX_VALUE` 不得打乱 / 扩展点（`EntityMetaFiller`/`TenantHandler`/`AesEncryptHandler`）稳定
- `ProtocolEngine` / 协议业务 starter（`ocpp`/`t645`/`ykc`）须复用 / 不重复实现编解码 / 改引擎须跑 netty 与协议 starter 测试
- 跨服务 starter（`dubbo`/`feign`/`nacos`）/ `@ConditionalOnClass` / 优雅降级
- Liquibase 聚合 / `lambda-\w*-changelog.xml` 正则 / `lambda-datasource-changelog.xml` 最先 / `lambda-additional-changelog.xml` 最后 / 改聚合须向下兼容
- Spotless / Palantir Java Format / v2.67.0 / `formatJavadoc=false` / compile 阶段 / `mvn spotless:apply` / SpotBugs / 4.10.3.0 / `spotbugs-exclude.xml` / 禁止绕过
- `lombok.config` / `addLombokGeneratedAnnotation` / antrun 生成 / 禁止手编
- 测试 / `surefire.useModulePath=false` / `mvn -pl lambda-cloud-starter-netty test` / ykc/netty/t645/iotdb 测试 / jacoco / `mvn verify` / 禁止反射 private / 禁止无效陪跑测试
- 安全红线 / 禁止硬编码密钥 Token / 禁止提交真实凭据 / 敏感日志禁入 / 禁止绕过鉴权租户隔离加密
- Git 提交 / Conventional Commits / type（feat/fix/refactor/style/docs/chore/test/perf）/ scope 优先模块名 / subject 中英文 / 原子提交 / 规则文件独立提交 / 禁止裸 fix/refactor / 禁止 --no-verify / 禁止 force push 主分支
- 包结构 / `com.lambda.autoconfig` / `com.lambda.cloud.core.*` / `com.lambda.cloud.<domain>` / `com.lambda.security.*`（历史包名）/ `com.lambda.cloud.processor.*` / 命名 `*AutoConfiguration` `*Properties` `*DO` `*DTO` `*VO` `LambdaBaseMapper` `*Processor`
- 例外/过渡条款 / 宪章 §7 登记 / `com.lambda.security` 历史包名与其他域 `com.lambda.cloud.<domain>` 不一致（未要求迁移）/ 测试覆盖不均（ykc 127/netty 25/多数模块无测试，待补）
