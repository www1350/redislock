package com.absurd.distributedlock.db.v1;

import com.absurd.distributedlock.db.DBBaseTest;
import com.alibaba.druid.pool.DruidPooledConnection;
import org.junit.Test;

import java.sql.SQLException;
import java.util.concurrent.locks.Lock;

/**
 * @author wangwenwei
 * @time 2018/6/18
 */
public class DBLockTest extends DBBaseTest {

    @Test
    public void lock() throws InterruptedException {
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
                    Lock lock = new DBLock(connection, "mylock");
                        if (lock.tryLock()){
                            try {
                                Thread.sleep(100L);
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
