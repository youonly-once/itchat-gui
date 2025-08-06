package cn.shu.wechat.task;

import cn.shu.wechat.constant.DownloadStatus;
import cn.shu.wechat.constant.DownloadType;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.swing.frames.MainFrame;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

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

    private final static ScheduledExecutorService updateContactsScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Contacts-Updater");
        t.setDaemon(true); // 设置为守护线程
        return t;
    });
    private static final ExecutorService workerPool =
            Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("Download--VirtualThread-", 0).factory());

    static {
        cleanerScheduler.scheduleWithFixedDelay(DownloadManager::cleanFinishedTasks, 1, 5, TimeUnit.MINUTES);
        updateContactsScheduler.scheduleWithFixedDelay(DownloadManager::updateContactsInfo, 1, 1, TimeUnit.MINUTES);
        updateContactsScheduler.scheduleWithFixedDelay(DownloadManager::updateContactsInfo1, 1, 5, TimeUnit.MINUTES);
    }


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
        DownloadTask<R> existing = taskMap.putIfAbsent(task.getTaskId(), task);
        if (existing != null) {
            if (existing.getStatus() == DownloadStatus.FAIL) {
                taskMap.remove(task.getTaskId());
            } else if (existing.getStatus() == DownloadStatus.WAITING || existing.getStatus() == DownloadStatus.RUNNING) {
                return;
            }
        }
        Future<R> submit = workerPool.submit(task);// 异步执行
        task.setFuture(submit);
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
        // 原子注册任务：避免重复提交
        //如果 key（即 task.getTaskId()）不存在于 map 中，则将 task 插入 map，并返回 null；
        //如果 key 已经存在，则不覆盖原有值，直接返回原有的 value。
        DownloadTask<R> existing = taskMap.putIfAbsent(task.getTaskId(), task);

        if (existing!=null) {
            //存在已有任务
            if (existing.getStatus() == DownloadStatus.FAIL) {
                taskMap.remove(task.getTaskId());
            } else if (existing.getStatus() == DownloadStatus.WAITING || existing.getStatus() == DownloadStatus.RUNNING) {
                if (existing.getFuture() == null) {
                    log.error("轮询等待中：{}", existing);
                    awaitDownloadLatch(task);
                    return (R) task.getResult();
                }
                try {

                    log.error("future等待中：{}", existing);
                    return existing.getFuture().get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error(e.getMessage());
                }
            }else{
                log.info("任务已完成：{}", existing);
                return (R) existing.getResult();
            }
        }else {
            log.info("提交新任务：{}", task);
            Future<R> future = workerPool.submit(task);
            task.setFuture(future);
            try {
                return future.get(timeout,unit);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error(e.getMessage(),e);
            }
        }
        return null;
    }

    /**
     * 如果当前没有相同任务正在执行(无论之前任务是否成功)  则提交新任务
     *
     * @param task 下载任务
     * @param <R>  结果类型
     * @return 下载结果，失败或中断时返回 null
     */
    public static <R> R submitNewAwait(DownloadTask<R> task) {
        DownloadTask<R> existing = taskMap.putIfAbsent(task.getTaskId(), task);
        if (existing != null) {
            if (existing.getStatus() == DownloadStatus.WAITING || existing.getStatus() == DownloadStatus.RUNNING) {
                if (existing.getFuture() == null) {
                    awaitDownloadLatch(existing);
                    return (R) task.getResult();
                }
                try {
                    return existing.getFuture().get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error(e.getMessage());
                }
            }
        }
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
     * @param <R>     返回结果类型
     * @return 下载结果，超时或失败时返回 null
     */
    public static <R> R submitAwait(DownloadTask<R> task) {
        if (StringUtils.isEmpty(task.getTaskId())){
            throw new RuntimeException("任务ID不能为空");
        }
        return submitAwait(task, Long.MAX_VALUE, TimeUnit.DAYS);
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
        if (future == null) {
            awaitDownloadLatch(task);
            return;
        }
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
        awaitDownload(taskId, 1000 * 60 * 1);
    }

    /**
     * 等待任务完成，支持自定义超时时间
     *
     * @param taskId  任务ID
     * @param timeOut 超时时间（单位：毫秒）
     */
    public static void awaitDownload(String taskId, long timeOut) {
        DownloadTask task = taskMap.get(taskId);
        if (task == null) {
            return;
        }

        Future<?> future = task.getFuture();
        if (future == null) {
            awaitDownloadLatch(task, timeOut);
            return;
        }
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

    private static void awaitDownloadLatch(DownloadTask<?> task) {
        awaitDownloadLatch(task, 60 * 1000);
    }

    /**
     * 在没有Future的情况下等待任务完成。
     */
    private static void awaitDownloadLatch(DownloadTask<?> task, long timeoutMillis) {
        long deadline = System.currentTimeMillis() + timeoutMillis;

        while (true) {
            DownloadStatus status = task.getStatus();
            if (status != DownloadStatus.WAITING && status != DownloadStatus.RUNNING) {
                return;
            }

            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                log.warn("等待任务超时：{}", task.getTaskId());
                return;
            }

            try {
                Thread.sleep(Math.min(200, remaining));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 恢复中断状态
                log.error("等待任务时被中断：{}", task.getTaskId(), e);
                return;
            }
        }
    }



    private static void updateContactsInfo() {

        long l = System.currentTimeMillis();

        DownloadTask<Void> task1 = new DownloadTask<>();
        task1.setTaskId("webWxGetContact");
        task1.setType(DownloadType.GetContacts);
        submitNewAwait(task1);

        log.info("获取普通联系人，耗时：{}（秒）", (System.currentTimeMillis() - l) / 1000);
        //否则 SearchPanel中保存的以前的联系人信息 且不能释放
        MainFrame.getContext().getLeftPanel().getSearchPanel().setSearchList(Core.getMemberMap().values());

    }

    private static void updateContactsInfo1() {

        long l = System.currentTimeMillis();

        DownloadTask<Void> task2 = new DownloadTask<>();
        task2.setTaskId("WebWxBatchGetContact");
        task2.setType(DownloadType.GetBatchContacts);
        submitNewAwait(task2);
        //ContactsPanel.getContext().notifyDataSetChanged();
        log.info("获取群联系人，耗时：{}（秒）", (System.currentTimeMillis() - l) / 1000);
        //否则 SearchPanel中保存的以前的联系人信息 且不能释放
        MainFrame.getContext().getLeftPanel().getSearchPanel().setSearchList(Core.getMemberMap().values());

    }

}
