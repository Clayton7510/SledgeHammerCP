package com.bfb.data.shammer.pooling;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.sql.Connection;

/**
 * Created by clong on 1/29/17.
 */
public abstract class SledgeHammerPoolAwareFactory<T> implements PooledObjectFactory<T> {
    private SledgeHammerPool<T> _pool = null;

    protected void setPool(SledgeHammerPool<T> pool) {
        _pool = pool;
    }
    protected SledgeHammerPool<T> getPool() { return _pool; }
}
