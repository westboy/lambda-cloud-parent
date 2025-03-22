# lamuda-cloud-liquibase

## 简介
liquibase 可以通过维护xml，把数据库的变更跟随版本记录，简化生产环境数据同步复杂度。
 
## 项目依赖
```
   lamuda-cloud-starter-datasource
```

## 使用方式
1. 在pom.xml文件中引入相关依赖

```xml
   <dependency>
       <groupId>com.lamuda.cloud</groupId>
       <artifactId>lamuda-cloud-starter-liquibase</artifactId>
       <version>1.0.0-RELEASES</version>
   </dependency>
```

2. 在bootstrap/application.yaml文件中配置

```yaml
   lamuda:
     liquibase:
       enabled: true
       url: ${spring.datasource.url}
       username: ${spring.datasource.username}
       password: ${spring.datasource.password}
       driver-class-name: ${spring.datasource.driver-class-name}
```



## 新建文件夹

新建liquibase的changelogs目录，必须是固定结构：
```
- resources
  - META-INF
    - db
      - changelogs
        - init
        - update
```
编写总的changelog文件，用来引入后面需要新建的其他：init、后续维护的update文件夹

总的changelog文件的文件名格式：lamuda-项目名简称-changelog.xml。例如：lamuda-core-changelog.xml
```
- resources
    - META-INF
        - db
            - changelogs
                - init
                - update
                - lamuda-core-changelog.xml
```
在里面引入同包下的其他文件夹里面的changelog.xml文件：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">

    <!-- 引入初始化文件包里面的changelog.xml文件，内容包含初始化表、初始化数据等 />-->
    <includeAll path="META-INF/db/changelogs/init"/>
    <!-- 引入后续维护文件包里面的changelog.xml文件，内容包含修改表结构、新增数据等 />-->
    <includeAll path="META-INF/db/changelogs/update"/>

</databaseChangeLog>
```

## init文件夹

里面是初始化数据，分为初始化表和初始化数据
```
- resources
    - META-INF
        - db
            - changelogs
              - init
                - lamuda-core-create-table.xml
                - lamuda-core-insert-data.xml
```

### 初始化表

固定结构：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">

    <!-- changset语句>
    
</databaseChangeLog>
```

changset语句：

1. **changSet**标签：书写changset语句的外标签
    - id：**lamuda-项目名简称-年月日秒序号**(202407232501)。示例：lamuda-core-202407232501
    - author：作者名称。示例：echo
2. **preConditions**标签：**前置判断条件**
    - onFail：如果判断条件为false，需要执行的策略。一般用MARK_RAN，即错误跳过，继续执行下一个。
3. **not**标签：非
4. **tableExists**标签：表存在。经常与preConditions、not配合使用，达到“表如果存在则跳过执行下一个changSet语句"
5. **createTable**标签：创建表
    - table：期望创建的表名
    - remarks：表注释，相当于sql中的comment

6. **colmn**标签：创建字段的定义
    - name：字段名
    - type：字段类型，如果想设置字符集，直接跟着写在后面即可，如"varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci“
    - remark：字段注释，相当于comment
7. constraints标签：字段的约束
    - nullable：空值开关
        - 不指定：deafult null
        - flase：not null
    - primaryKey：主键开关
        - true：设置该字段为表主键



完整示例：

```xml
<changeSet id="lamuda-core-202407232501" author="echo">
    <preConditions onFail="MARK_RAN">
        <not>
            <tableExists tableName="decoction_info"/>
        </not>
    </preConditions>
    <createTable tableName="decoction_info" remarks="中药饮片字典">
        <column name="id" type="bigint" remarks="主键">
            <constraints primaryKey="true"/>
        </column>
        <column name="chn_code" type="varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci" remarks="中国中药饮片代码"/>
        <column name="create_time" type="datetime" remarks="创建时间">
            <constraints nullable="false"/>
        </column>
        <column name="del_flag" type="int" remarks="0未删除 1已删除" defaultValue="0">
            <constraints nullable="false"/>
        </column>
    </createTable>
</changeSet>
```



### 初始化数据

1. changSet标签：书写changset语句的外标签
    - id：lamuda-项目名简称-年月日秒序号(202407232501)。示例：lamuda-core-202407232501
    - author：作者名称。示例：echo
2. preConditions标签：前置判断条件
    - onFail：如果判断条件为false，需要执行的策略。一般用MARK_RAN，即错误跳过，继续执行下一个。
