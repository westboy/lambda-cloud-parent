package com.lamuda.cloud.datasource.property;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author w
 */
@Data
@NoArgsConstructor
public class DataSourceProperty {

    private String id;
    private String url;
    private String username;
    private String password;
    private String driverClassName;

    private String databaseId;
    private String schema;

    private boolean isReadOnly;

    public void setJdbcUrl(String url) {
        this.url = url;
    }
}
