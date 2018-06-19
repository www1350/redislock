package com.absurd.distributedlock.db.v1;

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

    private final static  String INSERT_LOCK_SQL = "INSERT INTO `lock`(`name`,`update_time`) VALUES (?,CURRENT_TIMESTAMP);";

    private final static  String DELETE_LOCK_SQL = "DELETE FROM `lock` WHERE `name` = ?;";

    public DBLock(Connection connection, String lockName) {
        this.connection = connection;
        this.lockName = lockName;
    }

    @Override
    protected boolean tryRedisLock() {
        int count = 0;
        Thread current = Thread.currentThread();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_LOCK_SQL);
            preparedStatement.setString(1 ,lockName);
            count = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (count >0){
            locked = true;
            setExclusiveOwnerThread(current);
            return true;
        }
        return false;
    }

    @Override
    protected void unlock0() {
        int count = 0;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(DELETE_LOCK_SQL);
            preparedStatement.setString(1, lockName);
            count = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (count >0){
            return;
        }

    }
}