3. **sqlCheck**标签：设置一段sql用来检查，一般检查即将插入的数据的主键是否已经在表中存在
    - expectedResult：
        - 0：代表期望sql的返回结果为0，如果不是0则报错
4. **insert**标签：一个insert的完整开闭标签，就是一条插入数据
    - tableName：想插入数据的表名
5. **colmn**标签：
    - name：字段名称
    - valueNumeric：数字类型字段值
    - value：字符类型字段值，相当于mysql中使用''单引号括起来的值
    - valueDate：日期类型字段值



完整示例：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">
    
<!--    INSERT INTO `cmai_server_core`.`dict_info` (`id`, `code`, `type`, `name`, `value`, `notes`, `sort`, `create_time`, `update_time`, `create_user`, `update_user`, `del_flag`, `state`) VALUES (568804692331593728, 'PEOPLE_TYPE', 1, '普通人群', 2, '', '2', '2024-04-18 14:26:47', '2024-04-18 14:26:47', 550622042005700608, 550622042005700608, 0, 1);-->
    <changeSet id="cmai-core-202407251001" author="zsl">
        <preConditions onFail="MARK_RAN">
            <sqlCheck expectedResult="0">
                SELECT COUNT(1) FROM DUAL WHERE EXISTS(SELECT 1 FROM dict_info WHERE ID = 568804692331593728 )
            </sqlCheck>
        </preConditions>
        <insert tableName="dict_info">
            <column name="id" valueNumeric="568804692331593728"/>
            <column name="code" value="PEOPLE_TYPE"/>
            <column name="type" valueNumeric="1"/>
            <column name="name" value="普通人群"/>
            <column name="value" valueNumeric="2"/>
            <column name="notes" value=""/>
            <column name="sort" value="2"/>
            <column name="create_time" valueDate="2024-04-18 14:26:47"/>
            <column name="update_time" valueDate="2024-04-18 14:26:47"/>
            <column name="create_user" valueNumeric="550622042005700608"/>
            <column name="update_user" valueNumeric="550622042005700608"/>
            <column name="del_flag" valueNumeric="0"/>
            <column name="state" valueNumeric="1"/>
        </insert>
    </changeSet>
</databaseChangeLog>
```



## update文件夹

### 删除表

```xml
<changeSet id="dropTable" author="fulizhe">
		<preConditions>
			<tableExists tableName="tb2_member" />
		</preConditions>
		<dropTable tableName="tb2_member" />
</changeSet>
```



### 字段

#### 添加字段

```xml
	<changeSet id="cmai-core-202407251001" author="zsl">
        <addColumn tableName="user_info">
            <column name="chn_code" type="varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci" remarks="中国中药饮片代码"/>
        </addColumn>
    </changeSet>
```

1. **addColmn**标签：
    - tableName：想插入字段的表名
2. colmn标签：
    - name：字段名称
    - valueNumeric：数字类型字段值
    - value：字符类型字段值，相当于mysql中使用''单引号括起来的值
    - valueDate：日期类型字段值



#### 删除字段

```xml
<dropColumn tableName="user_info" columnName="name"/>
```

1. **dropColmn**标签：
    - tableName：想删除字段的表名



#### 修改字段

```xml
<changeSet id="cmai-core-202407251001" author="zsl">
    <!-- 修改名称+类型 -->
	<renameColumn tableName="tb2_member" oldColumnName="nick_name" newColumnName="nick_name_new" columnDataType="varchar(20)"/>
    <!-- 修改类型 -->
    <modifyDataType tableName="tb2_member" columnName="nick_name_new" newDataType="varchar(20)" />
</changeSet>
```

1. **renameColumn**标签：
    - tableName：想修改字段的表名
    - oldColmnName：旧字段名
    - newColumnName：新字段名
    - columnDataType（选填）：字段类型
    - remark（选填）：新的字段描述
2. **modifyDataType**标签：
    - tableName：表名
    - columnName：字段名
    - newDataType：类型名



### 数据

#### 添加数据

```xml
<changeSet id="lamuda-core-202407232501" author="echo">
    <insert tableName="tb2_member">  
            <column  name="mobile"  value="132"/>  
    </insert>
</changeSet>
```



#### 修改数据

```xml
<changeSet id="lamuda-core-202407232501" author="echo">
	<update  tableName="tb2_member">
	    <column name="mobile" value="address value"/>
	    <where>id=100</where>
    </update>	
</changeSet>
```



#### 删除数据

```xml
<changeSet id="lamuda-core-202407232501" author="echo">
	<delete  tableName="tb2_member">  
        <where>id=100</where>  
    </delete> 
</changeSet>
```