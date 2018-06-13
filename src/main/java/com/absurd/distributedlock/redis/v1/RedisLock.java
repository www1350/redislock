package com.absurd.distributedlock.redis.v1;

import com.absurd.distributedlock.AbstractLock;
import redis.clients.jedis.Jedis;

/**
 * v1版本，
 * tryLock
 * 1、setnx key 1
 * 2、pexpire lockExpire
 *
 * release
 * 1、delete key
 *
 * 问题：setnx后崩溃导致锁一直无法释放
 * @author wangwenwei
 * @time 2018/6/7
 */
public class RedisLock extends AbstractLock {
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
