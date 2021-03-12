package cn.shu.wechat.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池工具类
 *
 * @author SXS
 * @since 3/12/2021
 */

public class ExecutorsUtil {
    /**
     * 头像下载线程池
     */
    private final static ExecutorService headImageDownloadExecutorService = Executors.newCachedThreadPool();

    /**
     * 全局线程池
     */
    private final static ExecutorService globalExecutorService = Executors.newCachedThreadPool();


    public static ExecutorService getGlobalExecutorService() {
        return globalExecutorService;
    }

    public static ExecutorService getHeadImageDownloadExecutorService() {
        return headImageDownloadExecutorService;
    }
}
