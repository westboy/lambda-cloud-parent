# 标准包分层结构

> 本文以 `lambda-cloud-core` / `lambda-cloud-starter-mybatis` / `lambda-cloud-starter-security` / `lambda-cloud-starter-netty` 为参考标准，是 [engineering-contract.md](engineering-contract.md) §18 的配套细则。
> 冲突时以工程契约 §18 为准；本文负责把「长什么样」说具体。

## 1. 总体原则

- **自动配置入口统一**放在 `com.lambda.autoconfig`，业务域包内不得放置 `@AutoConfiguration`（见工程契约 §6.3）。
- **基础库** `lambda-cloud-core` 在 `com.lambda.cloud.core.*`，不套用业务子域分层。
- **业务域**在 `com.lambda.cloud.<domain>`（mybatis/netty/iotdb 等）；**安全域**因历史原因在 `com.lambda.security`（非 `com.lambda.cloud.security`，见 §5 例外）。
- **注解处理器**在 `com.lambda.cloud.processor.*`。
- 新增代码补充到现有子包，禁止新建并行的旧式顶层分层目录。

## 2. 标准目录树

### 2.1 基础库 `lambda-cloud-core`

```
com.lambda.cloud.core
├── shared/        基类: BaseDO / BaseDTO / BaseVO / BasePageDTO / BaseEnum
├── annotation/    注解: @AutoConverter / @FieldMapping 等
├── convert/       转换: ConverterResolver / BaseConverter / ConvertFunctions
├── exception/     异常体系: ErrorModel / ErrorCode / Feign 异常族
├── jackson/       JSON 序列化
├── principal/     身份主体
└── utils/         工具: Constants / HmacGenerator / TypeConverter / ClassTypeUtils / Assert
```

### 2.2 业务 starter（以 mybatis / netty 为参考）

```
com.lambda.autoconfig              # 自动配置入口(统一)
├── XxxAutoConfiguration           @AutoConfiguration, 注册到 AutoConfiguration.imports
├── condition/                     条件判定(按需)
└── datascope/                     数据权限装配(按需)

com.lambda.cloud.<domain>          # 业务域
├── handler/        处理器(如 mybatis 的 AesEncryptHandler / GlobalMetaObjectHandler)
├── injector/       注入器(如 LambdaSqlInjector)
├── mapper/         Mapper 基类(如 LambdaBaseMapper)
├── mapping/        SQL 方法定义
├── tenant/         多租户(如 TenantLineInnerInterceptor / TenantHandler)
├── datascope/      数据权限
├── protocol/       协议(如 netty 的 protocol/engine/annotation/...)
├── customizer/     定制器(如 netty 的 ServerBootstrap/ChannelPipeline Customizer)
├── pool/           连接池(按需)
├── repository/     仓储(按需)
├── exception/      域异常
└── utils/          域工具(按需)
```

### 2.3 安全域 `lambda-cloud-starter-security`

```
com.lambda.autoconfig              # 自动配置入口
com.lambda.security                # 安全域(历史包名, 非 com.lambda.cloud.security)
├── web/                           # Web 认证入口
│   ├── form/                      表单登录(FormAuthenticationProcessingFilter/FormLogoutFilter)
│   ├── hmac/                      HMAC 认证(HmacAuthenticationProcessingFilter)
│   ├── sms/                       短信登录
│   ├── third/                     第三方登录(WxMaLoginProvider 等)
│   ├── verify/                    验证码(Captcha/Sms 生成与存储)
│   └── xss/                       XSS 防护(XSSDefendFilter)
├── encode/                        编码(StandardPasswordEncoder 等)
├── handler/                       处理器(+ impl/)
├── provider/                      认证 provider
├── service/                       服务(如 HmacClientService 契约)
├── inteceptor/                    拦截器
└── exception/                     安全异常
```

### 2.4 注解处理器 `lambda-cloud-processor`

```
com.lambda.cloud.processor
├── converter/    AutoConverterProcessor(@AutoConverter -> MapStruct)
└── permission/   PermissionProcessor + extractor + scanner + model(ApiPermissionMetadata/PermissionFileMetadata) + config
```

## 3. 子包职责

| 子包 | 职责 | 禁止 |
| --- | --- | --- |
| `com.lambda.autoconfig` | `@AutoConfiguration` 入口 + 条件 Bean 装配 | 放业务实现、漏 `AutoConfiguration.imports` 注册 |
| `shared/`（core） | 基类定义 | 放业务逻辑 |
| `convert/`（core） | 转换器定位与公共转换 | 手写转换、与 `@AutoConverter` 重复 |
| `handler/` | 处理器（加密、填充等） | 与子域无关的通用处理 |
| `injector/` | SQL 方法注入器 | 业务逻辑 |
| `mapper/` | Mapper 基类继承 `LambdaBaseMapper` | 写业务逻辑 |
| `tenant/` | 多租户拦截器与 handler | 自建租户切换独立数据源 |
| `protocol/`（netty） | 协议引擎、注解、字段访问 | 重复实现编解码 |
| `customizer/`（netty） | Bootstrap/Pipeline 定制扩展点 | — |
| `web/<strategy>/`（security） | 各认证策略入口 | 策略间强耦合 |

## 4. 命名

见工程契约 §18.2。重点：自动配置 `*AutoConfiguration`、属性 `*Properties`、实体 `*DO`（继承 `BaseDO`）、DTO `*DTO`（继承 `BaseDTO`）、VO `*VO`（继承 `BaseVO`）、Mapper 继承 `LambdaBaseMapper`、注解处理器 `*Processor`。

## 5. 参考实现与例外

- **参考标准**：`lambda-cloud-core`（基础库分层）、`lambda-cloud-starter-mybatis`（业务域 + autoconfig 分层）、`lambda-cloud-starter-security`（多策略 web 分层）、`lambda-cloud-starter-netty`（协议引擎 + 定制器分层）。新增 starter 应遵循同一结构。
- **例外（按宪章 §7 登记）**：`lambda-cloud-starter-security` 包名为 `com.lambda.security`，与其他业务域 `com.lambda.cloud.<domain>` 不一致（历史现状，未要求迁移；新增域应统一用 `com.lambda.cloud.<domain>`）。
- **特殊模块**：`lambda-cloud-starter-dependencies` 为 BOM（`packaging=pom`，无代码）；`lambda-cloud-starter-test` 为测试设施库。
