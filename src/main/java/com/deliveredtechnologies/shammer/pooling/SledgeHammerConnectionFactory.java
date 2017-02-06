package com.deliveredtechnologies.shammer.pooling;

import com.deliveredtechnologies.shammer.SledgeHammerConfig;
import com.deliveredtechnologies.shammer.util.DriverDataSource;
import com.deliveredtechnologies.shammer.util.DataSourceUtil;
import com.deliveredtechnologies.shammer.util.PropertyUtil;
import org.apache.commons.pool2.PooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.Properties;

/**
 * Created by clong on 1/29/17.
 */
public class SledgeHammerConnectionFactory extends SledgeHammerPoolAwareFactory<Connection> {

    private static final Logger LOG = LoggerFactory.getLogger(SledgeHammerConnectionFactory.class);

    private SledgeHammerConfig _config;
    private DataSource _dataSource = null;

    public SledgeHammerConnectionFactory(SledgeHammerConfig config) {
        _config = config;
    }

    @Override
    public PooledObject<Connection> makeObject() throws Exception {
        if (Optional.ofNullable(getDataSource()).isPresent()) {
            LOG.info("Creating Connection object for pool '" + _config.getPoolName() + "'");
            Connection connection = new SledgeHammerConnection(getDataSource().getConnection(), getPool());
            PooledObject<Connection> pooledObject = new SledgeHammerPooledConnection(connection);
            return pooledObject;
        }
        else {
            throw new Exception("DataSource not initialized!");
        }
    }

    @Override
    public void destroyObject(PooledObject<Connection> p) throws Exception {
        LOG.info("Destroying Connection object in pool '" + _config.getPoolName() + "'");
        if (!p.getObject().isClosed()) {
            LOG.debug("Closing Connection in pool '" + _config.getPoolName() + "'");
            p.getObject().close();
        }
        p = null;
    }

    @Override
    public boolean validateObject(PooledObject<Connection> p) {
        LOG.debug("Validating object using '" + _config.getValidationQuery() + "' for pool '" + _config.getPoolName() + "'");
        try (Statement stmt = p.getObject().createStatement()) {
            stmt.executeQuery(_config.getValidationQuery());
            LOG.debug("Validation PASSED for pool '" + _config.getPoolName() + "'");
            return true;
        }
        catch (SQLException e) {
            LOG.warn("Validation FAILED for pool '" + _config.getPoolName() + "'", e);
            return false;
        }
    }

    @Override
    public void activateObject(PooledObject<Connection> p) throws Exception {
        //intentionally left blank
    }

    @Override
    public void passivateObject(PooledObject<Connection> p) throws Exception {
        //intentionally left blank
    }

    public DataSource getDataSource() {
        if (Optional.ofNullable(_dataSource).isPresent()) {
            return _dataSource;
        }

        synchronized (this) {
            initDataSource();
            return _dataSource;
        }
    }

    private void initDataSource() {
        final String jdbcUrl = _config.getJdbcUrl();
        final String username = _config.getUsername();
        final String password = _config.getPassword();
        final String dsClassName = _config.getDataSourceClassName();
        final String driverClassName = _config.getDriverClassName();
        final Properties dataSourceProperties = _config.getDataSourceProperties();

        if (dsClassName != null) {
            _dataSource = DataSourceUtil.createInstance(dsClassName, DataSource.class);
            PropertyUtil.setTargetFromProperties(_dataSource, dataSourceProperties);
        }
        else if (Optional.ofNullable(jdbcUrl).isPresent()) {
            _dataSource = new DriverDataSource(jdbcUrl, driverClassName, dataSourceProperties, username, password);
        }
    }
}
