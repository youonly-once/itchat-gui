package cn.shu.wechat.task;

import cn.shu.wechat.api.DownloadTools;
import cn.shu.wechat.constant.DownloadStatus;
import cn.shu.wechat.constant.DownloadType;
import cn.shu.wechat.dto.response.sync.AddMsgList;
import cn.shu.wechat.service.impl.LoginServiceImpl;
import cn.shu.wechat.utils.SpringContextHolder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.net.http.HttpResponse;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
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
    @Getter(value = AccessLevel.PROTECTED)
    private final BlockingQueue<Long> processBlockingQueue = new ArrayBlockingQueue<>(100);
    /**
     * 微信资源路径（如头像路径）
     */
    @Getter
    @Setter
    protected String relativeUrl;
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
     * 用户名（用于下载头像等）
     */
    @Getter
    @Setter
    protected String userName;
    @Getter(value = AccessLevel.PROTECTED)
    @Setter(value = AccessLevel.PROTECTED)
    private Future<R> future;

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
     * 任务完成后的回调处理逻辑（可选）
     */
    @Getter(value = AccessLevel.PROTECTED)
    @Setter
    private Consumer<DownloadTask<R>> callback = null;
    /**
     * 当前下载任务的状态（默认 WAITING）
     */
    @Getter(value = AccessLevel.PROTECTED)
    private volatile DownloadStatus status = DownloadStatus.WAITING;
    /**
     * 下载结果，支持泛型返回
     */
    @Getter
    private R result;

    /**
     * 消息体对象（适用于 FN 类型下载）
     */
    private AddMsgList msg;

    /**
     * 消息体对象（适用于 FN 类型下载）
     */
    @Getter
    @Setter
    private String groupName;

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

            // 根据下载类型执行对应的工具方法
            switch (type) {
                case FN:
                    DownloadTools.getDownloadFn(msg, processBlockingQueue);
                    this.result = (R) msg;
                    break;
                case RESOURCE_BY_MSGID:
                    DownloadTools.downloadFileByMsgId(msgId, destPath, processBlockingQueue);
                    this.result = (R) destPath;
                    break;
                case HEAD_IMAGE_BIG:
                    this.result = (R) DownloadTools.downloadBigHeadImg(relativeUrl, userName);
                    break;
                case ByRelativeUrl:
                    this.result = (R) DownloadTools.downloadHeadImgByRelativeUrl(relativeUrl);
                    break;
                case RESOURCE_BY_USERNAME:
                    this.result = (R) DownloadTools.downloadHeadImgByUserName(userName);
                    break;
                case ImgByMsgID:
                    this.result = (R) DownloadTools.downloadImgByMsgID(String.valueOf(msgId), resourceType);
                    break;
                case ImgByteByMsgID:
                    this.result = (R) DownloadTools.downloadImgEntityByMsgID(String.valueOf(msgId), resourceType, HttpResponse.BodyHandlers.ofByteArray());
                    break;
                case GetContacts:
                    LoginServiceImpl loginService = SpringContextHolder.getBean(LoginServiceImpl.class);
                     loginService.webWxGetContact();
                    this.result = (R) "";
                    break;
                case GetBatchContacts: {
                    loginService = SpringContextHolder.getBean(LoginServiceImpl.class);
                    if (StringUtils.isNotEmpty(groupName)){
                        loginService.WebWxBatchGetContact(groupName);
                    }else{
                        loginService.WebWxBatchGetContact();
                    }
                    break;
                }
                default:
                    throw new IllegalArgumentException("未知下载类型: " + type);
            }
            if (callback != null) {
                callback.accept(this);
            }
            if (result == null
                    && type != DownloadType.GetContacts
                    && type != DownloadType.GetBatchContacts) {
                throw new Exception("result is null!");
            }
            status = DownloadStatus.SUCCESS;
            log.info("资源下载完成({})：{}", type.getDescription(), this);
            return (R) result;

        } catch (Exception e) {
            status = DownloadStatus.FAIL;

            log.error("下载文件失败({})：{},{}", type.getDescription(), this.toString(), e.getMessage(),e);
        }
        return null;
    }

    @Override
    public String toString() {
        return "DownloadTask{" +
                ", relativeUrl='" + relativeUrl + '\'' +
                ", taskId='" + taskId + '\'' +
                ", url='" + url + '\'' +
                ", destPath='" + destPath + '\'' +
                ", userName='" + userName + '\'' +
                ", type=" + type +
                ", msgId='" + msgId + '\'' +
                ", status=" + status +
                ", result=" + result +
                ", msg=" + msg +
                ", resourceType='" + resourceType + '\'' +
                '}';
    }
}
