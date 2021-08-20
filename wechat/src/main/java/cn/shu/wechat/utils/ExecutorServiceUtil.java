package cn.shu.wechat.utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池工具类
 *
 * @author SXS
 * @since 3/12/2021
 */
public class ExecutorServiceUtil {
    /**
     * 头像下载线程池
     * 核心线程 0，临时线程最大
     * 线程执行完任务立即销毁
     */
    private final static ExecutorService headImageDownloadExecutorService = new ThreadPoolExecutor(
            0
            , Integer.MAX_VALUE
            , 0
            , TimeUnit.MICROSECONDS
            , new SynchronousQueue<>()
            , new MyThreadFactory("HeadImgDownloadPool-Thread-", false, Thread.NORM_PRIORITY)
    );

    /**
     * 全局线程池
     */
    private final static ExecutorService globalExecutorService = new ThreadPoolExecutor(
            1
            , Integer.MAX_VALUE
            , 0L
            , TimeUnit.SECONDS
            , new SynchronousQueue<>()
            , new MyThreadFactory("GlobalPool-Thread-", false, 6)
    );

    public static ExecutorService getReceivingExecutorService() {
        return receivingExecutorService;
    }

    /**
     * 接收消息线程池
     */
    private final static ExecutorService receivingExecutorService = new ThreadPoolExecutor(
            1
            , 5
            , 0L
            , TimeUnit.SECONDS
            , new SynchronousQueue<>()
            , new MyThreadFactory("Receiving-Thread-", false, 6)
    );

    static class MyThreadFactory implements ThreadFactory {
        private final AtomicInteger integer = new AtomicInteger();
        private final String prefix;
        private final boolean daemon;
        private final int priority;

        public MyThreadFactory(String prefix, boolean daemon, int priority) {
            this.prefix = prefix;
            this.daemon = daemon;
            this.priority = priority;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(daemon);
            thread.setPriority(priority);
            thread.setName(prefix + integer.getAndIncrement());
            return thread;
        }
    }
    public static ExecutorService getGlobalExecutorService() {
        return globalExecutorService;
    }

    public static ExecutorService getHeadImageDownloadExecutorService() {
        return headImageDownloadExecutorService;
    }


}
