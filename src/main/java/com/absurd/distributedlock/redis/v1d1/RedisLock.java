package com.absurd.distributedlock.redis.v1d1;

import com.absurd.distributedlock.AbstractLock;
import redis.clients.jedis.Jedis;

/**
 * v1.1版本
 * tryLock
 * 1. setnx(lockkey, 当前时间+过期超时时间) ，如果返回1，则获取锁成功；如果返回0则没有获取到锁，转向2。
 * 2. get(lockkey)获取值oldExpireTime ，并将这个value值与当前的系统时间进行比较，如果小于当前系统时间，则认为这个锁已经超时，可以允许别的请求重新获取，转向3。
 * 3. 计算newExpireTime=当前时间+过期超时时间，然后getset(lockkey, newExpireTime) 会返回当前lockkey的值currentExpireTime。
 * 4. 判断currentExpireTime与oldExpireTime 是否相等，如果相等，说明当前getset设置成功，获取到了锁。如果不相等，说明这个锁又被别的请求获取走了，那么当前请求可以直接返回失败，或者继续重试。
 * 5. 在获取到锁之后，当前线程可以开始自己的业务处理，当处理完毕后，比较自己的处理时间和对于锁设置的超时时间，如果小于锁设置的超时时间，则直接执行delete释放锁；如果大于锁设置的超时时间，则不需要再锁进行处理。
 *
 * release
 * 1、delete key
 * 问题：
 * 1. 在锁竞争较高的情况下，会出现Value不断被覆盖，但是没有一个Client获取到锁。
 * 2. 在获取锁的过程中不断的修改原有锁的数据，设想一种场景C1，C2竞争锁，C1获取到了锁，
 * C2锁执行了GETSET操作修改了C1锁的过期时间，如果C1没有正确释放锁，锁的过期时间被延长，其它Client需要等待更久的时间。
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
        long newExpireTime=System.currentTimeMillis()+lockExpire +1;
        if(1 == client.setnx(name, String.valueOf(newExpireTime))){
            locked = true;
            setExclusiveOwnerThread(current);
            return true;
        } else{
            String oldExpireTime = client.get(name);
            long currentTimestamp = System.currentTimeMillis();
            //很奇怪，这里没有判空就会卡死在这里
            if(oldExpireTime == null || Long.valueOf(oldExpireTime) < currentTimestamp){
                newExpireTime= currentTimestamp + lockExpire +1;
                String currentExpireTime=client.getSet(name,String.valueOf(newExpireTime));
                if(currentExpireTime == null && oldExpireTime ==  null || currentExpireTime.equals(oldExpireTime)){
                    locked = true;
                    setExclusiveOwnerThread(current);
                    return true;
                }else{
                    return false;
                }
            } else {
                return false;
            }
        }


    }


}
