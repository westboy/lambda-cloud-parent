# lambda-cloud-starter-dependencies 依赖管理模块

## 功能概述
本模块是Lambda Cloud项目的依赖管理父模块，主要功能：
1. 统一管理所有starter模块的版本
2. 提供dependencyManagement供子模块继承
3. 配置项目发布到Maven私服

## 核心功能

### 1. 依赖管理
统一管理的starter模块包括：
- lambda-cloud-starter-dubbo
- lambda-cloud-starter-gateway  
- lambda-cloud-starter-feign
- lambda-cloud-starter-actuator
- lambda-cloud-starter-datasource
- lambda-cloud-starter-liquibase
- lambda-cloud-starter-logger
- lambda-cloud-starter-lucene
- lambda-cloud-starter-oss
- lambda-cloud-starter-mybatis
- lambda-cloud-starter-plugin
- lambda-cloud-starter-redis
- lambda-cloud-starter-sercurity
- lambda-cloud-starter-swagger
- lambda-cloud-starter-web
- lambda-cloud-starter-websocket
- lambda-cloud-starter-sms

### 2. 发布配置
配置了发布到私有Maven仓库：
```xml
<distributionManagement>
    <repository>
        <id>maven-releases</id>
        <url>http://192.168.130.243:8081/repository/maven-releases/</url>
    </repository>
    <snapshotRepository>
        <id>maven-snapshots</id>
        <url>http://192.168.130.243:8081/repository/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```

### 3. Lombok配置
配置了Lombok生成的注解标记：
```
lombok.addLombokGeneratedAnnotation=true
```

## 使用说明
1. 子模块继承本模块即可统一版本
2. 发布时使用mvn deploy命令
3. 版本号继承自父项目${project.parent.version}
