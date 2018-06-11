package com.absurd.distributedlock.redis;

import org.junit.Before;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author wangwenwei
 * @time 2018/6/9
 */
public class BaseTest {
    protected JedisPool jedisPool = null;

    protected RedissonClient redClient = null;

    protected ExecutorService executorService = Executors.newCachedThreadPool();

    protected CountDownLatch countDownLatch = new CountDownLatch(1000);

    protected volatile static Integer[] arr = {0};

    protected static AtomicInteger atomicInteger = new AtomicInteger(0);

    protected Lock reetLock = new ReentrantLock();

    @Before
    public void setUp(){
        JedisPoolConfig config = new JedisPoolConfig();
        // 控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；
        // 如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
        config.setMaxTotal(10000);
        // 控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
        config.setMaxIdle(2000);
        // 表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
        config.setMaxWaitMillis(1000 * 100);
        config.setTestOnBorrow(true);
        jedisPool = new JedisPool(config,"localhost",6379);


        Config config2 = new Config();
        config2.useSingleServer()
                .setAddress("redis://127.0.0.1:6379");
        redClient = Redisson.create(config2);
    }

}
