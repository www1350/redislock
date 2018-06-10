package com.absurd.redislock.v2;

import com.absurd.redislock.AbstractRedisLock;
import redis.clients.jedis.Jedis;

/**
 * @author wangwenwei
 * @time 2018/6/7
 */
public class RedisLock extends AbstractRedisLock{
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
        if("OK" .equals( client.set(name, String.valueOf(current.getId()), "nx", "px", lockExpire))){
            locked = true;
            setExclusiveOwnerThread(current);
            return true;
        } else{
            return false;
        }


    }


}
