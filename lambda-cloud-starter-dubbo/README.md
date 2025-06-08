# lambda-cloud-starter-dubbo Dubbo Starter模块

## 功能概述
本模块提供Dubbo分布式服务框架的Spring Boot Starter支持，主要功能包括：
1. Dubbo服务提供者和消费者自动配置
2. 集成Nacos服务发现
3. 提供Kryo序列化支持

## 核心依赖
- org.apache.dubbo:dubbo
- org.apache.dubbo:dubbo-spring-boot-starter
- org.apache.dubbo:dubbo-spring-boot-actuator
- com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery
- com.esotericsoftware:kryo (5.4.0)
- de.javakaffee:kryo-serializers (0.45)

## 配置说明
Dubbo基础配置示例：
```properties
# 应用名称
dubbo.application.name=your-service-name
# 注册中心地址
dubbo.registry.address=nacos://127.0.0.1:8848
# 协议配置
dubbo.protocol.name=dubbo
dubbo.protocol.port=20880
```

## 注意事项
1. 需要配合Nacos服务发现使用
2. 默认使用Kryo作为序列化方式
3. 版本号继承自父项目${project.parent.version}
4. 当前WelcomeLogoApplicationListener为空实现
