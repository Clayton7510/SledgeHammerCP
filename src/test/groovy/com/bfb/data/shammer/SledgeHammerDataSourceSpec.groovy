package com.bfb.data.shammer

import com.bfb.data.shammer.pooling.SledgeHammerConnection
import com.bfb.data.shammer.pooling.SledgeHammerConnectionFactory
import com.bfb.data.shammer.pooling.SledgeHammerConnectionPool
import com.bfb.data.shammer.pooling.SledgeHammerPool
import com.bfb.data.shammer.pooling.SledgeHammerPoolAwareFactory
import com.bfb.data.shammer.pooling.SledgeHammerPooledConnection
import spock.lang.Specification

import javax.sql.DataSource
import javax.xml.crypto.Data
import java.sql.Connection
import java.sql.SQLException

/**
 * Created by clong on 2/4/17.
 * Tests for {@link SledgeHammerDataSource}
 */
class SledgeHammerDataSourceSpec extends Specification {

    SledgeHammerConfig config
    SledgeHammerPool<Connection> pool
    SledgeHammerConnectionFactory factory
    SledgeHammerDataSource dataSource

    def setup() {
        config = new SledgeHammerConfig()
        config.setMaxTotal(1)
        config.blockWhenExhausted = false

        factory = Mock()
        pool = Spy(SledgeHammerConnectionPool, constructorArgs: [factory, config])
        dataSource = new SledgeHammerDataSource(pool)
    }

    def "getConnection() should return a connection from the pool"() {
        given:
            Connection con = Mock(Connection)

        when: "a Connection is requested"
            Connection result = dataSource.getConnection()

        then: "a Connection is borrowed from the pool"
            1 * pool.borrowObject() >> con
            result == con
    }

    def "close() on a connection from the pool should return it to the pool"() {
        given:
            Connection con = new SledgeHammerConnection(Mock(Connection), pool)
            SledgeHammerPooledConnection pcon = new SledgeHammerPooledConnection(con)

        when: "SledgeHammerDataSource is instantiated"
            Connection result = dataSource.getConnection()
            result.close()

        then: "config is validated & copied"
            1 * factory.makeObject() >> pcon
            1 * pool.returnObject(con)
            result == con
    }

    def "the pool should be limited by the maximum number of connections"() {
        given:
            Connection con = new SledgeHammerConnection(Stub(Connection), pool)
            SledgeHammerPooledConnection pcon = new SledgeHammerPooledConnection(con)

        when: "SledgeHammerDataSource is instantiated"
            Connection result = dataSource.getConnection()
            dataSource.getConnection()

        then: "config is validated & copied"
            1 * factory.makeObject() >> pcon
            result == con

        then: "throw exception on borrow with exhausted pool"
            thrown NoSuchElementException
    }

    def "closing a SledgeHammerDataSource should close the DataSource, and its pool"() {
        when: "the DataSource is closed"
            dataSource.close()

        then: "the pool is closed"
            1 * pool.close()
            dataSource.isClosed()
    }

    def "getConnection() from a closed SledgeHammerDataSource should result in an error"() {
        when: "the DataSource is closed and an exception is requested"
            dataSource.close()
            dataSource.getConnection()

        then: "the pool is closed and error is thrown"
            1 * pool.close()
            thrown SQLException
    }

    def "unwrapping a SledgeHammerDataSource to a DataSource type should return the current object"() {
        when: "the SledgeHammerDataSource object is unwrapped to a type of DataSource"
            //since ds is of type DataSource, it returns itself
            def unwrappedObj = dataSource.unwrap(DataSource)

        then: "the unwrapped object is the same as the original SlegeHammerDataSource object"
            unwrappedObj == dataSource
    }

    def "unwrapping a SledgeHammerDataSource to the DataSource type contained in the factory should return the factory's DataSource object"() {
        given:
            //a different DataSource object
            IDataSource ds = Mock()

        when: "the SledgeHammerDataSource object is unwrapped to a type of IDataSource"
            //since IDataSource is the type returned from the factory, the DataSource from the factory is returned
            def unwrappedObj = dataSource.unwrap(IDataSource)

        then: "the unwrapped object is the same as the original SledgeHammerDataSource object"
            1 * pool.getFactory() >> factory
            1 * factory.getDataSource() >> ds
            unwrappedObj == ds
    }

    def "unwrapping a SledgeHammerDataSource to an other DataSource type should recursively unwrap"() {
        given:
            //a DataSource to return from the factory
            DataSource ds = Mock()

        when: "the SledgeHammerDataSource object is unwrapped to a type of IDataSource"
            //since IDataSource isn't the type of shds or the type returned from the factory, a recursive call is made
            def unwrappedObj = dataSource.unwrap(IDataSource)

        then: "the unwrapped object is the same as the original SlegeHammerDataSource object"
            1 * pool.getFactory() >> factory
            1 * factory.getDataSource() >> ds
            1 * ds.unwrap(IDataSource) >> ds
            unwrappedObj == ds
    }

    def "isWrapperFor should be true for DataSource and SledgeHammerDataSource" () {
        when: "DataSource and SledgeHammerDataSource are checked against isWrapperFor"
            def result = dataSource.isWrapperFor(DataSource) && dataSource.isWrapperFor(SledgeHammerDataSource)

        then: "isWrapperFor is true for both SledgeHammerDataSource and DataSource"
            result
    }

    def "isWrapperFor should be true for the DataSource type in the factory" () {
        when: "the factory's DataSource type is checked against isWrapperFor"
            def result = dataSource.isWrapperFor(IDataSource)

        then: "isWrapperFor is true for the factory's DataSource type"
            1 * pool.getFactory() >> factory
            1 * factory.getDataSource() >> Stub(IDataSource)
            result
    }

    def "isWrapperFor should be true for the wrapped DataSource type in the factory" () {
        given:
            //datasources
            DataSource ds = Mock(DataSource)

        when: "the factory's DataSource type is checked against isWrapperFor"
            def result = dataSource.isWrapperFor(IDataSource)

        then: "isWrapperFor is true for the factory's DataSource type"
            1 * pool.getFactory() >> factory
            1 * factory.getDataSource() >> ds
            1 * ds.isWrapperFor(IDataSource) >> true
            result
    }
}
