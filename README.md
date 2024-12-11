## 关于 jingfang-cloud

  基于 Spring Cloud 的微服务架构开发框架，集成了各种常用的中间件，提供了一套完整的微服务解决方案。它包含了多个子模块，每个子模块提供不同的功能。
用于支持快速创建微服务脚手架，组件内提供了一些基础的配置。在中小型项目下业务开发人员只需要专注于业务开发，不用考虑框架层的各种配置。

## 项目层级及项目介绍

```
jingfang-cloud-parent
├── jingfang-cloud-starters
│   ├── jingfang-cloud-starter-actuator #提供核心依赖，包括日志、lombok、mybatis、hutool等。一般情况下，项目只需要依赖这一个核心包即可进入正常的开发编码，提高开发效率。
│   ├── jingfang-cloud-starter-core #提供所有的脚手架组件依赖集合，当时用到大多数组建的时候，用户避免一个一个的组件引入。
│   ├── jingfang-cloud-starter-datasource #提供数据源的支持，包括动态数据源和标准数据源。
│   ├── jingfang-cloud-starter-dependencies #模块管理了所有脚手架组件的依赖，包括日志、lombok、mybatis、hutool等。用户只需要依赖这一个核心包即可进入正常的开发编码，提高开发效率。
│   ├── jingfang-cloud-starter-dubbo #模块提供了一套完整的 Dubbo 微服务解决方案。它集成了 Dubbo、Spring Boot、Spring Cloud 等常用技术，用于构建高性能、高可用的分布式系统。
│   ├── jingfang-cloud-starter-feign #提供 Feign 客户端的支持，用于简化 HTTP 请求的发送。
│   ├── jingfang-cloud-starter-gateway #提供 Spring Cloud Gateway 的集成，用于构建 API 网关。
│   ├── jingfang-cloud-starter-kafka #提供 Kafka 的集成，用于消息队列的通信。
│   ├── jingfang-cloud-starter-liquibase #提供数据库版本管理工具 Liquibase 的集成。
│   ├── jingfang-cloud-starter-logger #提供日志记录和监控的支持。
│   ├── jingfang-cloud-starter-lucene #提供 Lucene 的集成，用于全文搜索。
│   ├── jingfang-cloud-starter-mybatis #提供 MyBatis 的集成，用于数据库操作。
│   ├── jingfang-cloud-starter-oss #提供对象存储服务（OSS）的支持。
│   ├── jingfang-cloud-starter-plugin #提供插件机制的支持，用于扩展框架功能。
│   ├── jingfang-cloud-starter-redis #提供 Redis 的集成，用于缓存和消息队列。
│   ├── jingfang-cloud-starter-security #提供安全认证和授权的支持。
│   ├── jingfang-cloud-starter-swagger #提供 Swagger 的集成，用于生成 API 文档。
│   ├── jingfang-cloud-starter-test #提供测试工具的支持。
│   ├── jingfang-cloud-starter-web #提供 Web MVC 的支持，用于构建 Web 应用。
│   └── jingfang-cloud-starter-websocket #提供 WebSocket 的支持，用于实现实时通信。
```

## 系统版本号
### 版本号命名
  版本号格式为：主版本号.次版本号.修订号，版本号递增规则如下：
  * 主版本号：系统版本号是从1开始的，主版本号表示一个全新的框架版本，例如:重大的重构重大的功能改动、重大的不兼容性的变化;
  * 次版本号：发布较大的新功能，或者较大的重构或者模块变化，或者出现不兼容性改动，会增加子版本号;子版本的发布会伴随着完整的changelog，算是一个较大的版本发布;
  * 修订号：往往是bug修复，或者增加较小的功能改进，在保证完整向后兼容的前提下，会增加修正版本号;
  
  当主版本号增加时，子版本号及修正版本号置0;当子版本号增加时，修正版本号置 0;开发阶段，基于版本命名规则，在预上线的版本号上追加“-SNAPSHOT"，做为快照版本。