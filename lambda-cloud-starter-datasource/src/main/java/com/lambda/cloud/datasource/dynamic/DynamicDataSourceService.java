package com.lambda.cloud.datasource.dynamic;

import com.lambda.cloud.datasource.property.DataSourceProperty;
import javax.sql.DataSource;

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
     * 获取数据源
     *
     * @param id
     * @return boolean
     */
    DataSource getDataSource(String id);

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
