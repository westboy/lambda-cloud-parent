# lamuda-cloud-core

## 简介
 core 模块提供了核心依赖，包括日志、lombok、mybatis、hutool等。一般情况下，项目只需要依赖这一个核心包即可进入正常的开发编码，提高开发效率。
 
## 项目依赖
  没有当前脚手架依赖

## 使用方式
  项目的pom文件中添加以下依赖：
```
        <dependency>
            <groupId>${project.groupId}</groupId>
            <artifactId>lamuda-cloud-core</artifactId>
            <version>${project.version}</version>
        </dependency>
```

## 功能点
  1.Assert 断言功能:用户不需要再格外的硬编码进行判断必填项，返回错误，只需要按照对应格式返回即可。
    以判断字符串“str”是否为空为例:
```
    硬编码：
    if(null == str || "".equals(str)){
        return message;
    }
    使用断言：
    Assert.isBlank(str,"必填项不可为空")
```
  2.提供基础表的基础字段封装类baseDo，提供所有的字段自动插入修改的的值填充。
  ```
      @TableField(fill = FieldFill.INSERT)
    private String createUser;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private String updateUser;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @JsonIgnore
    @TableLogic(value = "0",delval = "1")
    private Boolean delFlag;
  ```
  3.提供日志、注册中心等组件基础配置。如果没有配置nacos的地址会使用默认地址localhost，默认配置账号密码为：nacos