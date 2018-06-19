package com.absurd.distributedlock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wangwenwei
 * @time 2018/6/9
 */
public class BaseTest {

    protected static AtomicInteger atomicInteger = new AtomicInteger(0);

    protected ExecutorService executorService = Executors.newCachedThreadPool();

    protected volatile static Integer[] arr = {0};
}
