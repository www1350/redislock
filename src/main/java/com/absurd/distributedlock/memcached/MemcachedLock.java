package com.absurd.distributedlock.memcached;

import com.absurd.distributedlock.AbstractLock;
import net.rubyeye.xmemcached.XMemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;

import java.util.concurrent.TimeoutException;

/**
 * @author wangwenwei
 * @time 2018/6/13
 */
public class MemcachedLock extends AbstractLock {

    private final XMemcachedClient client;

    private final String name;

    protected final int lockExpire;//锁的有效时长(秒)

    public MemcachedLock(XMemcachedClient client, String name, int lockExpire) {
        this.client = client;
        this.name = name;
        this.lockExpire = lockExpire;
    }


    @Override
    protected boolean tryRedisLock() {
        Thread current = Thread.currentThread();
        try {
           boolean flag = client.add(name, lockExpire, 1);
            if (flag){
                locked = true;
                setExclusiveOwnerThread(current);
                return true;
            } else {
                return false;
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MemcachedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void unlock0() {
        try {
            client.delete(name);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MemcachedException e) {
            e.printStackTrace();
        }
    }
}
