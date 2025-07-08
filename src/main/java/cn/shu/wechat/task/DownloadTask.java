package cn.shu.wechat.task;

import cn.shu.wechat.api.DownloadTools;
import cn.shu.wechat.constant.DownloadStatus;
import cn.shu.wechat.constant.DownloadType;
import cn.shu.wechat.dto.response.sync.AddMsgList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

/**
 * 下载任务类，支持多种下载类型（头像、资源、图片等）
 * 实现 Callable 接口，用于提交到线程池并返回结果
 * 泛型 R 表示任务下载结果的类型
 */
@NoArgsConstructor
@Log4j2
public class DownloadTask<R> implements Callable<R> {

    /**
     * 下载过程进度队列（每次进度更新时追加）
     */
    @Getter
    private final LinkedBlockingDeque<Long> FILE_DOWNLOAD_PROCESS = new LinkedBlockingDeque<>();
    /**
     * 任务唯一标识
     */
    @Getter
    @Setter
    private String taskId;
    /**
     * 下载资源 URL（适用于 HTTP 下载）
     */
    @Getter
    private String url;
    /**
     * 文件保存路径
     */
    @Getter
    private String destPath;
    /**
     * 任务完成后的回调处理逻辑（可选）
     */
    private Consumer<DownloadTask<R>> callback = null;
    /**
     * 当前下载任务的状态（默认 WAITING）
     */
    @Getter
    private volatile DownloadStatus status = DownloadStatus.WAITING;
    /**
     * 已下载字节数，用于进度跟踪
     */
    @Getter
    private volatile long downloadedBytes = 0;
    /**
     * 下载类型（由上层设置）
     */
    @Getter
    @Setter
    private DownloadType type;

    /**
     * 微信消息ID（适用于基于消息下载）
     */
    @Getter
    @Setter
    private String msgId;

    /**
     * 下载结果，支持泛型返回
     */
    @Getter
    @Setter
    private Object result;

    /**
     * 微信资源路径（如头像路径）
     */
    @Getter
    @Setter
    private String relativeUrl;

    /**
     * 用户名（用于下载头像等）
     */
    @Getter
    @Setter
    private String userName;

    /**
     * 消息体对象（适用于 FN 类型下载）
     */
    private AddMsgList msg;

    /**
     * 资源类型（如图片、视频等）
     */
    @Getter
    @Setter
    private String resourceType;

    // 构造函数们（支持不同场景）

    public DownloadTask(AddMsgList msg, Consumer<DownloadTask<R>> callback) {
        this.msg = msg;
        this.callback = callback;
    }

    public DownloadTask(String taskId, String url, String destPath, Consumer<DownloadTask<R>> callback) {
        this.taskId = taskId;
        this.url = url;
        this.destPath = destPath;
        this.callback = callback;
    }

    public DownloadTask(Long msgId, String destPath, Consumer<DownloadTask<R>> callback) {
        this.msgId = String.valueOf(msgId);
        this.destPath = destPath;
        this.callback = callback;
    }

    public DownloadTask(String relativeUrl, String userName, Consumer<DownloadTask<R>> callback) {
        this.relativeUrl = relativeUrl;
        this.userName = userName;
        this.callback = callback;
    }

    public DownloadTask(String userName, Consumer<DownloadTask<R>> callback) {
        this.userName = userName;
        this.callback = callback;
    }

    /**
     * 任务执行入口，提交到线程池后自动调用
     * 根据不同类型进行下载，更新状态和进度
     *
     * @return 下载结果对象（R类型），失败返回 null
     */
    @Override
    public R call() {
        try {
            status = DownloadStatus.RUNNING;

            // 下载进度回调，用于实时记录进度
            Consumer<Long> progressCallback = downloaded -> {
                this.downloadedBytes = downloaded;
                FILE_DOWNLOAD_PROCESS.offer(downloaded);
            };

            // 根据下载类型执行对应的工具方法
            switch (type) {
                case FN:
                    DownloadTools.getDownloadFn(msg, progressCallback);
                    break;
                case RESOURCE_BY_MSGID:
                    DownloadTools.downloadFileByMsgId(msgId, destPath, progressCallback);
                    break;
                case HEAD_IMAGE_BIG:
                    this.result = DownloadTools.downloadBigHeadImg(relativeUrl, userName);
                    break;
                case ByRelativeUrl:
                    this.result = DownloadTools.downloadHeadImgByRelativeUrl(relativeUrl);
                    break;
                case RESOURCE_BY_USERNAME:
                    this.result = DownloadTools.downloadHeadImgByUserName(userName);
                    break;
                case ImgByMsgID:
                    this.result = DownloadTools.downloadImgByMsgID(String.valueOf(msgId), resourceType);
                    break;
                case ImgByteByMsgID:
                    this.result = DownloadTools.downloadImgByteByMsgID(String.valueOf(msgId), resourceType);
                    break;
                default:
                    throw new IllegalArgumentException("未知下载类型: " + type);
            }
            if (result == null) {
                throw new Exception("result is null!");
            }
            status = DownloadStatus.SUCCESS;
            log.info("资源下载完成：{}", this);
            return (R) result;

        } catch (Exception e) {
            status = DownloadStatus.FAIL;
            log.error("下载文件失败：{},{}", this.toString(), e.toString());
        }
        return null;
    }

    /**
     * 打印任务的详细信息（用于日志和调试）
     */
    @Override
    public String toString() {
        return "DownloadTask{" +
                "taskId='" + taskId + '\'' +
                ", url='" + url + '\'' +
                ", destPath='" + destPath + '\'' +
                ", callback=" + callback +
                ", status=" + status +
                ", downloadedBytes=" + downloadedBytes +
                ", FILE_DOWNLOAD_PROCESS=" + FILE_DOWNLOAD_PROCESS +
                ", type=" + type +
                ", msgId='" + msgId + '\'' +
                ", result=" + result +
                ", relativeUrl='" + relativeUrl + '\'' +
                ", userName='" + userName + '\'' +
                ", msg=" + msg +
                ", resourceType='" + resourceType + '\'' +
                '}';
    }
}
