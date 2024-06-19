//package com.jingfang.cloud.mybatis.meta;
//
//import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.ibatis.reflection.MetaObject;
//import org.springframework.stereotype.Component;
//import java.time.LocalDateTime;
//
///**
// * GlobalMetaObjectHandler
// *
// * @author Jin
// */
//@Slf4j
//@Component
//public class GlobalMetaObjectHandler implements MetaObjectHandler {
//
//    @Override
//    public void insertFill(MetaObject metaObject) {
//        LoginUser loginUser = SecurityUtils.getCurrentLoginUser();
//        if (null != loginUser) {
//            this.strictInsertFill(metaObject, "createUser", Integer.class, Integer.parseInt(loginUser.getUserId().toString()));
//        } else {
//            this.strictInsertFill(metaObject, "createUser", Integer.class, -1);
//        }
//        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
//    }
//
//    @Override
//    public void updateFill(MetaObject metaObject) {
//        LoginUser loginUser = SecurityUtils.getCurrentLoginUser();
//        if (null != loginUser) {
//            this.strictUpdateFill(metaObject, "updateUser", Integer.class, Integer.parseInt(loginUser.getUserId().toString()));
//        } else {
//            this.strictInsertFill(metaObject, "updateUser", Integer.class, -1);
//        }
//        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
//    }
//}
