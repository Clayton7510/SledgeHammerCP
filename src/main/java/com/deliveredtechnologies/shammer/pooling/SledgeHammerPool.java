package com.deliveredtechnologies.shammer.pooling;

import com.deliveredtechnologies.shammer.SledgeHammerConfig;
import org.apache.commons.pool2.PooledObjectFactory;

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
