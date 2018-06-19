package com.absurd.distributedlock.db.v2;

import com.absurd.distributedlock.AbstractLock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author wangwenwei
 * @time 2018/6/17
 */
public class DBLock extends AbstractLock{
    private final Connection connection;

    private final String lockName;

    private final static String FOR_UPDATE_LOCK_SQL = "SELECT * FROM `lock` WHERE `name`= ? FOR UPDATE;";

    public DBLock(Connection connection, String lockName) {
        this.connection = connection;
        this.lockName = lockName;
    }


    @Override
    protected boolean tryRedisLock() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        boolean flag = false;
        Thread current = Thread.currentThread();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(FOR_UPDATE_LOCK_SQL);
            preparedStatement.setString(1 ,lockName);
            flag = preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (flag){
            locked = true;
            setExclusiveOwnerThread(current);
            return true;
        }
        return false;

    }

    @Override
    protected void unlock0() {
        try {
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
