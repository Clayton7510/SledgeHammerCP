package com.deliveredtechnologies.shammer;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.Properties;

/**
 * Created by clong on 1/29/17.
 * JavaBean configuration for SledgeHammerCP
 */
public class SledgeHammerConfig extends GenericObjectPoolConfig {

    private String _poolName;
    private String _jdbcUrl;
    private String _username;
    private String _password;
    private String _dataSourceClassName;
    private String _driverClassName;
    private Properties _dataSourceProperties;
    private String _validationQuery;

    public boolean validate() {
        //TODO: complete validate method
        return true;
    }

    public void copyState(SledgeHammerDataSource dataSource) {
        //TODO: complete copyState
    }

    public String getPoolName() {
        return _poolName;
    }

    public void setPoolName(String _poolName) {
        this._poolName = _poolName;
    }

    public String getJdbcUrl() {
        return _jdbcUrl;
    }

    public void setJdbcUrl(String _jdbcUrl) {
        this._jdbcUrl = _jdbcUrl;
    }

    public String getUsername() {
        return _username;
    }

    public void setUsername(String _username) {
        this._username = _username;
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(String _password) {
        this._password = _password;
    }

    public String getDataSourceClassName() {
        return _dataSourceClassName;
    }

    public void setDataSourceClassName(String _dataSourceClassName) {
        this._dataSourceClassName = _dataSourceClassName;
    }

    public String getDriverClassName() {
        return _driverClassName;
    }

    public void setDriverClassName(String _driverClassName) {
        this._driverClassName = _driverClassName;
    }

    public Properties getDataSourceProperties() {
        return _dataSourceProperties;
    }

    public void setDataSourceProperties(Properties _dataSourceProperties) {
        this._dataSourceProperties = _dataSourceProperties;
    }

    public String getValidationQuery() {
        return _validationQuery;
    }

    public void setValidationQuery(String _validationQuery) {
        this._validationQuery = _validationQuery;
    }
}
