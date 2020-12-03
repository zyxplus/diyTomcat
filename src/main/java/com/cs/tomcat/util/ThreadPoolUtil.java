package com.cs.tomcat.util;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil {
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(20,
            100,
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>());

    public static void run(Runnable r) {
        threadPoolExecutor.execute(r);
    }
}
