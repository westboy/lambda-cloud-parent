package com.lambda.cloud.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

/**
 * 实体字段填充器
 *
 * @author jin
 */
public interface EntityMetaFiller {

    /**
     * 插入时填充
     *
     * @param handler    默认处理器
     * @param metaObject 元信息
     */
    default void insertFill(MetaObjectHandler handler, MetaObject metaObject) {}

    /**
     * 更新时填充
     *
     * @param handler    默认处理器
     * @param metaObject 元信息
     */
    default void updateFill(MetaObjectHandler handler, MetaObject metaObject) {}
}
