package cn.shu.wechat.utils;

import org.apache.tomcat.util.threads.TaskThreadFactory;

import java.util.concurrent.*;

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
            , TimeUnit.SECONDS
            , new SynchronousQueue<>()
            , new TaskThreadFactory("HeadImgDownloadPool-Thread-", false, Thread.NORM_PRIORITY));

    /**
     * 全局线程池
     */
    private final static ExecutorService globalExecutorService = new ThreadPoolExecutor(
            20
            , Integer.MAX_VALUE
            , 60L
            , TimeUnit.SECONDS
            , new SynchronousQueue<>()
            , new TaskThreadFactory("GlobalPool-Thread-", false, 6)
    );


    public static ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    /**
     * 更新联系人定时任务
     */
    private final static ScheduledExecutorService scheduledExecutorService
            = Executors.newScheduledThreadPool(3,
                    new TaskThreadFactory("ScheduledExecutorServiceThread-", false, Thread.NORM_PRIORITY));


    public static ExecutorService getGlobalExecutorService() {
        return globalExecutorService;
    }

    public static ExecutorService getHeadImageDownloadExecutorService() {
        return headImageDownloadExecutorService;
    }


}
