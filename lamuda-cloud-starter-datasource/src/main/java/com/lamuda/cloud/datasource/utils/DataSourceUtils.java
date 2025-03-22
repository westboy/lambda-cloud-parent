package com.lamuda.cloud.datasource.utils;

import com.lamuda.cloud.datasource.property.DataSourceProperty;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.experimental.UtilityClass;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * @author w
 */
@UtilityClass
public class DataSourceUtils {
    /**
     * @param url
     * @param username
     * @param password
     * @param driverClassName
     * @return DataSource
     */
    public static DataSource getInstance(String url, String username, String password, String driverClassName) {
        HikariConfig configuration = new HikariConfig();
        configuration.setJdbcUrl(url);
        configuration.setUsername(username);
        configuration.setPassword(password);
        configuration.setDriverClassName(driverClassName);
        return new HikariDataSource(configuration);
    }

    /**
     * test
     *
     * @param property DataSourceProperty
     * @return boolean
     */
    public static boolean test(DataSourceProperty property) {
        HikariConfig configuration = new HikariConfig();
        configuration.setJdbcUrl(property.getUrl());
        configuration.setUsername(property.getUsername());
        configuration.setPassword(property.getPassword());
        configuration.setDriverClassName(property.getDriverClassName());
        configuration.setMaximumPoolSize(1);
        configuration.setMinimumIdle(1);
        configuration.setConnectionTimeout(3000);
        configuration.setReadOnly(property.isReadOnly());
        try (HikariDataSource dataSource = new HikariDataSource(configuration);
             Connection connection = dataSource.getConnection()) {
            boolean verified = connection.isValid(1000);
            if (verified) {
                DatabaseMetaData meta = connection.getMetaData();
                property.setSchema(connection.getSchema());
                property.setDatabaseId(meta.getDatabaseProductName().toLowerCase());
            }
            return verified;
        } catch (SQLException | RuntimeException e) {
            return false;
        }
    }

    /**
     * test
     *
     * @param dataSource DataSource
     * @return boolean
     */
    public static boolean test(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(1000);
        } catch (SQLException e) {
            return false;
        }
    }

}
