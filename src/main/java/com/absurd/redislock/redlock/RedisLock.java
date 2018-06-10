package com.absurd.redislock.redlock;

import com.absurd.redislock.AbstractRedisLock;
import org.redisson.RedissonRedLock;
import org.redisson.api.RedissonClient;

/**
 * @author wangwenwei
 * @time 2018/6/7
 */
public class RedisLock extends AbstractRedisLock {
    private final RedissonClient client;

    private final String name;

    private RedissonRedLock redLock;

    protected final long lockExpire;//锁的有效时长(毫秒)


    public RedisLock(RedissonClient client, String name, long lockExpire) {
        this.client = client;
        this.name = name;
        redLock = new RedissonRedLock(client.getLock(this.name));
        this.lockExpire = lockExpire;
    }


    @Override
    public void unlock0() {
        redLock.unlock();
    }

    @Override
    protected boolean tryRedisLock() {
        Thread current = Thread.currentThread();

        if ( redLock.tryLock()) {
            locked = true;
            setExclusiveOwnerThread(current);
            return true;
        }else{
            return false;
        }
    }


}
