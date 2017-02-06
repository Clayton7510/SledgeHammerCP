package com.deliveredtechnologies.shammer.pooling;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectState;

import java.io.PrintWriter;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.Optional;

import static org.apache.commons.pool2.PooledObjectState.*;

/**
 * Created by clong on 1/29/17.
 */
public class SledgeHammerPooledConnection implements PooledObject<Connection> {
    private Connection _connection;
    private long _createTime = System.currentTimeMillis();
    private long _idleTime = 0l;
    private long _lastIdleTime = 0l;
    private long _lastBorrowTime = _createTime;
    private long _lastReturnTime = _createTime;
    private long _lastUsedTime = _createTime;
    private boolean _logAbandoned = false;
    private Exception _usedBy;
    private Exception _borrowedBy;


    private PooledObjectState _state = IDLE;
    private PooledObjectState _lastState = IDLE;

    public SledgeHammerPooledConnection(Connection connection) {
        _connection = connection;
    }

    @Override
    public Connection getObject() {
        return _connection;
    }

    @Override
    public long getCreateTime() {
        return _createTime;
    }

    @Override
    public long getActiveTimeMillis() {
        if (_lastReturnTime > _lastBorrowTime) {
            return _lastReturnTime - _lastBorrowTime;
        }
        return System.currentTimeMillis() - _lastBorrowTime;
    }

    @Override
    public long getIdleTimeMillis() {
        long diff = System.currentTimeMillis() - _lastReturnTime;
        return diff >= 0 ? diff : 0;
    }

    @Override
    public long getLastBorrowTime() {
        return _lastBorrowTime;
    }

    @Override
    public long getLastReturnTime() {
        return _lastReturnTime;
    }

    @Override
    public long getLastUsedTime() {
        return _lastUsedTime;
    }

    @Override
    public int compareTo(PooledObject<Connection> other) {
        long lastActiveDiff = this.getLastReturnTime() - other.getLastReturnTime();
        if (lastActiveDiff == 0) {
            return System.identityHashCode(this) - System.identityHashCode(other);
        }
        return (int)Math.min(Math.max(lastActiveDiff, Integer.MIN_VALUE), Integer.MAX_VALUE);
    }

    @Override
    public boolean startEvictionTest() {
        if (_state == IDLE) {
            _lastState = _state;
            _state = EVICTION;
            return true;
        }
        return false;
    }

    @Override
    public boolean endEvictionTest(Deque<PooledObject<Connection>> idleQueue) {
        if (_state == EVICTION || _state == EVICTION_RETURN_TO_HEAD) {
            _lastState = _state;
            _state = IDLE;
            return true;
        }
        return false;
    }

    @Override
    public boolean allocate() {
        switch (_state) {
            case INVALID:
                if (_lastState == INVALID) {
                    return false;
                }
            case IDLE:
                _lastState = _state;
                _state = ALLOCATED;
                _lastUsedTime = _lastBorrowTime = System.currentTimeMillis();
                if (_logAbandoned) {
                    _borrowedBy = new AbandonedObjectCreatedException();
                }
                return true;
            case EVICTION:
                _lastState = _state;
                _state = EVICTION_RETURN_TO_HEAD;
                return false;
        }
        return false;
    }

    @Override
    public boolean deallocate() {
        if (_state == ALLOCATED || _state == RETURNING) {
            _lastState = _state;
            _state = IDLE;
            _lastReturnTime = System.currentTimeMillis();
            _borrowedBy = null;
            return true;
        }
        return false;
    }

    @Override
    public void invalidate() {
        _lastState = _state;
        _state = INVALID;
    }

    @Override
    public void setLogAbandoned(boolean logAbandoned) {
        _logAbandoned = logAbandoned;
    }

    @Override
    public void use() {
        _lastUsedTime = System.currentTimeMillis();
        _usedBy = new Exception("Last code to use this object");
    }

    @Override
    public void printStackTrace(PrintWriter writer) {
        boolean flush = false;
        if (Optional.ofNullable(_borrowedBy).isPresent()) {
            _borrowedBy.printStackTrace(writer);
            flush = true;
        }

        if (Optional.ofNullable(_usedBy).isPresent()) {
            _usedBy.printStackTrace(writer);
            flush = true;
        }

        if (flush) {
            writer.flush();
        }
    }

    @Override
    public PooledObjectState getState() {
        return _state;
    }

    @Override
    public void markAbandoned() {
        _lastState = _state;
        _state = ABANDONED;
    }

    @Override
    public void markReturning() {
        _lastState = _state;
        _state = RETURNING;
    }

    static class AbandonedObjectCreatedException extends Exception {
        private static final long serialVersionUID = 7398692158058772916L;
        private static final SimpleDateFormat format = new SimpleDateFormat
                ("'Pooled object created' yyyy-MM-dd HH:mm:ss Z " +
                 "'by the following code has not been returned to the pool:'");
        private final long _objCreatedTime;
        public AbandonedObjectCreatedException() {
            super();
            _objCreatedTime = System.currentTimeMillis();
        }

        @Override
        public String getMessage() {
            String msg;
            synchronized(format) {
                msg = format.format(new Date(_objCreatedTime));
            }
            return msg;
        }
    }
}
