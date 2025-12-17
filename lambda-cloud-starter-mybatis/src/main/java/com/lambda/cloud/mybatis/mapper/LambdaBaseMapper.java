package com.lambda.cloud.mybatis.mapper;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;

/**
 * ExtendBaseMapper
 *
 * @author jpjoo
 */
@SuppressWarnings("UnusedReturnValue")
public interface LambdaBaseMapper<T> extends BaseMapper<T> {

    /**
     * 批量插入的 条数 （如有需要再做成配置项）
     */
    int BATCH_SIZE = 1000;

    /**
     * mysql或oracle 动态判断数据库类型 形式的全字段批量新增
     *
     * @param entity list
     * @return int
     */
    int insertAll(List<T> entity);

    /**
     * mysql或oracle 动态判断数据库类型 形式的全字段批量新增  条数过多自动分批执行(事务需要自己添加，如果在这里夹容易出现事务嵌套)
     *
     * @param entity list   默认1000一次
     * @return int
     */
    default int insertAllBatch(List<T> entity) {
        return insertAllBatch(entity, BATCH_SIZE);
    }

    /**
     * mysql或oracle 动态判断数据库类型 形式的全字段批量新增  条数过多自动分批执行(事务需要自己添加，如果在这里夹容易出现事务嵌套)
     *
     * @param entity List
     * @param max    批次大小
     * @return int
     */
    default int insertAllBatch(List<T> entity, int max) {
        if (null == entity) {
            return 0;
        }
        final int size = entity.size();
        ListUtil.partition(entity, max).forEach(this::insertAll);
        return size;
    }

    /**
     * 根据code 查询单条记录 如有多条则只取第一条
     *
     * @param code 除id外的唯一值， 需要用 @TableCodeField 注解标识
     * @return 对应实体
     */
    T selectByCode(String code);

    /**
     * 根据code修改  不能修改主键
     *
     * @param entity 实体类，code字段必须有值
     * @return 修改条数
     */
    int updateByCode(@Param(Constants.ENTITY) T entity);

    /**
     * 根据code删除
     *
     * @param code 除id外的唯一值， 需要用 @TableCodeField 注解标识
     * @return 删除条数
     */
    int deleteByCode(String code);

    /**
     * 根据 Wrapper 条件，判断是否存在记录
     *
     * @param queryWrapper 实体对象封装操作类
     * @return 是否存在记录
     */
    @Override
    boolean exists(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);
}
