package com.absurd.distributedlock.db;

import com.absurd.distributedlock.BaseTest;
import com.alibaba.druid.pool.DruidDataSource;
import org.junit.After;
import org.junit.Before;

import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

/**
 * @author wangwenwei
 * @time 2018/6/19
 */
public class DBBaseTest extends BaseTest{
    protected CountDownLatch countDownLatch = new CountDownLatch(10);



    protected static DruidDataSource druidDataSource = null;

    @Before
    public void setUp(){
        druidDataSource = new DruidDataSource();
        druidDataSource.setAsyncInit(false);
        druidDataSource.setUrl("jdbc:mysql://127.0.0.1:3306/mylock");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("123456");
        druidDataSource.setMaxActive(20);
        druidDataSource.setInitialSize(10);
        druidDataSource.setMinIdle(10);
        druidDataSource.setMaxWait(6000L);
        druidDataSource.setTestOnBorrow(true);
        druidDataSource.setTestOnReturn(true);
        druidDataSource.setPoolPreparedStatements(true);
        druidDataSource.setMinEvictableIdleTimeMillis(300000L);
        druidDataSource.setTimeBetweenEvictionRunsMillis(60000L);
        druidDataSource.setMaxOpenPreparedStatements(20);
        try {
            druidDataSource.init();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    @After
    public void destory(){
        druidDataSource.close();
    }
}
