package com.absurd.distributedlock.redis.v2;

import com.absurd.distributedlock.AbstractLock;
import redis.clients.jedis.Jedis;

/**
 * v2版本
 * tryLock
 * 1、SET lockkey 1 NX PX 过期超时时间（毫秒）
 *
 *
 * release
 * 1、delete key
 *
 * 问题：1. 由于C1的停顿导致C1 和C2同都获得了锁并且同时在执行，在业务实现间接要求必须保证幂等性
 * 1.1 C1成功获取到了锁，之后C1因为GC进入等待或者未知原因导致任务执行过长，最后在锁失效前C1没有主动释放锁
 * 1.2. C2在C1的锁超时后获取到锁，并且开始执行，这个时候C1和C2都同时在执行，会因重复执行造成数据不一致等未知情况
 * 1.3. C1如果先执行完毕，则会释放C2的锁，此时可能导致另外一个C3进程获取到了锁
 *
 * 2. C1释放了不属于C1的锁
 * @author wangwenwei
 * @time 2018/6/7
 */
public class RedisLock extends AbstractLock{
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
