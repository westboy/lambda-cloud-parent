# Lambda Cloud 项目宪章（Project Charter）

> 本宪章用于定义 Lambda Cloud 基础框架的工程治理目标、范围边界、权责关系与强制性原则。
> 本宪章对所有参与者（人类开发者与自动化编码助手）均具有约束力。

## 1. 目的（Purpose）

1) 统一基础框架工程规则入口，降低下游应用集成与自动化改动的不确定性。
2) 明确“允许做什么 / 禁止做什么 / 如何做”的执行标准。
3) 保障架构一致性：依赖治理、模块独立性、自动配置约定、跨切能力（多租户、数据权限、字段加密、认证、协议引擎等）不被破坏。
4) 明确本仓库是**能力提供方**：产出 parent POM、BOM 与 `lambda-cloud-starter-*` 自动配置 starter，供下游（如 `lambda-fusion-*`）依赖；规则需区分“能力提供方”与“下游集成方”视角。

## 2. 适用范围（Scope）

本宪章适用于本仓库内所有 `lambda-cloud-*` 模块（core / processor / dependencies + 26 个 starter）中的：

- starter 的提供与扩展、自动配置装配
- 依赖与版本治理（parent POM / BOM）
- 注解处理器（`lambda-cloud-processor`）的产物与契约
- 跨切能力（MyBatis 增强、安全认证、Netty 协议引擎、Liquibase 聚合等）的提供方实现
- 重构与架构调整、数据库变更（Liquibase changelog）

下游应用集成 `lambda-cloud-*` starter 时，应遵循本宪章与工程契约中面向“集成方”的条款（如配置前缀、条件装配开关、扩展点接口）。

## 3. 定义（Definitions）

### 3.1 规则等级

- 现状（AS-IS）：当前仓库已经落地的真实状态。若未明确要求迁移，修改应优先与现状保持一致。
- 推荐（SHOULD）：目标态或新增代码的推荐写法。与现状冲突时，默认仅对新增代码生效，不要求一次性迁移存量。
- 禁止（MUST NOT）：硬性红线。任何情况下不得违反；与示例或现状冲突时，以禁止为最高优先级。

### 3.2 迁移态（Partial Migration）

当同一主题同时存在“现状”与“推荐”时，表示项目处于部分迁移阶段：

- 未明确要求迁移：不得为追求推荐写法而大面积改造既有代码。
- 明确要求迁移：可以按迁移目标进行模块级或功能级改造，并需保证编译、测试、运行通过。

## 4. 工程治理原则（Engineering Principles）

### 4.1 单一事实来源（Single Source of Truth）

- 依赖版本由根 `pom.xml` properties 统一声明，经 `lambda-cloud-starter-dependencies`（BOM）对外管理；本仓 `lambda-cloud-parent` 自身即 parent POM。
- 对象转换、API 权限元数据由编译期注解处理器（`lambda-cloud-processor`）自动生成，不得手写。
- 多租户、字段加密、审计填充、认证等跨切能力由对应 starter 统一提供。

### 4.2 复用优先（Reuse First）

在引入新实现前，必须先确认 `lambda-cloud-core` 与各 starter 是否已提供能力；不得重复实现同类基类、转换器、拦截器、处理器、协议编解码。

### 4.3 模块独立性（Module Independence）

- 每个 starter 应可被独立引入、最小依赖，避免 starter 间强耦合。
- 可选依赖（`optional=true`）是刻意的，缺失时必须优雅降级；改强依赖前必须先核对条件装配逻辑。

### 4.4 安全与合规（Security & Compliance）

- 不得在模块中硬编码依赖版本或密钥。
- 不得引入绕过鉴权、绕过多租户隔离、绕过字段加密的实现。
- 敏感配置（数据库、Redis、密钥等）必须经环境变量注入，不得提交真实值。

## 5. 权责（Roles & Responsibilities）

### 5.1 规则维护者

- 负责维护工程契约内容与更新。
- 负责对“禁止事项”进行增补与纠偏。
- 负责同步 `.github/copilot-instructions.md`、`.cursorrules` 等摘要文件与 `.rule/` 主规则的一致性。

### 5.2 代码提交者（含自动化编码助手）

- 变更必须满足工程契约。
- 若规则冲突或规则缺失，必须在执行前发起澄清或变更请求，不得按历史习惯自行补充实现。
- 执行 Git 提交前必须遵守工程契约 §17（Git 代码提交规范）。

## 6. 交付物结构（Deliverables）

本仓库规则文档采用分层结构（以 `.rule` 目录为准）：

- `project-charter.md`：项目宪章（目标、范围、原则、权责、规则等级）
- `engineering-contract.md`：工程契约（可执行条款：依赖、模块独立、core 能力、转换、注解处理器、自动配置、MyBatis、安全、Netty、协议、跨服务、Liquibase、静态检查、测试、安全红线、Git 等）
- `contract-index.md`：工程契约索引（条款索引 / 关键词索引）
- `package-structure.md`：标准包分层结构（以 core / mybatis / security / netty 为参考标准，工程契约 §18 配套）

模块能力说明（自动配置入口、配置项、关键机制、how-to）位于 `README.md`；规则文件负责“怎么做”，README 负责“框架长什么样、怎么用”。

## 7. 争议与例外处理（Disputes & Exceptions）

当开发任务与工程契约发生冲突时：

1) 禁止事项优先。
2) 若必须突破现状约束，需明确迁移目标与影响范围。
3) 若必须引入例外，需以“新增条款”形式写入工程契约，并说明：适用范围、风险与回滚策略。
4) 已登记的过渡例外见 `contract-index.md` B 段“例外/过渡条款”。
