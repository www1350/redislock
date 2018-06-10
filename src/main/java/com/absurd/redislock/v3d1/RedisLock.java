package com.absurd.redislock.v3d1;

import com.absurd.redislock.AbstractRedisLock;
import com.google.common.collect.Lists;
import redis.clients.jedis.Jedis;

import java.util.UUID;

/**
 * v3.1版本
 * 1、SET lockkey uuid NX PX 过期超时时间（毫秒）
 *
 * release
 * 使用lua脚本
 *  if redis.call("get",KEYS[1]) == ARGV[1] then
 *     return redis.call("del",KEYS[1])
 *  else
 *     return 0
 *  end
 *
 *  ARGV参数为加锁的时候设置的id
 *
 *  问题：
 *  由于Redis集群数据同步为异步，假设在Master节点获取到锁后未完成数据同步情况下Master节点crash，
 *  此时在新的Master节点依然可以获取锁，所以多个Client同时获取到了锁
 * @author wangwenwei
 * @time 2018/6/7
 */
public class RedisLock extends AbstractRedisLock{
    private final Jedis client;

    private final String name;

    private volatile String uuid = null;

    private static String releaseLua = "      if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n" +
            "          return redis.call(\"del\",KEYS[1])\n" +
            "      else\n" +
            "          return 0\n" +
            "      end";

    protected final long lockExpire;//锁的有效时长(毫秒)

    public RedisLock(Jedis client, String name, long lockExpire) {
        this.client = client;
        this.name = name;
        this.lockExpire = lockExpire;
    }


    @Override
    public void unlock0() {
        client.eval(releaseLua, Lists.newArrayList(name), Lists.newArrayList(uuid));
    }

    @Override
    protected boolean tryRedisLock() {
        Thread current = Thread.currentThread();
        uuid = UUID.randomUUID().toString();
        if("OK".equals(client.set(name, uuid, "nx", "px", lockExpire))){
            locked = true;
            setExclusiveOwnerThread(current);
            return true;
        } else {
            return false;
        }


    }


}
