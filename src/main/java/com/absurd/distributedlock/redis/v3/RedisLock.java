package com.absurd.distributedlock.redis.v3;

import com.absurd.distributedlock.AbstractRedisLock;
import com.google.common.collect.Lists;
import redis.clients.jedis.Jedis;

/**
 * v3 版本
 * 1、SET lockkey 当前时间+过期超时时间 NX PX 过期超时时间（毫秒）
 *
 *
 * release
 * 使用lua脚本
 *  if redis.call("get",KEYS[1]) == ARGV[1] then
 *     return redis.call("del",KEYS[1])
 *  else
 *     return 0
 *  end
 *
 *  ARGV参数为加锁的时候设置的时间戳
 *
 *  问题：如果在并发极高的场景下，可能存在UnixTimestamp重复问题
 *
 * @author wangwenwei
 * @time 2018/6/7
 */
public class RedisLock extends AbstractRedisLock {
    private final Jedis client;

    private final String name;

    private volatile String newExpireValue = null;

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
        client.eval(releaseLua, Lists.newArrayList(name), Lists.newArrayList(newExpireValue));
    }

    @Override
    protected boolean tryRedisLock() {
        Thread current = Thread.currentThread();
        long newExpireTime=System.currentTimeMillis()+lockExpire +1;
        newExpireValue = String.valueOf(newExpireTime);
        if("OK".equals(client.set(name, newExpireValue, "nx", "px", lockExpire))){
            locked = true;
            setExclusiveOwnerThread(current);
            return true;
        } else {
            return false;
        }


    }


}
