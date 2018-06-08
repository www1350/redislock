package com.absurd.redislock.v1d1;

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
