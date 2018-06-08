package com.absurd.redislock.v1;

import com.absurd.redislock.BaseTest;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * @author wangwenwei
 * @time 2018/6/7
 */
public class RedisLockTest extends BaseTest{

    @Test
    public void lock() throws InterruptedException {
        for(int i=0;i<1000;i++)
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Jedis jedis = jedisPool.getResource();
                Lock lock = new RedisLock(jedis, "wangwenwei:aaa:bb34", 200L);
                try {
                    if (lock.tryLock(100, TimeUnit.MILLISECONDS)){
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
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
                jedis.close();
            }
        });

        countDownLatch.await();
        System.out.println("aaa"+arr[0]+"ddd"+atomicInteger.get());

    }


    @Test
    public void lock_local() throws InterruptedException {
        for(int i=0;i<1000;i++)
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (reetLock.tryLock(2, TimeUnit.SECONDS)){
                            try {
                                Thread.sleep(100L);
                                arr[0]++;
                            } finally {
                                reetLock.unlock();
                            }
                        }else {
                            atomicInteger.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    countDownLatch.countDown();
                }
            });

        countDownLatch.await();
        System.out.println("aaa"+arr[0]+"ddd"+atomicInteger.get());

    }
}
