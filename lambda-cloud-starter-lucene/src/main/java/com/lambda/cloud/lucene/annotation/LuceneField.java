package com.lambda.cloud.lucene.annotation;

import java.lang.annotation.*;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;

/**
 * LuceneField
 *
 * @author Jin
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LuceneField {
    String name() default "";

    String pattern() default "";

    Class<? extends Field> field() default StoredField.class;

    Field.Store store() default Field.Store.YES;

    // 是否需要检索的字段
    boolean isQueryField() default false;

    // 是否扩展字段
    boolean isExtendField() default false;
}
