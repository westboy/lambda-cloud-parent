### lambda-cloud-starter-lucene 项目介绍及使用说明

#### 简介
`lambda-cloud-starter-lucene` 是一个基于Apache Lucene的全文搜索模块，主要用于在微服务架构中实现高效的全文搜索功能。它包含了Lucene的自动配置、索引创建、索引搜索等功能，以简化开发者在项目中对全文搜索的管理。

#### 主要功能
1. **Lucene自动配置**：模块中包含了Spring Boot的自动配置功能，可以自动读取配置文件中的Lucene信息并进行配置。
2. **索引创建**：支持创建全文搜索索引，可以将数据索引化以便进行高效搜索。
3. **索引搜索**：提供全文搜索功能，可以对索引进行查询操作，快速获取搜索结果。

#### 项目依赖
该项目依赖于多个库来实现其功能，包括：
- `lucene-core`：提供Lucene的核心功能。
- `lucene-analyzers-common`：提供常用的文本分析器。
- `lucene-queryparser`：提供查询解析器，支持复杂的查询语法。

#### 使用方式
要在你的项目中使用 `lambda-cloud-starter-lucene`，你需要在项目的pom文件中添加以下依赖：
```xml
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>lambda-cloud-starter-lucene</artifactId>
    <version>${project.parent.version}</version>
</dependency>
```

其中`${project.groupId}`和`${project.parent.version}`应替换为实际的项目信息。

#### 配置示例
在项目的配置文件（如application.yaml）中，可以配置Lucene信息：
```yaml
lucene:
  directory: RAMDirectory
  analyzer: StandardAnalyzer
```

通过上述配置和依赖添加，你可以在项目中使用`lambda-cloud-starter-lucene`提供的全文搜索功能，简化全文搜索的配置和使用。
