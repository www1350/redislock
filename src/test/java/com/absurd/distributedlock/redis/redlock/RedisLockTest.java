package com.absurd.distributedlock.redis.redlock;

import com.absurd.distributedlock.redis.BaseTest;
import org.junit.Test;

import java.util.concurrent.locks.Lock;

/**
 * @author wangwenwei
 * @time 2018/6/7
 */
public class RedisLockTest extends BaseTest {



    @Test
    public void lock()  {
        for(int i=0;i<1000;i++)
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Lock lock = new RedisLock(redClient, "wangwenwei:aaa:bb34", 100L);
                if (lock.tryLock()){
                    try {
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

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("aaa"+arr[0]+"ddd"+atomicInteger.get());

    }

}
