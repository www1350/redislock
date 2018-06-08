package com.absurd.redislock.v1;

import com.absurd.redislock.AbstractRedisLock;
import redis.clients.jedis.Jedis;

/**
 * @author wangwenwei
 * @time 2018/6/7
 */
public class RedisLock extends AbstractRedisLock {
    private final Jedis client;

    private final String name;

    protected final long lockExpire;//锁的有效时长(毫秒)


    public RedisLock(Jedis client, String name, long lockExpire) {
        this.client = client;
        this.name = name;
        this.lockExpire = lockExpire;
    }


    @Override
    public void unlock0() {
        client.del(name);
    }

    @Override
    protected boolean tryRedisLock() {
        Thread current = Thread.currentThread();
        if ( 1 == client.setnx(name, String.valueOf(current.getId()))) {
            locked = true;
            setExclusiveOwnerThread(current);
            client.pexpire(name, lockExpire);
            return true;
        }else{
            return false;
        }
    }


}
