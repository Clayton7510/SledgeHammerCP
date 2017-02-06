package com.deliveredtechnologies.shammer.pooling

import com.deliveredtechnologies.shammer.SledgeHammerConfig
import org.apache.commons.pool2.PooledObject
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import spock.lang.Specification

import java.sql.Connection

/**
 * Created by clong on 2/4/17.
 * Tests for {@link SledgeHammerConnectionPool}
 */
class SledgeHammerConnectionPoolSpec extends Specification {
    GenericObjectPoolConfig config
    SledgeHammerPoolAwareFactory<Connection> factory
    SledgeHammerConnectionPool pool

    def setup() {
        config = new SledgeHammerConfig()
        config.maxTotal = 1
        config.blockWhenExhausted = false
        config.testOnBorrow = true
        config.testOnReturn = true
        config.testOnCreate = true

        factory = Mock()
        pool = new SledgeHammerConnectionPool(factory, config)
    }

    def "onBorrow should validate the Connection object and create one if it doesn't exist"() {
        given:
            PooledObject<Connection> pooledObject = new SledgeHammerPooledConnection(Stub(Connection))

        when:
            def obj = pool.borrowObject()

        then:
            1 * factory.makeObject() >> pooledObject
            1 * factory.validateObject(pooledObject) >> true
            1 * factory.activateObject(pooledObject)
    }

    def "onReturn should validate the Connection object and return it to the pool"() {
        given:
            PooledObject<Connection> pooledObject = new SledgeHammerPooledConnection(Stub(Connection))

        when: "a Connection object is borrowed and returned"
            def obj = pool.borrowObject()
            pool.returnObject(obj)

        then: "the Connection object is validated onBorrow and onReturn"
            1 * factory.makeObject() >> pooledObject
            1 * factory.activateObject(pooledObject)
            2 * factory.validateObject(pooledObject) >> true
            1 * factory.passivateObject(pooledObject)
    }

    def "the Connection object in the pool should be destroyed if onReturn validation fails"() {
        given:
            PooledObject<Connection> pooledObject = new SledgeHammerPooledConnection(Stub(Connection))

        when: "a Connection object is borrowed and is returned"
            def obj = pool.borrowObject()
            pool.returnObject(obj)

        then: "the Connection object passes validation onBorrow"
            1 * factory.makeObject() >> pooledObject
            1 * factory.activateObject(pooledObject)
            1 * factory.validateObject(pooledObject) >> true

        then: "the Connection object fails validation onReturn and is destroyed"
            1 * factory.validateObject(pooledObject) >> false
            1 * factory.destroyObject(pooledObject)
    }

    def "the Connection object in the pool should be destroyed if onBorrow validation fails"() {
        given:
            PooledObject<Connection> pooledObject = new SledgeHammerPooledConnection(Stub(Connection))

        when: "a Connection object is borrowed and is returned"
            def obj = pool.borrowObject()
            pool.returnObject(obj)
            pool.borrowObject()

        then: "the Connection object passes validation onBorrow"
            1 * factory.makeObject() >> pooledObject
            1 * factory.activateObject(pooledObject)
            1 * factory.validateObject(pooledObject) >> true

        then: "the Connection object passes validation onReturn"
            1 * factory.validateObject(pooledObject) >> true
            1 * factory.passivateObject(pooledObject)

        then: "the Connection object fails validation onBorrow and is destroyed"
            1 * factory.activateObject(pooledObject)
            1 * factory.validateObject(pooledObject) >> false
            1 * factory.destroyObject(pooledObject)

        then: "a new Connection object is created and added to the pool"
            1 * factory.makeObject() >> pooledObject
            1 * factory.activateObject(pooledObject)
            1 * factory.validateObject(pooledObject) >> true
    }

    def "if Connection validation fails twice onBorrow then an error should be thrown"() {
        given:
            PooledObject<Connection> pooledObject = new SledgeHammerPooledConnection(Stub(Connection))

        when: "a Connection object is borrowed and is returned"
            def obj = pool.borrowObject()
            pool.returnObject(obj)
            pool.borrowObject()

        then: "the Connection object passes validation onBorrow"
            1 * factory.makeObject() >> pooledObject
            1 * factory.activateObject(pooledObject)
            1 * factory.validateObject(pooledObject) >> true

        then: "the Connection object passes validation onReturn"
            1 * factory.validateObject(pooledObject) >> true
            1 * factory.passivateObject(pooledObject)

        then: "the Connection object fails validation onBorrow and is destroyed"
            1 * factory.activateObject(pooledObject)
            1 * factory.validateObject(pooledObject) >> false
            1 * factory.destroyObject(pooledObject)

        then: "a new Connection object is created, fails validation and an error is thrown"
            1 * factory.makeObject() >> pooledObject
            1 * factory.activateObject(pooledObject)
            1 * factory.validateObject(pooledObject) >> false
            1 * factory.destroyObject(pooledObject)
            thrown NoSuchElementException
    }
}
