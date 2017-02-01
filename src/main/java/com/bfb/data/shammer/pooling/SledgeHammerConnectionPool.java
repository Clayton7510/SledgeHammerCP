package com.bfb.data.shammer.pooling;

import com.bfb.data.shammer.SledgeHammerConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.sql.Connection;

/**
 * Created by clong on 1/29/17.
 */
public class SledgeHammerConnectionPool extends GenericObjectPool<Connection> implements SledgeHammerPool<Connection>{

    private SledgeHammerConfig _config;

    public SledgeHammerConnectionPool(SledgeHammerPoolAwareFactory factory, SledgeHammerConfig config) {
        super(factory, config);
        factory.setPool(this);
        _config = config;
    }

    @Override
    public SledgeHammerConfig getConfig() {
        return _config;
    }
}
