package com.absurd.redislock.v3d1;

import com.absurd.redislock.AbstractRedisLock;
import com.google.common.collect.Lists;
import redis.clients.jedis.Jedis;

import java.util.UUID;

/**
 * @author wangwenwei
 * @time 2018/6/7
 */
public class RedisLock extends AbstractRedisLock{
    private final Jedis client;

    private final String name;

    private volatile String newExpireValue = null;

    protected final long lockExpire;//锁的有效时长(毫秒)

    public RedisLock(Jedis client, String name, long lockExpire) {
        this.client = client;
        this.name = name;
        this.lockExpire = lockExpire;
    }




    @Override
    public void unlock0() {
        client.eval("      if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n" +
                "          return redis.call(\"del\",KEYS[1])\n" +
                "      else\n" +
                "          return 0\n" +
                "      end", Lists.newArrayList(name), Lists.newArrayList(newExpireValue));
    }

    @Override
    protected boolean tryRedisLock() {
        Thread current = Thread.currentThread();
        newExpireValue = UUID.randomUUID().toString();
        if(1 == client.setnx(name, newExpireValue)){
            locked = true;
            setExclusiveOwnerThread(current);
            return true;
        } else {
            return false;
        }


    }


}
