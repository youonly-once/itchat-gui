package cn.shu.wechat.task;

import cn.shu.wechat.api.DownloadTools;
import cn.shu.wechat.constant.DownloadStatus;
import cn.shu.wechat.utils.SleepUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * 下载管理器（队列驱动）
 */

@Log4j2
public class DownloadManager  {

    private final static BlockingQueue<DownloadTask> taskQueue = new LinkedBlockingQueue<>();
    private final static ExecutorService workerPool;
    private final static Map<String, DownloadTask> taskMap = new ConcurrentHashMap<>();

    static  {
        int concurrentWorkers = 10;
        workerPool = Executors.newFixedThreadPool(concurrentWorkers);
        for (int i = 0; i < concurrentWorkers; i++) {
            workerPool.submit(DownloadManager::workerLoop);
        }
    }

    private static void workerLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                DownloadTask task = taskQueue.take();
                if (task.isCancelled()) continue;
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void submit(DownloadTask task) {
        if (taskMap.containsKey(task.getTaskId())) {
            throw new IllegalArgumentException("任务已存在: " + task.getTaskId());
        }
        taskMap.put(task.getTaskId(), task);
        taskQueue.offer(task);
    }

    public static Object submitAwait(DownloadTask task) {
        submit(task);
        while (task.getStatus() == DownloadStatus.RUNNING || task.getStatus() == DownloadStatus.FAIL) {
            SleepUtils.sleep(100);
        }
        return task.getResult();
    }

    public DownloadStatus getStatus(String taskId) {
        return Optional.ofNullable(taskMap.get(taskId))
                .map(DownloadTask::getStatus)
                .orElse(null);
    }

    public long getDownloadedBytes(String taskId) {
        return Optional.ofNullable(taskMap.get(taskId))
                .map(DownloadTask::getDownloadedBytes)
                .orElse(-1L);
    }


    public boolean cancel(String taskId) {
        DownloadTask task = taskMap.get(taskId);
        if (task != null && task.getStatus() == DownloadStatus.WAITING) {
            task.cancel();
            taskQueue.remove(task);
            return true;
        }
        return false;
    }
    /**
     * 等待下载完成
     */
    public static void awaitDownload(String taskId){
        DownloadTask task = taskMap.get(taskId);
        while (task.getStatus() == DownloadStatus.RUNNING || task.getStatus() == DownloadStatus.FAIL) {
            SleepUtils.sleep(100);
        }
    }
    /**
     * 等待下载完成
     */
    public static void awaitDownloadTimeOut(String taskId){
        long startTime = System.currentTimeMillis();
        long timeOut = 1000 * 60 *5;

        DownloadTask task = taskMap.get(taskId);
        while (task.getStatus() == DownloadStatus.RUNNING || task.getStatus() == DownloadStatus.FAIL) {
            // 检查是否超时
            if (System.currentTimeMillis() - startTime > timeOut) {
                log.error("下载等待超时: {}" , taskId);
                break;
            }
            SleepUtils.sleep(100); // 每 100ms 轮询一次
        }
    }
    /**
         * 等待下载完成，最多等待 10 分钟
     * @param taskId 文件路径
     */
    public static void awaitDownload(String taskId,long timeOut) {
        long startTime = System.currentTimeMillis();

        DownloadTask task = taskMap.get(taskId);
        while (task.getStatus() == DownloadStatus.RUNNING || task.getStatus() == DownloadStatus.FAIL) {
            // 检查是否超时
            if (System.currentTimeMillis() - startTime > timeOut) {
                log.error("下载等待超时: {}" , taskId);
                break;
            }
            SleepUtils.sleep(100); // 每 100ms 轮询一次
        }
    }

    public void shutdown() {
        workerPool.shutdownNow();
    }

}
