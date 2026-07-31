# Lambda Cloud - GitHub Copilot 指令

> 本文件是 GitHub Copilot 在本仓库工作的中文规则速查。
> **完整权威规则以 `.rule/engineering-contract.md` 为准，冲突时以 `.rule/` 为准。** 规则正文不得仅在本文件修改，须同步 `.rule/`。

## 规则入口

动手前依次读取（`.rule/` 为唯一主规则来源）：

1. `.rule/project-charter.md` - 项目宪章（规则等级 AS-IS / SHOULD / MUST NOT、迁移态、例外）
2. `.rule/engineering-contract.md` - 工程契约（可执行条款）
3. `.rule/contract-index.md` - 主题索引 + 关键词索引
4. `.rule/package-structure.md` - 标准包分层细则

规则优先级：`MUST NOT`（禁止）> `MUST`（强制）> 宪章解释 > `AGENTS.md`（仅入口）。

## 红线速查（MUST NOT，任何情况下不得违反）

- 不得在 starter 模块 POM 硬编码依赖 `<version>`（由 parent POM / BOM 统一）。
- 不得重复实现 `lambda-cloud-core` 能力：基类（`BaseDO`/`BaseDTO`/`BaseVO`）、转换（`@AutoConverter`/`ConverterResolver`）、工具（`Constants`/`HmacGenerator`）、异常体系。
- 不得手写对象转换（用 `@AutoConverter` + `AutoConverterProcessor` 编译期生成）。
- 不得把 `optional=true` 可选依赖改强依赖（缺失须优雅降级）；改前必核对条件装配。
- 不得在业务域包内放 `@AutoConfiguration`（统一在 `com.lambda.autoconfig`）；不得遗漏 `AutoConfiguration.imports` 注册或末尾留空行。
- 不得手写或手工修改 `lambda-cloud-processor` 生成的产物（`AutoConverterProcessor`/`PermissionProcessor`）。
- 不得重复实现二进制编解码（须复用 `ProtocolEngine`）。
- 不得绕过 Spotless/SpotBugs 提交（禁止 `--no-verify`）、不得手动编辑 `lombok.config`。
- 不得硬编码密钥/Token/版本、不得提交含真实凭据的配置。
- 不得绕过鉴权/多租户隔离/字段加密。

## 自动配置与属性（MUST）

- 三件套：`com.lambda.autoconfig.XxxAutoConfiguration`（`@AutoConfiguration`，注册到 `AutoConfiguration.imports`）+ 条件 Bean + `XxxProperties`（`@ConfigurationProperties`）。
- 条件装配：`@ConditionalOnProperty`（带 `matchIfMissing`）/`@ConditionalOnMissingBean`/`@ConditionalOnClass`/`@ConditionalOnWebApplication`。
- 属性 kebab-case，前缀：`lambda.security.*`/`lambda.oss.*`/`lambda.liquibase.*`/`lambda.netty.*`/`lambda.cache.*`/`lambda.sse.*`/`mybatis-plus.*`。
- 模块独立性：每个 starter 可独立引入、最小依赖。

## 注解处理器（MUST）

`lambda-cloud-processor`：`AutoConverterProcessor`（`@AutoConverter` -> MapStruct 转换器）+ `PermissionProcessor`（API 权限元数据）。改注解后须 `mvn clean compile` 重新生成。

## 构建与提交

```bash
mvn clean install                  # 须先 install 本仓（parent/BOM 为 2026.1.1-SNAPSHOT）
mvn -pl <module> -am clean install # 单模块 + 依赖
mvn compile                        # 已绑定 Spotless(Palantir 2.67.0) + SpotBugs(4.10.3.0)
mvn spotless:apply                 # 格式修复
mvn -pl lambda-cloud-starter-netty test   # 协议模块测试
```

提交格式（Conventional Commits）：`<type>(<scope>): <描述>`，type ∈ feat/fix/refactor/style/docs/chore/test/perf，scope 优先模块名（如 `mybatis`/`security`/`netty`/`lambda-cloud-starter-xxx`），描述中英文均可、祈使语气。原子提交，规则文件独立提交，禁止裸 `fix`/`refactor`。提交前 `mvn compile` 通过。
