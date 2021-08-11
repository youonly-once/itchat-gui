package cn.shu.wechat.swing.entity;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.enums.WXReceiveMsgCodeEnum;
import cn.shu.wechat.enums.WXReceiveMsgCodeOfAppEnum;
import cn.shu.wechat.swing.db.model.ImageAttachment;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 *
 * @author 舒新胜
 * @date 20/03/2017
 */

@Data
@NoArgsConstructor
public class MessageItem implements Comparable<MessageItem> {
    public static final int SYSTEM_MESSAGE = 0;
    public static final int LEFT_TEXT = 1;
    public static final int LEFT_IMAGE = 2;
    public static final int LEFT_ATTACHMENT = 3;
    public static final int LEFT_VIDEO = 4;
    public static final int LEFT_VOICE = 5;
    public static final int LEFT_LINK = 6;

    public static final int RIGHT_TEXT = -1;
    public static final int RIGHT_IMAGE = -2;
    public static final int RIGHT_ATTACHMENT = -3;
    public static final int RIGHT_VIDEO = -4;
    public static final int RIGHT_VOICE = -5;
    public static final int RIGHT_LINK = -6;

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


    private FileAttachmentItem fileAttachment;
    private ImageAttachmentItem imageAttachment;
    private VideoAttachmentItem videoAttachmentItem;
    private VoiceAttachmentItem voiceAttachmentItem;
    private LinkAttachmentItem linkAttachmentItem;
    private boolean isSystemMsg;

    public MessageItem(cn.shu.wechat.beans.pojo.Message message, String roomId) {
        this();
        this.setId(message.getId());
        this.setMessageContent(message.getPlaintext());
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

        switch (this.getWxReceiveMsgCodeEnum()) {
            case UNKNOWN:
                break;
            case MSGTYPE_TEXT:
                break;
            case MSGTYPE_VOICE:
                voiceAttachmentItem = VoiceAttachmentItem.builder()
                        .id(message.getId())
                        .voiceLength(message.getVoiceLength())
                        .voicePath(message.getFilePath()).build();
                break;
            case MSGTYPE_MICROVIDEO:
            case MSGTYPE_APP:
                switch (WXReceiveMsgCodeOfAppEnum.getByCode(message.getAppMsgType())) {
                    case OTHER:
                        break;
                    case LINK:
                        Map<String, Object> stringObjectMap = MessageTools.parseUndoMsg(message.getContent());
                        Object desc = stringObjectMap.get("msg.appmsg.des");
                        Object url = stringObjectMap.get("msg.appmsg.url");
                        Object title = stringObjectMap.get("msg.appmsg.title");
                        Object thumbUrl = stringObjectMap.get("msg.appmsg.thumburl");
                        Object sourceName = stringObjectMap.get("msg.appmsg.sourcedisplayname");
                        linkAttachmentItem = LinkAttachmentItem.builder()
                                .desc(desc == null?"":desc.toString())
                                .thumbUrl(thumbUrl == null?"":thumbUrl.toString())
                                .id(message.getId())
                                .title(title == null?"":title.toString())
                                .url(url == null?"":url.toString())
                                .sourceName(sourceName == null?"":sourceName.toString())
                                .build();
                    case PROGRAM:
                      break;
                    case FILE:
                        //文件类消息
                        this.fileAttachment =FileAttachmentItem.builder()
                                .description(message.getFileName())
                                .fileName(message.getFileName())
                                .fileSize(message.getFileSize())
                                .id(message.getId())
                                .filePath(message.getFilePath())
                                .slavePath(message.getSlavePath())
                                .build();
                        this.setMessageContent(message.getFilePath());
                        break;
                    default:
                }
                break;
            case MSGTYPE_VIDEO:
                videoAttachmentItem= VideoAttachmentItem.builder()
                        .id(message.getId())
                        .salveImgHeight(message.getImgHeight())
                        .salveImgWidth(message.getImgWidth())
                        .slaveImgPath(message.getSlavePath())
                        .videoLength(message.getPlayLength())
                        .videoPath(message.getFilePath())
                        .build();
                this.setMessageContent(message.getFilePath());
                break;
            case MSGTYPE_IMAGE:
            case MSGTYPE_EMOTICON:
                this.imageAttachment = ImageAttachmentItem.builder()
                        .description(message.getFileName())
                        .id(message.getId())
                        .imagePath(message.getFilePath())
                        .slavePath(message.getSlavePath())
                        .title(message.getFileName())
                        .width(message.getImgWidth())
                        .height(message.getImgHeight())
                        .build();
                break;
            case MSGTYPE_VOIPMSG:
            case MSGTYPE_VOIPNOTIFY:
            case MSGTYPE_VOIPINVITE:
            case MSGTYPE_LOCATION:
            case MSGTYPE_VERIFYMSG:
            case MSGTYPE_STATUSNOTIFY:
            case MSGTYPE_SYSNOTICE:
            case MSGTYPE_SYS:
            case MSGTYPE_RECALLED:
                //系统类消息
                isSystemMsg = true;
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

        setMessageType();
    }

    /**
     * 设置消息类型
     */
    private void setMessageType(){

        // 自己发的消息
        if (Core.getUserSelf().getUsername().equals(this.getSenderId())) {
            // 文件附件
            if (fileAttachment!=null) {
                this.setMessageType(RIGHT_ATTACHMENT);
            }
            // 图片消息
            else if (imageAttachment!=null) {
                this.setMessageType(RIGHT_IMAGE);
            }
            // 视频消息
            else if (videoAttachmentItem!=null){
                this.setMessageType(RIGHT_VIDEO);
            }
            else if (isSystemMsg){
                this.setMessageType(SYSTEM_MESSAGE);
            }else if(voiceAttachmentItem!= null){
                this.setMessageType(RIGHT_VOICE);
            }else if(linkAttachmentItem!= null){
                this.setMessageType(RIGHT_LINK);
            }
            // 普通文本消息
            else {
                this.setMessageType(RIGHT_TEXT);
            }

        } else {
            // 文件附件
            if (fileAttachment!=null) {
                this.setMessageType(LEFT_ATTACHMENT);
            }
            // 图片消息
            else if (imageAttachment!=null) {
                this.setMessageType(LEFT_IMAGE);
            }
            // 视频消息
            else if (videoAttachmentItem!=null){
                this.setMessageType(LEFT_VIDEO);
            }

            else if (isSystemMsg){
                this.setMessageType(SYSTEM_MESSAGE);
            }
            else if(voiceAttachmentItem!= null){
                this.setMessageType(LEFT_VOICE);
            }else if(linkAttachmentItem!= null){
                this.setMessageType(LEFT_LINK);
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

