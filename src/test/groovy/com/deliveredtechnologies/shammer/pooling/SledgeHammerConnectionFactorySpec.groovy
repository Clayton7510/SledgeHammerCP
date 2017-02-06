package com.deliveredtechnologies.shammer.pooling

import com.deliveredtechnologies.shammer.SledgeHammerConfig
import org.apache.commons.pool2.PooledObject
import spock.lang.Specification

import javax.sql.DataSource
import java.sql.Connection
import java.sql.Statement

/**
 * Created by clong on 2/4/17.
 * Tests for {@link SledgeHammerConnectionFactory}
 */
class SledgeHammerConnectionFactorySpec extends Specification {
    SledgeHammerConfig config
    SledgeHammerConnectionPool pool
    SledgeHammerConnectionFactory factory

    def setup() {
        config = new SledgeHammerConfig()
        config.validationQuery = 'SELECT 1;'

        factory = Spy(SledgeHammerConnectionFactory, constructorArgs: [config])

        pool = Spy(SledgeHammerConnectionPool, constructorArgs: [factory, config])
    }

    def "makeObject should create a PooledObject Connection"() {
        given:
            DataSource ds = Mock()
            Connection con = Mock()

        when: "makeObject is called"
            PooledObject<Connection> obj = factory.makeObject()

        then: "a SledgeHammerConnection proxy containing the Connection is returned in a SledgeHammerPooledConnection object"
            2 * factory.getDataSource() >> ds
            1 * ds.getConnection() >> con
            obj instanceof SledgeHammerPooledConnection
            obj.getObject() instanceof SledgeHammerConnection
            obj.getObject().unwrap(Connection) == con
    }

    def "makeObject should throw an exception when the DataSource can't be created"() {
        when: "makeObject is called"
            factory.makeObject()

        then: "an exception is thrown"
            1 * factory.getDataSource() >> null
            thrown Exception
    }

    def "destroyObject should close the Connection"() {
        given:
            PooledObject<Connection> obj = Mock()
            Connection con = Mock()

        when: "destroyObject is called"
            factory.destroyObject(obj)

        then: "the Connection is closed"
            2 * obj.getObject() >> con
            1 * con.isClosed() >> false
            1 * con.close()
    }

    def "validateObject should execute the SQL statement supplied by the config"() {
        given:
            Connection con = Mock()
            PooledObject<Connection> obj = Mock()
            Statement stmt = Mock()

        when: "validateObject is called"
            def result = factory.validateObject(obj)

        then: "the SQL statement supplied by the config is executed against the connection"
            1 * obj.getObject() >> con
            1 * con.createStatement() >> stmt
            1 * stmt.executeQuery(config.validationQuery)
            result
    }
}
