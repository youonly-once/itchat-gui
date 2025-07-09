package cn.shu.wechat.task;

import cn.shu.wechat.constant.DownloadStatus;
import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 下载管理器（线程池驱动）
 * 支持提交异步下载任务、同步等待下载结果、任务状态查询等功能。
 */
@Log4j2
public class DownloadManager {

    private final static ScheduledExecutorService cleanerScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Download-Cleaner");
        t.setDaemon(true); // 设置为守护线程
        return t;
    });

    static {
        cleanerScheduler.scheduleAtFixedRate(DownloadManager::cleanFinishedTasks, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * 下载任务执行线程池：
     * - 核心线程数为 CPU 核心数
     * - 最大线程数为 核心数 * 5（可根据并发量调整）
     * - 使用 SynchronousQueue：不缓存任务，任务必须直接交付给线程执行
     * - 拒绝策略为 AbortPolicy：任务无法提交时抛出异常
     */
    private final static ExecutorService workerPool = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),                          // 核心线程数
            Runtime.getRuntime().availableProcessors() * 4,                      // 最大线程数
            0L, TimeUnit.MILLISECONDS,                                           // 空闲线程立即释放
            new SynchronousQueue<>(),                                            // 不缓存任务，直接交付
            new ThreadFactory() {                                                // 自定义线程命名
                private final AtomicInteger index = new AtomicInteger(1);

                public Thread newThread(Runnable r) {
                    return new Thread(r, "DownloadWorker-" + index.getAndIncrement());
                }
            },
            new ThreadPoolExecutor.AbortPolicy()                                 // 提交失败时抛出异常
    );

    /**
     * 当前所有下载任务的缓存映射，key 为任务 ID，value 为任务实例
     */
    private final static Map<String, DownloadTask> taskMap = new ConcurrentHashMap<>();

    /**
     * 异步提交下载任务
     *
     * @param task 下载任务
     * @param <R>  结果类型
     */
    public static <R> void submit(DownloadTask<R> task) {
        if (taskMap.containsKey(task.getTaskId())) {
            DownloadTask<R> downloadTask = taskMap.get(task.getTaskId());
            if (downloadTask.getStatus() == DownloadStatus.FAIL) {
                taskMap.remove(task.getTaskId());
            } else if (downloadTask.getStatus() == DownloadStatus.WAITING || downloadTask.getStatus() == DownloadStatus.RUNNING) {
                return;
            }
        }
        taskMap.put(task.getTaskId(), task);
        Future<R> submit = workerPool.submit(task);// 异步执行
        task.setFuture(submit);
    }

    /**
     * 同步提交下载任务，阻塞等待执行完成
     *
     * @param task 下载任务
     * @param <R>  结果类型
     * @return 下载结果，失败或中断时返回 null
     */
    public static <R> R submitAwait(DownloadTask<R> task) {
        if (taskMap.containsKey(task.getTaskId())) {
            DownloadTask<R> downloadTask = taskMap.get(task.getTaskId());
            if (downloadTask.getStatus() == DownloadStatus.FAIL) {
                taskMap.remove(task.getTaskId());
            } else if (downloadTask.getStatus() == DownloadStatus.WAITING || downloadTask.getStatus() == DownloadStatus.RUNNING) {
                if (downloadTask.getFuture() == null) {
                    awaitDownload(task.getTaskId());
                    return (R) task.getResult();
                }
                try {
                    return downloadTask.getFuture().get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error(e.getMessage());
                }
            }
        }
        taskMap.put(task.getTaskId(), task);
        Future<R> future = workerPool.submit(task);
        task.setFuture(future);
        try {
            return future.get(); // 阻塞等待执行完成
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * 同步提交任务，带超时控制
     *
     * @param task    下载任务
     * @param timeout 最大等待时间
     * @param unit    时间单位
     * @param <R>     返回结果类型
     * @return 下载结果，超时或失败时返回 null
     */
    public static <R> R submitAwait(DownloadTask<R> task, long timeout, TimeUnit unit) {
        if (taskMap.containsKey(task.getTaskId())) {
            log.error("任务已存在: " + task.getTaskId());
            throw new IllegalArgumentException("任务已存在: " + task.getTaskId());
        }
        taskMap.put(task.getTaskId(), task);
        Future<R> submit = workerPool.submit(task);
        task.setFuture(submit);
        try {
            return submit.get(timeout, unit); // 带超时时间阻塞
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * 获取指定任务的下载状态
     *
     * @param taskId 任务ID
     * @return 下载状态，若任务不存在则返回 null
     */
    public static DownloadStatus getStatus(String taskId) {
        return Optional.ofNullable(taskMap.get(taskId))
                .map(DownloadTask::getStatus)
                .orElse(null);
    }


    /**
     * 获取任务的实时下载进度队列（如每秒下载量）
     *
     * @param taskId 任务ID
     * @return 下载过程队列（可能为 null）
     */
    public static BlockingQueue<Long> getProcessLinkedBlockingDeque(String taskId) {
        return Optional.ofNullable(taskMap.get(taskId))
                .map(DownloadTask::getProcessBlockingQueue)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在，taskId=" + taskId));
    }

    /**
     * 判断任务是否存在
     *
     * @param taskId 任务ID
     * @return 是否存在任务
     */
    public static boolean containsTask(String taskId) {
        return taskMap.containsKey(taskId);
    }

    /**
     * 阻塞等待任务完成
     *
     * @param taskId 任务ID
     */
    public static void awaitDownload(String taskId) {
        DownloadTask task = taskMap.get(taskId);
        if (task == null) {
            log.error("任务不存在，taskId=" + taskId);
            return;
        }
        Future<?> future = task.getFuture();
        try {
            future.get(); // 阻塞等待
        } catch (InterruptedException | ExecutionException e) {
            log.error("等待任务执行出错", e);
        }
    }

    /**
     * 等待任务完成（最长 5 分钟），超时打印错误日志
     *
     * @param taskId 任务ID
     */
    public static void awaitDownloadTimeOut(String taskId) {
        awaitDownload(taskId, 1000 * 60 * 5);
    }

    /**
     * 等待任务完成，支持自定义超时时间
     *
     * @param taskId  任务ID
     * @param timeOut 超时时间（单位：毫秒）
     */
    public static void awaitDownload(String taskId, long timeOut) {
        long startTime = System.currentTimeMillis();
        DownloadTask task = taskMap.get(taskId);
        if (task == null) {
            log.error("任务不存在，taskId=" + taskId);
            return;
        }

        Future<?> future = task.getFuture();
        try {
            future.get(timeOut, TimeUnit.MILLISECONDS); // 阻塞等待
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("等待任务执行出错", e);
        }

    }

    private static void cleanFinishedTasks() {
        int before = taskMap.size();
        taskMap.entrySet().removeIf(entry -> {
            DownloadStatus status = entry.getValue().getStatus();
            return (status == DownloadStatus.SUCCESS || status == DownloadStatus.FAIL) && entry.getValue().getFuture().isDone();
        });
        int after = taskMap.size();
        if (before != after) {
            log.info("定时清理下载任务：共清理 {} 条，剩余任务 {} 条", (before - after), after);
        }
    }

}
