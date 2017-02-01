package com.bfb.data.shammer.pooling;

import com.bfb.data.shammer.SledgeHammerConfig;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.sql.Connection;

/**
 * Created by clong on 2/4/17.
 */
public interface SledgeHammerPool<T> {
    SledgeHammerConfig getConfig();
    PooledObjectFactory<T> getFactory();
    T borrowObject() throws Exception;
    void returnObject(T obj);
    void close();

}
