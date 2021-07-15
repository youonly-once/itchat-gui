package cn.shu.wechat.swing.db.model;

import cn.shu.wechat.enums.WXReceiveMsgCodeEnum;
import lombok.Data;

/**
 * Created by song on 20/03/2017.
 */

@Data
public class Message extends BasicModel {
    private String id;
    private String roomId;
    private String messageContent;
    private boolean groupable;
    private long timestamp;
    private String senderUsername;
    private String senderId;
    private long updatedAt;
    private boolean needToResend;
    private int progress; // 文件上传进度
    private boolean deleted;
    private boolean systemMessage; //是否是系统消息
    private WXReceiveMsgCodeEnum msgType;
    private String fileAttachmentId;
    private String imageAttachmentId;

    public Message() {
    }

}

