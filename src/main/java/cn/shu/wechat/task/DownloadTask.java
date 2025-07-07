package cn.shu.wechat.task;

import cn.shu.wechat.api.DownloadTools;
import cn.shu.wechat.constant.DownloadStatus;
import cn.shu.wechat.constant.DownloadType;
import cn.shu.wechat.dto.response.sync.AddMsgList;
import cn.shu.wechat.utils.SleepUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

/**
 * 下载任务类
 */
@NoArgsConstructor
@Log4j2
public class DownloadTask implements Runnable {
    @Getter @Setter
    private String taskId;
    @Getter
    private String url;
    @Getter
    private String destPath;
    private Consumer<DownloadTask> callback = null;

    @Getter
    private volatile DownloadStatus status = DownloadStatus.WAITING;
    @Getter
    private volatile long downloadedBytes = 0;

    @Getter
    private final HashMap<String, LinkedBlockingDeque<Long>> FILE_DOWNLOAD_PROCESS = new HashMap<>();

    @Getter @Setter
    private volatile DownloadType type;

    @Getter @Setter
    private String msgId;

    @Getter @Setter
    private Object result;

    @Getter @Setter
    private String relativeUrl;

    @Getter @Setter
    private String userName;
    private AddMsgList msg;

    @Getter @Setter
    private String resourceType;

    @Getter
    private volatile boolean cancelled = false;



    public DownloadTask(AddMsgList msg,  Consumer<DownloadTask> callback) {
        this.msg = msg;
        this.callback = callback;
    }

    public DownloadTask(String taskId, String url, String destPath, Consumer<DownloadTask> callback) {
        this.taskId = taskId;
        this.url = url;
        this.destPath = destPath;
        this.callback = callback;
    }

    public DownloadTask(Long msgId, String destPath, Consumer<DownloadTask> callback) {
        this.msgId = String.valueOf(msgId);
        this.destPath = destPath;
        this.callback = callback;

    }

    public DownloadTask(String relativeUrl, String userName, Consumer<DownloadTask> callback) {
        this.relativeUrl = relativeUrl;
        this.userName = userName;
        this.callback = callback;
    }


    public DownloadTask(String userName, Consumer<DownloadTask> callback) {
        this.userName = userName;
        this.callback = callback;
    }

    @Override
    public void run() {
        status = DownloadStatus.RUNNING;
        if (StringUtils.isNotEmpty(destPath)) {
            FILE_DOWNLOAD_PROCESS.put(destPath, new LinkedBlockingDeque<>());
        }else {
            FILE_DOWNLOAD_PROCESS.put(taskId, new LinkedBlockingDeque<>());
        }
        try {
            Consumer<Long> progressCallback = downloaded -> {
                this.downloadedBytes = downloaded;
                FILE_DOWNLOAD_PROCESS.get(taskId).offer(downloaded);
            };
                switch (type) {
                    case FN:
                        DownloadTools.FILE_DOWNLOAD_PROCESS.computeIfAbsent(destPath, k -> new LinkedBlockingDeque<>());
                        DownloadTools.getDownloadFn(msg,progressCallback);
                        break;
                    case RESOURCE_BY_MSGID:
                        DownloadTools.downloadFileByMsgId(msgId, destPath,progressCallback);
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
                        this.result = DownloadTools.downloadImgByMsgID(String.valueOf(msgId),resourceType);
                        break;
                    case ImgByteByMsgID:
                        this.result = DownloadTools.downloadImgByteByMsgID(String.valueOf(msgId),resourceType);
                        break;
                    default:
                        throw new IllegalArgumentException("未知下载类型: " + type);


            }
            status = DownloadStatus.SUCCESS;
            log.info("资源下载完成：{}", this);
        } catch (Exception e) {
            status = DownloadStatus.FAIL;
            log.error("下载文件失败：{},{}", this.toString(),e.toString());
        }
    }

    public void cancel() {
        this.cancelled = true;
    }


    /**
     * 等待下载完成
     */
    public void awaitDownload(){
        while (this.getStatus() == DownloadStatus.RUNNING || this.getStatus() == DownloadStatus.FAIL) {
            SleepUtils.sleep(100);
        }
    }

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
                ", cancelled=" + cancelled +
                '}';
    }
}
