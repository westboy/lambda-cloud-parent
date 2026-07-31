# 工程契约索引（条款索引 / 关键词索引）

> 本文件是 [engineering-contract.md](engineering-contract.md) 的导航索引：A 段按主题定位条款，B 段按关键词检索。

## A. 条款索引（按主题）

- 依赖/版本治理：工程契约 §1（SNAPSHOT 前置 §1.3）
- 模块独立性（独立可用 / optional 优雅降级）：工程契约 §2
- `lambda-cloud-core` 能力提供（基类/工具/异常）：工程契约 §3
- 对象转换（@AutoConverter / ConverterResolver）：工程契约 §4
- 注解处理器（AutoConverterProcessor / PermissionProcessor）：工程契约 §5
- starter 自动配置规范（三件套 / 条件装配）：工程契约 §6
- 配置属性命名（kebab-case / 前缀表）：工程契约 §7
- MyBatis 增强（注入器/加密/多租户/填充/拦截器顺序/多库）：工程契约 §8
- 安全认证（Sa-Token 多登录/策略/XSS/授权）：工程契约 §9
- Netty 协议引擎（注解/引擎/访问优化/服务/定制）：工程契约 §10
- 协议业务 starter（ocpp/t645/ykc）：工程契约 §11
- 跨服务（Dubbo/Feign/Nacos）：工程契约 §12
- 数据库迁移（Liquibase 聚合）：工程契约 §13
- 静态检查（Spotless + SpotBugs）：工程契约 §14
- 测试（useModulePath / 协议测试 / jacoco）：工程契约 §15
- 安全红线：工程契约 §16
- Git 代码提交规范：工程契约 §17（格式 §17.1 / 粒度与检查 §17.2 / 禁令 §17.3）
- 代码组织与命名：工程契约 §18
- **标准包分层结构：package-structure.md**

## B. 关键词索引（用于检索）

- 禁止硬编码版本 / parent POM / BOM / `lambda-cloud-starter-dependencies` / SNAPSHOT 前置 install / Spring Boot 4.1.0 / Spring Cloud 2025.1.2
- 模块独立 / `optional=true` / 优雅降级 / 最小依赖
- `BaseDO` / `BaseDTO` / `BaseVO` / `BasePageDTO` / `BaseEnum` / 审计字段 createUser/createTime/updateUser/updateTime
- `@AutoConverter` / `isReverse` / `ConverterResolver` / `ConvertFunctions` / `@FieldMapping` / MapStruct / 禁止手写转换
- `lambda-cloud-processor` / `AutoConverterProcessor` / `PermissionProcessor` / `ApiPermissionMetadata` / `PermissionFileMetadata` / 编译期生成 / `mvn clean compile`
- `@AutoConfiguration` / `AutoConfiguration.imports` / 末尾无空行 / `com.lambda.autoconfig`
- `@ConditionalOnProperty` / `@ConditionalOnMissingBean` / `@ConditionalOnClass` / `@ConditionalOnWebApplication` / `matchIfMissing` / 向后兼容 / 扩展点接口
- kebab-case / `lambda.security.*` / `lambda.oss.*` / `lambda.liquibase.*` / `lambda.netty.*` / `lambda.cache.*` / `lambda.sse.*` / `mybatis-plus.tenant.enabled` / `mybatis-plus.encrypt.*` / 禁止 `@Value` 散落
- `LambdaSqlInjector` / `LambdaBaseMapper` / `insertAll` / `selectByCode` / `updateByCode` / `deleteByCode` / `exists` / `@TableCodeField`
- `AesEncryptHandler` / `@TableField(typeHandler=)` / 字段加密
- `TenantLineInnerInterceptor` / `TenantHandler` / 多租户 / 与加密兼容
- `GlobalMetaObjectHandler` / `EntityMetaFiller` / 自动填充 / 拦截器顺序 Order 9/10/20/30
- `DatabaseIdProvider` / MySQL/Oracle/PostgreSQL/H2/DM
- Sa-Token / `StpLogicUtils` / loginUser / hmac / `FormAuthenticationProcessingFilter` / `FormLockingStrategy` / `StandardPasswordEncoder` / `CaptchaVerifyCodeGenerateImpl` / `RedisCaptchaStore` / `SmsVerifyCodeGenerateImpl` / `HmacAuthenticationProcessingFilter` / `HmacClientService` / `WxMaLoginProvider` / 第三方登录
- `XSSDefendFilter` / OWASP ESAPI / `@RequiresAuth` / `@RequiresPermission` / `@RequiresRole` / `SaSameUtil` / CSRF
- `@ProtocolPayload` / `@ProtocolField` / `ProtocolEngine` / `parse` / `serialize` / `validate` / UINT8/16/32/64 / HEX/ASCII/BCD / CP56Time2a / `ByteCodeFieldAccessor` / ASM / `ReflectionFieldAccessor` / `FieldAccessorFactory` / `CrcAlgorithm` / `Crc16Algorithm` / `NettyServer` / `SmartLifecycle` / EPOLL/NIO / `ServerBootstrapConfigurationCustomizer` / `ChannelPipelineConfigurationCustomizer` / `NettyChannelInitializer`
- 协议业务 starter / `lambda-cloud-starter-ocpp` / `lambda-cloud-starter-t645` / `lambda-cloud-starter-ykc` / 复用 ProtocolEngine
- Dubbo / Feign / Nacos / `@ConditionalOnClass` / 优雅降级
- Liquibase / `META-INF/db/changelogs` / `lambda-*-changelog.xml` / `lambda-datasource-changelog.xml` 最先 / `lambda-additional-changelog.xml` 最后 / 正则 `lambda-\w*` / `<include>` 带连字符子 changelog
- Spotless / Palantir Java Format / v2.67.0 / `formatJavadoc=false` / compile 阶段 / `mvn spotless:apply` / SpotBugs / 4.10.3.0 / `spotbugs-exclude.xml` / 禁止绕过
- `lombok.config` / `addLombokGeneratedAnnotation` / antrun 生成 / 禁止手编
- 测试 / `surefire.useModulePath=false` / `mvn -pl lambda-cloud-starter-netty test` / ykc/netty/t645/iotdb 测试 / jacoco / `mvn verify` / 禁止反射 private / 禁止无效陪跑测试
- 安全红线 / 禁止硬编码密钥 Token / 禁止绕过鉴权租户隔离加密 / 敏感日志禁入
- Git 提交 / Conventional Commits / type（feat/fix/refactor/style/docs/chore/test/perf）/ scope 优先模块名 / subject 中英文 / 原子提交 / 规则文件独立提交 / 禁止裸 fix/refactor / 禁止 --no-verify / 禁止 force push 主分支
- 包结构 / `com.lambda.autoconfig` / `com.lambda.cloud.core.*` / `com.lambda.cloud.<domain>` / `com.lambda.security.*` / `com.lambda.cloud.processor.*` / 命名 `*AutoConfiguration` `*Properties` `*DO` `*DTO` `*VO` `LambdaBaseMapper`
- 例外/过渡条款 / 宪章 §7 登记 / 测试覆盖不均（ykc 127/netty 25/多数模块无测试，待补）/ security 包名 `com.lambda.security` 与其他域 `com.lambda.cloud.<domain>` 不一致（历史现状）
