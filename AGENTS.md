# Lambda Cloud 项目规则入口

> 本文件是自动化编码助手的规则入口文件。
> Lambda Cloud 基础框架的完整工程规则以 `.rule` 目录为唯一主规则来源。
> `CLAUDE.md` 通过 `@AGENTS.md` 导入引用本文件而非复制内容--请编辑本文件，不要编辑 `CLAUDE.md`。

## 1. 主规则来源

所有代码生成、修改、重构、依赖调整、配置调整、数据库变更与架构决策，必须优先读取并遵守以下文件：

1. [.rule/project-charter.md](.rule/project-charter.md) - 项目宪章
2. [.rule/engineering-contract.md](.rule/engineering-contract.md) - 工程契约
3. [.rule/contract-index.md](.rule/contract-index.md) - 附录索引
4. [.rule/package-structure.md](.rule/package-structure.md) - 包结构细则（工程契约 §18 配套）

## 2. 文件职责

| 文件 | 职责 |
| :--- | :--- |
| `.rule/project-charter.md` | 项目宪章：定义工程治理目标、适用范围、规则等级、迁移态、权责与例外处理原则 |
| `.rule/engineering-contract.md` | 工程契约：定义可执行条款，包括依赖治理、模块独立、core 能力、转换、注解处理器、自动配置、MyBatis、安全、Netty、协议、跨服务、Liquibase、静态检查、测试、安全红线、Git 等规则 |
| `.rule/contract-index.md` | 附录索引：提供主题索引与关键词索引，用于快速定位工程契约条款 |
| `.rule/package-structure.md` | 包结构细则：工程契约 §18 的配套细则，把标准包分层结构说具体；冲突时以工程契约 §18 为准 |
| `README.md` | 框架入门与能力说明：构建与测试、架构与模块组织、core 转换体系、MyBatis/Security/Netty 能力、常见开发任务；规则文件负责“怎么做”，README 负责“框架长什么样、怎么用” |

## 3. 规则优先级

当规则之间存在差异或执行歧义时，按以下顺序处理：

1. `.rule/engineering-contract.md` 中的 `MUST NOT` / 禁止条款优先级最高。
2. `.rule/engineering-contract.md` 中的 `MUST` / 强制条款必须执行。
3. `.rule/project-charter.md` 中定义的规则等级、迁移态与例外处理原则用于解释工程契约。
4. 本 `AGENTS.md` 仅作为规则入口，不承载工程规则正文。

## 4. 自动化助手执行要求

自动化编码助手在执行任务前必须遵守以下流程：

1. 先读取 `.rule/project-charter.md`，确认规则等级、迁移态和例外处理原则。
2. 再读取 `.rule/engineering-contract.md`，确认当前任务涉及的强制条款与禁止条款。
3. 必要时读取 `.rule/contract-index.md`，通过关键词定位相关规则。
4. 若用户要求与 `.rule` 规则冲突，必须先说明冲突点并请求确认，不得直接违反禁止条款。
5. 若 `.rule` 存在缺失、冲突或不可执行之处，应优先提出规则修订建议，而不是按历史习惯自行补充实现。
6. 执行 Git 提交前，必须遵守 [工程契约 §17 Git 代码提交规范](.rule/engineering-contract.md)（Conventional Commits 格式、scope 优先模块名、subject 中英文、原子提交、提交前 `mvn compile` 通过、规则文件独立提交）。

## 5. 构建与运行速查

> 详细说明见 [README.md](README.md)；本节仅为速查，规则以 `.rule/` 为准。

- 构建：`mvn clean install`（本仓 parent/BOM 为 SNAPSHOT，下游依赖前必须先 install 本仓到本地仓库）。
- 单模块：`cd <module> && mvn clean install`，或 `mvn -pl <module> -am clean install`。
- 静态检查：`mvn compile` 已绑定 Spotless（Palantir 2.67.0）+ SpotBugs（4.10.3.0）；修复用 `mvn spotless:apply`（见工程契约 §14）。
- 测试：`mvn -pl <module> test`（协议模块 netty/ykc/t645/iotdb 有测试，见工程契约 §15）。
- 覆盖率：`mvn clean verify`，查看 `target/site/jacoco/index.html`。
- 注解处理器：改 `@AutoConverter`/`@FieldMapping`/协议注解/权限注解后须 `mvn clean compile` 重新生成产物（见工程契约 §5）。

## 6. 规则维护

- 新增规则必须写入 `.rule/engineering-contract.md` 或对应 `.rule` 文件，不得追加到本文件。
- 索引更新维护在 `.rule/contract-index.md`。

## 7. AI 智能体识别文件

为让不同 AI 编码助手自动识别项目规则，除本 `AGENTS.md` 外维护以下中文摘要 / 引导文件，均指向 `.rule/` 为唯一主规则来源：

| 文件 | 识别的智能体 |
|---|---|
| `AGENTS.md` | Claude Code（经 `CLAUDE.md` 引导）、Cursor、Codex、IDEA AI Assistant 2025+ |
| `.github/copilot-instructions.md` | GitHub Copilot |
| `.cursorrules` | Cursor、部分 IDEA AI Assistant 配置 |

- `.github/copilot-instructions.md` 与 `.cursorrules` 承载规则中文速查，便于智能体在上下文内直接引用；完整权威规则以 `.rule/engineering-contract.md` 为准，冲突时以 `.rule/` 为准。
- 摘要 / 引导文件内容变更时须同步更新 `.rule/` 主规则，禁止仅在摘要文件中修改规则。
