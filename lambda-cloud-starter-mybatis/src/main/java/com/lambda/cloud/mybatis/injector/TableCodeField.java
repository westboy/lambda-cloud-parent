package com.lambda.cloud.mybatis.injector;

import java.lang.annotation.*;

/**
 * 标注该字段为唯一编码
 * 仅起到标注作用，需要配合TableField来实现
 *
 * @author jpjoo
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TableCodeField {}
