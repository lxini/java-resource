package com.lagou.edu.utils;

import com.lagou.edu.annotation.MyAutowired;
import com.lagou.edu.annotation.MyComponent;

import java.sql.SQLException;

/**
 * @author 应癫
 * <p>
 * 事务管理器类：负责手动事务的开启、提交、回滚
 */
@MyComponent
public class TransactionManager {

    @MyAutowired
    private ConnectionUtils connectionUtils;

    public void setConnectionUtils(ConnectionUtils connectionUtils) {
        this.connectionUtils = connectionUtils;
    }


    /**
     * 开启手动事务控制
     *
     * @throws SQLException
     */
    public void beginTransaction() throws SQLException {
        connectionUtils.getCurrentThreadConn().setAutoCommit(false);
    }


    /**
     * 提交事务
     *
     * @throws SQLException
     */
    public void commit() throws SQLException {
        connectionUtils.getCurrentThreadConn().commit();
    }


    /**
     * 回滚事务
     *
     * @throws SQLException
     */
    public void rollback() throws SQLException {
        connectionUtils.getCurrentThreadConn().rollback();
    }
}
