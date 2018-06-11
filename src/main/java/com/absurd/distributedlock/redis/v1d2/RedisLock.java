package com.absurd.distributedlock.redis.v1d2;

import com.absurd.distributedlock.redis.AbstractRedisLock;
import com.google.common.collect.Lists;
import redis.clients.jedis.Jedis;

/**
 * v1.2版本，
 * tryLock
 * lua脚本保证原子性，防止setnx后崩溃出现的无法释放锁
 * local r = tonumber(redis.call('SETNX', KEYS[1],ARGV[1]));
 * if (locked == 1) then
 * redis.call('PEXPIRE',KEYS[1],ARGV[2]);
 * end
 * return r
 *
 * ARGV传入分别是1和过期时间
 *
 * release
 * 1、delete key
 *
 * 问题：如果Redis仅执行了一条命令后crash或者发生主从切换，依然会出现锁没有过期时间，最终导致无法释放。
 * @author wangwenwei
 * @time 2018/6/7
 */
public class RedisLock extends AbstractRedisLock {
    private final Jedis client;

    private final String name;

    protected final long lockExpire;//锁的有效时长(毫秒)

    private static String lockLuaScript = "local r = tonumber(redis.call('SETNX', KEYS[1],ARGV[1]));\n" +
            " if (r == 1) then\n" +
            "  redis.call('PEXPIRE',KEYS[1],ARGV[2]);\n" +
            " end\n" +
            " return r";


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
        Long ret = (Long) client.eval(lockLuaScript,
                Lists.newArrayList(name),
                Lists.newArrayList(String.valueOf(current.getId()), String.valueOf(lockExpire)) );
        if ( new Long(1).equals(ret)) {
            locked = true;
            setExclusiveOwnerThread(current);
            return true;
        }else{
            return false;
        }
    }


}
