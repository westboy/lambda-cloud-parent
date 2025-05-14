# lambda-cloud-actuator

## 简介
 Actuator 模块提供了生产级别的功能，比如健康检查，审计，指标收集，HTTP 跟踪等，帮助我们监控和管理Spring Boot 应用。默认会读取符合包命名规范
的文件："classpath*:com/lambda/cloud/**/*.class"
 
## 项目依赖
```
lambda-cloud-core
```

## 使用方式
  项目的pom文件中添加以下依赖：
```
        <dependency>
            <groupId>${project.groupId}</groupId>
            <artifactId>lambda-cloud-starter-actuator</artifactId>
            <version>${project.version}</version>
        </dependency>
```
