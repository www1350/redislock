package com.absurd.distributedlock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author wangwenwei
 * @time 2018/6/8
 */
public abstract class AbstractLock implements Lock{


    /**
     * 当前jvm内持有该锁的线程(if have one)
     */
    protected Thread exclusiveOwnerThread;

    protected boolean locked;

    @Override
    public void lock() {
        try {
            lockInterruptibly();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        lock(false, 0, null, true);
    }

    @Override
    public boolean tryLock() {
        try {
            return lock(true, -1, null, true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }


    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return lock(true, time, unit, true);
    }

    @Override
    public void unlock() {
        // 检查当前线程是否持有锁
        if (Thread.currentThread() != getExclusiveOwnerThread()) {
            throw new IllegalMonitorStateException("current thread does not hold the lock");
        }
        unlock0();
        setExclusiveOwnerThread(null);
    }

    protected void checkInterruption() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }

    protected Thread getExclusiveOwnerThread() {
        return exclusiveOwnerThread;
    }

    protected void setExclusiveOwnerThread(Thread exclusiveOwnerThread) {
        this.exclusiveOwnerThread = exclusiveOwnerThread;
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException("newCondition不支持");
    }

    /****
     *
     * @param useTimeout 是否使用超时时间
     * @param time 超时时间，如果useTimeout为true且超时时间小于0表示只尝试一次
     * @param unit
     * @param interrupt 是否被线程中断
     * @return
     * @throws InterruptedException
     */
    protected boolean lock(boolean useTimeout, long time, TimeUnit unit,boolean interrupt) throws InterruptedException{
        if (interrupt) {
            checkInterruption();
        }
        if (useTimeout && time < 0){
            if (interrupt) {
                checkInterruption();
            }
          return tryRedisLock();
        }
        long start = System.currentTimeMillis();
        long timeout = unit.toMillis(time);
        while (useTimeout ? isTimeout(start, timeout) : true) {
            if (interrupt) {
                checkInterruption();
            }
            if (tryRedisLock()){
                return true;
            }
        }

        return false;
    }

    protected abstract boolean tryRedisLock();

    protected abstract void unlock0();


    /****
     * 判断是否超时了
     * @param start
     * @param timeout
     * @return
     */
    public boolean isTimeout(long start, long timeout) {
        return start + timeout > System.currentTimeMillis();
    }

}
