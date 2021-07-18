package cn.shu.wechat.swing.entity;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.enums.WXReceiveMsgCodeEnum;
import cn.shu.wechat.swing.app.Launcher;
import cn.shu.wechat.swing.db.model.FileAttachment;
import cn.shu.wechat.swing.db.model.ImageAttachment;
import cn.shu.wechat.swing.db.model.VideoAttachment;
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
    public static final int LEFT_VIDEO = 4;

    public static final int RIGHT_TEXT = -1;
    public static final int RIGHT_IMAGE = -2;
    public static final int RIGHT_ATTACHMENT = -3;
    public static final int RIGHT_VIDEO = 4;


    private String id;
    private String roomId;
    private String messageContent;
    private boolean groupable;
    private long timestamp;
    private String senderUsername;
    private String senderId;

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
    private VideoAttachmentItem videoAttachmentItem;
    public MessageItem() {
    }

    public MessageItem(cn.shu.wechat.beans.pojo.Message message, String roomId) {
        this();
        this.setId(message.getId());
        this.setMessageContent(message.getContent());
        this.setGroupable(message.getFromUsername().startsWith("@@"));
        this.setRoomId(roomId);
        this.setSenderId(this.isGroupable()?message.getFromMemberOfGroupUsername():message.getFromUsername());
        if (this.groupable) {
            //如果是群则显示群成员名称
            this.setSenderUsername(message.getFromMemberOfGroupDisplayname());
        } else {
            this.setSenderUsername(ContactsTools.getContactDisplayNameByUserName(message.getFromUsername()));
        }

        this.setTimestamp(message.getCreateTime().getTime());
        this.setNeedToResend(!message.getIsSend());
        this.setProgress(message.getProcess());
        this.setDeleted(message.isDeleted());
        this.setWxReceiveMsgCodeEnum(WXReceiveMsgCodeEnum.getByCode(message.getMsgType()));

        boolean isFileAttachment = false;
        boolean isImageAttachment = false;
        boolean isVideoAttachment = false;

        switch (this.getWxReceiveMsgCodeEnum()) {

            case UNKNOWN:
                break;
            case MSGTYPE_TEXT:
                break;

            case MSGTYPE_VOICE:
            case MSGTYPE_MICROVIDEO:
            case MSGTYPE_APP:
            case MSGTYPE_VIDEO:
                //文件类消息
                isFileAttachment = true;
                FileAttachmentItem fileAttachmentItem = new FileAttachmentItem();
                fileAttachmentItem.setTitle(message.getFilePath());
                fileAttachmentItem.setId(message.getId());
                fileAttachmentItem.setDescription("desc");
                fileAttachmentItem.setLink(message.getFilePath());
                fileAttachmentItem.setSlavePath(message.getSlavePath());
                this.setMessageContent(message.getFilePath());
                this.fileAttachment = fileAttachmentItem;
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
                ia.setSlavePath(message.getSlavePath());
                ia.setImagePath(message.getFilePath());
                this.imageAttachment = new ImageAttachmentItem(ia);
                break;
/*            case MSGTYPE_VIDEO:
                //图片类消息
                isImageAttachment = true;

                ia = new ImageAttachment();
                ia.setDescription("DESC");
                ia.setHeight(500);
                ia.setWidth(400);
                ia.setTitle("sasd");
                ia.setImagesize(200);
                ia.setId(UUID.randomUUID().toString());
                ia.setSlavePath(message.getSlavePath());
                ia.setImagePath(message.getFilePath());
                this.imageAttachment = new ImageAttachmentItem(ia);
                this.imageAttachment.setVideo(true);
                break;*/
/*            case MSGTYPE_VIDEO:
                //视频类消息
                isVideoAttachment = true;
                VideoAttachmentItem videoAttachmentItem = new VideoAttachmentItem();
                videoAttachmentItem.setDescription("DESC");
                videoAttachmentItem.setHeight(500);
                videoAttachmentItem.setWidth(400);
                videoAttachmentItem.setTitle("sasd");
                videoAttachmentItem.setImagesize(200);
                videoAttachmentItem.setId(UUID.randomUUID().toString());
                videoAttachmentItem.setSlavePath(message.getSlavePath());
                videoAttachmentItem.setImagePath(message.getFilePath());
                this.videoAttachmentItem = videoAttachmentItem;
                break;*/
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
        if (Core.getUserSelf().getUsername().equals(this.getSenderId())) {
            // 文件附件
            if (isFileAttachment) {
                this.setMessageType(RIGHT_ATTACHMENT);
            }
            // 图片消息
            else if (isImageAttachment) {
                this.setMessageType(RIGHT_IMAGE);
            }
            // 视频消息
            else if (isVideoAttachment){
                this.setMessageType(RIGHT_VIDEO);
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
            // 视频消息
            else if (isVideoAttachment){
                this.setMessageType(LEFT_VIDEO);
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

