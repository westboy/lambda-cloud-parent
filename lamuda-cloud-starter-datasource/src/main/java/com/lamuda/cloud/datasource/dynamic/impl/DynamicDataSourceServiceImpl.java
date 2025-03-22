package com.lamuda.cloud.datasource.dynamic.impl;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.lamuda.cloud.datasource.dynamic.DynamicDataSourceService;
import com.lamuda.cloud.datasource.property.DataSourceProperty;
import com.lamuda.cloud.datasource.utils.DataSourceUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;

/**
 * DynamicDataSourceServiceImpl
 *
 * @author w
 */
@Slf4j
public class DynamicDataSourceServiceImpl implements DynamicDataSourceService {


    private DynamicRoutingDataSource dynamicRoutingDataSource;

    @Autowired
    public void setDynamicRoutingDataSource(DynamicRoutingDataSource dynamicRoutingDataSource) {
        this.dynamicRoutingDataSource = dynamicRoutingDataSource;
    }


    @Override
    public boolean addDataSource(DataSourceProperty property) {
        HikariConfig configuration = new HikariConfig();
        configuration.setJdbcUrl(property.getUrl());
        configuration.setUsername(property.getUsername());
        configuration.setPassword(property.getPassword());
        String driverClassName = property.getDriverClassName();
        if (StringUtils.isNotBlank(driverClassName)) {
            configuration.setDriverClassName(driverClassName);
        }
        try {
            HikariDataSource dataSource = new HikariDataSource(configuration);
            if (DataSourceUtils.test(dataSource)) {
                dynamicRoutingDataSource.addDataSource(property.getId(), dataSource);
                return true;
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean removeDataSource(String id) {
        dynamicRoutingDataSource.removeDataSource(id);
        return true;
    }

    @Override
    public boolean updateDataSource(String id, DataSourceProperty property) {
        DataSource dataSource = dynamicRoutingDataSource.getDataSource(id);
        if (dataSource != null) {
            property.setId(id);
            return removeDataSource(id) && addDataSource(property);
        }
        return false;
    }

    @Override
    public boolean test(DataSourceProperty property) {
        if (property == null) {
            return false;
        }
        return DataSourceUtils.test(property);
    }
}
