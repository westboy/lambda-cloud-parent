## 关于 lambda-cloud

基于 Spring Cloud 的微服务架构开发框架，集成了各种常用的中间件，提供了一套完整的微服务解决方案。它包含了多个子模块，每个子模块提供不同的功能。
用于支持快速创建微服务脚手架，组件内提供了一些基础的配置。在中小型项目下业务开发人员只需要专注于业务开发，不用考虑框架层的各种配置。

## 项目层级及项目介绍

```
lambda-cloud-parent
     ├── lambda-cloud-core
     ├── lambda-cloud-starter-actuator 
     ├── lambda-cloud-starter-datasource 
     ├── lambda-cloud-starter-dependencies 
     ├── lambda-cloud-starter-dubbo 
     ├── lambda-cloud-starter-feign 
     ├── lambda-cloud-starter-gateway 
     ├── lambda-cloud-starter-kafka 扩充kafka 增加延迟队列功能
     ├── lambda-cloud-starter-liquibase 
     ├── lambda-cloud-starter-logger 
     ├── lambda-cloud-starter-lucene 
     ├── lambda-cloud-starter-mybatis 
     ├── lambda-cloud-starter-oss 
     ├── lambda-cloud-starter-plugin 
     ├── lambda-cloud-starter-redis 
     ├── lambda-cloud-starter-security 
     ├── lambda-cloud-starter-swagger 
     ├── lambda-cloud-starter-test
     ├── lambda-cloud-starter-web 
     └── lambda-cloud-starter-websocket
```

## 系统版本号

### 版本号命名

版本号格式为：主版本号.次版本号.修订号，版本号递增规则如下：

* 主版本号：系统版本号是从1开始的，主版本号表示一个全新的框架版本，例如:重大的重构重大的功能改动、重大的不兼容性的变化;
* 次版本号：发布较大的新功能，或者较大的重构或者模块变化，或者出现不兼容性改动，会增加子版本号;子版本的发布会伴随着完整的changelog，算是一个较大的版本发布;
* 修订号：往往是bug修复，或者增加较小的功能改进，在保证完整向后兼容的前提下，会增加修正版本号;

当主版本号增加时，子版本号及修正版本号置0;当子版本号增加时，修正版本号置
0;开发阶段，基于版本命名规则，在预上线的版本号上追加“-SNAPSHOT"，做为快照版本。
