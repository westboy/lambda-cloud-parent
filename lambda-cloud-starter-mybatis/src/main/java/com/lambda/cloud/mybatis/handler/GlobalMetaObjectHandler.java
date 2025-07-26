package com.lambda.cloud.mybatis.handler;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

/**
 * GlobalMetaObjectHandler
 *
 * @author Jin
 */
@Slf4j
@SuppressFBWarnings("EI_EXPOSE_REP")
public record GlobalMetaObjectHandler(List<EntityMetaFiller> entityMetaFillers) implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        if (CollUtil.isNotEmpty(entityMetaFillers)) {
            for (EntityMetaFiller entityMetaFiller : entityMetaFillers) {
                entityMetaFiller.insertFill(this, metaObject);
            }
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (CollUtil.isNotEmpty(entityMetaFillers)) {
            for (EntityMetaFiller entityMetaFiller : entityMetaFillers) {
                entityMetaFiller.updateFill(this, metaObject);
            }
        }
    }
}
