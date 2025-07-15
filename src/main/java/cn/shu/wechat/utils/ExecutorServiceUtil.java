package cn.shu.wechat.utils;

import lombok.Getter;

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

    @Getter
    private static final ExecutorService headImageDownloadExecutorService =
            Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("HeadImgDownloadPool-VirtualThread-", 0).factory());



    /**
     * 全局线程池
     */
    @Getter
    private static final ExecutorService globalExecutorService =
            Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("GlobalPool-VirtualThread-", 0).factory());

    /**
     * 接收消息线程池
     */
    @Getter
    private static final ExecutorService receivingExecutorService =
            Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("Receiving-VirtualThread-", 0).factory());


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


}
