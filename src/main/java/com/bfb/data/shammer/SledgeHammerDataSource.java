package com.bfb.data.shammer;

import com.bfb.data.shammer.pooling.SledgeHammerConnectionFactory;
import com.bfb.data.shammer.pooling.SledgeHammerConnectionPool;
import com.bfb.data.shammer.pooling.SledgeHammerPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by clong on 1/29/17.
 */
public class SledgeHammerDataSource implements DataSource, Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(SledgeHammerDataSource.class);

    private AtomicBoolean _isClosed = new AtomicBoolean(false);
    private SledgeHammerPool<Connection> _pool;
    private int _loginTimeout = 0;

    public SledgeHammerDataSource(SledgeHammerPool<Connection> pool) {
        _pool = pool;
    }

    public SledgeHammerDataSource(SledgeHammerConfig config) {
        this(new SledgeHammerConnectionPool(new SledgeHammerConnectionFactory(config), config));
    }

    @Override
    public void close() throws IOException {
        LOG.info("Closing DataSource and Connection pool '" + _pool.getConfig() + "'");
        if (_isClosed.getAndSet(true)) {
            return;
        }

        _pool.close();
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (_isClosed.get()) {
            throw new SQLException("SledgeHammerDataSource " + this + " has been closed!");
        }

        if (Optional.ofNullable(_pool).isPresent()) {
            try {
                LOG.debug("Borrowing a Connection from the pool '" + _pool.getConfig().getPoolName() + "'");
                return _pool.borrowObject();
            }
            catch (NoSuchElementException nse) {
                throw nse;
            }
            catch (Exception e) {
                throw new SQLException("Unable to borrow from SledgeHammerConnectionPool", e);
            }
        }
        throw new SQLException("SledgeHammerConnectionPool connection pool is null!");
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }

        DataSource ds = ((SledgeHammerConnectionFactory)_pool.getFactory()).getDataSource();
        if (iface.isInstance(ds)) {
            return (T)ds;
        }

        return ds.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return true;
        }

        DataSource ds = ((SledgeHammerConnectionFactory)_pool.getFactory()).getDataSource();
        if (iface.isInstance(ds)) {
            return true;
        }

        return ds.isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        if (Optional.ofNullable(_pool).isPresent()) {
            return ((SledgeHammerConnectionFactory)_pool.getFactory()).getDataSource().getLogWriter();
        }
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        if (Optional.ofNullable(_pool).isPresent()) {
            ((SledgeHammerConnectionFactory)_pool.getFactory()).getDataSource().setLogWriter(out);
        }
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        _loginTimeout = seconds;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return _loginTimeout;
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    public boolean isClosed() {
        return _isClosed.get();
    }
}
