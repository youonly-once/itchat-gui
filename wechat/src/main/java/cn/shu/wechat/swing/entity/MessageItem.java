package cn.shu.wechat.swing.entity;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.enums.WXReceiveMsgCodeEnum;
import cn.shu.wechat.swing.app.Launcher;
import cn.shu.wechat.swing.db.model.FileAttachment;
import cn.shu.wechat.swing.db.model.ImageAttachment;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by song on 20/03/2017.
 */

@Data
public class MessageItem implements Comparable<MessageItem> {
    public static final int SYSTEM_MESSAGE = 0;
    public static final int LEFT_TEXT = 1;
    public static final int LEFT_IMAGE = 2;
    public static final int LEFT_ATTACHMENT = 3;

    public static final int RIGHT_TEXT = -1;
    public static final int RIGHT_IMAGE = -2;
    public static final int RIGHT_ATTACHMENT = -3;


    private String id;
    private String roomId;
    private String messageContent;
    private boolean groupable;
    private long timestamp;
    private String senderUsername;
    private String senderId;
    private long updatedAt;
    private int unreadCount;
    private boolean needToResend;
    private int progress;
    private boolean deleted;
    private int messageType;
    private WXReceiveMsgCodeEnum wxReceiveMsgCodeEnum;

    List<FileAttachmentItem> fileAttachments = new ArrayList<>();
    List<ImageAttachmentItem> imageAttachments = new ArrayList<>();

    private FileAttachmentItem fileAttachment;
    private ImageAttachmentItem imageAttachment;

    public MessageItem() {
    }

    public MessageItem(cn.shu.wechat.beans.pojo.Message message, String currentUserId, String roomId) {
        this();
        this.setId(message.getId());
        this.setMessageContent(message.getContent());
        this.setGroupable(message.getFromUsername().startsWith("@@"));
        this.setRoomId(roomId);
        this.setSenderId(message.getFromUsername());
        if (this.groupable) {
            //如果是群则显示群成员名称
            this.setSenderUsername(message.getFromMemberOfGroupDisplayname());
        } else {
            this.setSenderUsername(ContactsTools.getContactDisplayNameByUserName(message.getFromUsername()));
        }

        this.setTimestamp(message.getCreateTime().getTime());
        this.setUpdatedAt(message.getCreateTime().getTime());
        this.setNeedToResend(false);
        this.setProgress(100);
        this.setDeleted(false);
        this.setWxReceiveMsgCodeEnum(WXReceiveMsgCodeEnum.getByCode(message.getMsgType()));

        boolean isFileAttachment = false;
        boolean isImageAttachment = false;


        switch (this.getWxReceiveMsgCodeEnum()) {

            case UNKNOWN:
                break;
            case MSGTYPE_TEXT:
                break;

            case MSGTYPE_VOICE:
            case MSGTYPE_VIDEO:
            case MSGTYPE_MICROVIDEO:
                //文件类消息
                isFileAttachment = true;

                FileAttachment fa = Launcher.fileAttachmentService.findById(message.getFilePath());
                this.fileAttachment = new FileAttachmentItem(fa);
                break;
            case MSGTYPE_IMAGE:
            case MSGTYPE_EMOTICON:
                //图片类消息
                isImageAttachment = true;

                ImageAttachment ia = new ImageAttachment();
                ia.setDescription("DESC");
                ia.setHeight(500);
                ia.setWidth(400);
                ia.setTitle("sasd");
                ia.setImagesize(200);
                ia.setId(UUID.randomUUID().toString());
                ia.setImageUrl(message.getFilePath());
                this.imageAttachment = new ImageAttachmentItem(ia);
                break;
            case MSGTYPE_APP:
                break;
            case MSGTYPE_VOIPMSG:
                break;
            case MSGTYPE_VOIPNOTIFY:
                break;
            case MSGTYPE_VOIPINVITE:
                break;
            case MSGTYPE_LOCATION:
                break;
            case MSGTYPE_VERIFYMSG:
            case MSGTYPE_STATUSNOTIFY:
            case MSGTYPE_SYSNOTICE:
            case MSGTYPE_SYS:
            case MSGTYPE_RECALLED:
                //系统类消息
                this.setMessageType(SYSTEM_MESSAGE);
                break;
            case MSGTYPE_POSSIBLEFRIEND_MSG:
                break;
            case MSGTYPE_SHARECARD:
                break;
            case MSGTYPE_MAP:
                break;
            default:
                break;
        }




        /*for (FileAttachment fa : message.getFileAttachments())
        {
            this.fileAttachments.add(new FileAttachmentItem(fa));
        }

        for (ImageAttachment ia : message.getImageAttachments())
        {
            this.imageAttachments.add(new ImageAttachmentItem(ia));
        }*/

        // 自己发的消息
        if (this.getSenderId().equals(currentUserId)) {
            // 文件附件
            if (isFileAttachment) {
                this.setMessageType(RIGHT_ATTACHMENT);
            }
            // 图片消息
            else if (isImageAttachment) {
                this.setMessageType(RIGHT_IMAGE);
            }
            // 普通文本消息
            else {
                this.setMessageType(RIGHT_TEXT);
            }
        } else {
            // 文件附件
            if (isFileAttachment) {
                this.setMessageType(LEFT_ATTACHMENT);
            }
            // 图片消息
            else if (isImageAttachment) {
                this.setMessageType(LEFT_IMAGE);
            }
            // 普通文本消息
            else {
                this.setMessageType(LEFT_TEXT);
            }
        }

    }

    @Override
    public int compareTo(MessageItem o) {
        return (int) (this.getTimestamp() - o.getTimestamp());

    }
}

