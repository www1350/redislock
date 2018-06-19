package com.absurd.distributedlock.db.v2;

import com.absurd.distributedlock.db.DBBaseTest;
import com.alibaba.druid.pool.DruidPooledConnection;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;

/**
 * @author wangwenwei
 * @time 2018/6/18
 */
public class DBLockTest extends DBBaseTest {

    @Test
    public void lock() throws InterruptedException {
        try {
            DruidPooledConnection  connection = druidDataSource.getConnection();
            PreparedStatement preparedStatement =  connection.prepareStatement("INSERT INTO `lock`(`name`,`update_time`) VALUES (?,CURRENT_TIMESTAMP);");
            preparedStatement.setString(1, "mylock-2");
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for(int i=0;i<10;i++)
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    DruidPooledConnection connection = null;
                    try {
                        connection = druidDataSource.getConnection();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    Lock lock = new DBLock(connection, "mylock-2");
                        if (lock.tryLock()){
                            try {
                                Thread.sleep(1000L);
                                arr[0]++;
                            }catch (Exception e){
                                e.printStackTrace();
                            } finally {
                                lock.unlock();
                            }
                        }else {
                            atomicInteger.incrementAndGet();
                        }
                    countDownLatch.countDown();
                }
            });

        countDownLatch.await();
        System.out.println("aaa"+arr[0]+"ddd"+atomicInteger.get());

    }
}
