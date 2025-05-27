package com.lambda.cloud.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.OperatorUtils;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

/**
 * GlobalMetaObjectHandler
 *
 * @author Jin
 */
@Slf4j
public class GlobalMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LoginUser loginUser = OperatorUtils.getOperator();
        this.strictInsertFill(metaObject, "createUser", String.class, loginUser.getName());
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "delFlag", Boolean.class, false);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LoginUser loginUser = OperatorUtils.getOperator();
        this.strictUpdateFill(metaObject, "updateUser", String.class, loginUser.getName());
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
