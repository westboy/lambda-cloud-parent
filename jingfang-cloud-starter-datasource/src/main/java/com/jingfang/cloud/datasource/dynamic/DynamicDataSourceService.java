package com.jingfang.cloud.datasource.dynamic;

import com.jingfang.cloud.datasource.property.DataSourceProperty;

/**
 * 動態數據源
 *
 * @author w
 */
public interface DynamicDataSourceService {

    /**
     * 添加數據源
     *
     * @param property
     * @return boolean
     */
    boolean addDataSource(DataSourceProperty property);

    /**
     * 修改数据源
     *
     * @param id
     * @param property
     * @return boolean
     */
    boolean updateDataSource(String id, DataSourceProperty property);

    /**
     * 删除数据源
     *
     * @param id
     * @return boolean
     */
    boolean removeDataSource(String id);

    /**
     * 测试数据源
     *
     * @param property
     * @return boolean
     */
    boolean test(DataSourceProperty property);
}
