package cn.shu.wechat.swing.entity;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.api.DownloadTools;
import cn.shu.wechat.beans.msg.url.WXMsgUrl;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.enums.WXReceiveMsgCodeEnum;
import cn.shu.wechat.enums.WXReceiveMsgCodeOfAppEnum;
import cn.shu.wechat.utils.XmlStreamUtil;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.client.utils.DateUtils;

import java.awt.image.BufferedImage;
import java.util.Map;

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
    public static final int LEFT_PROGRAM_OF_APP = 7;

    public static final int RIGHT_TEXT = -1;
    public static final int RIGHT_IMAGE = -2;
    public static final int RIGHT_ATTACHMENT = -3;
    public static final int RIGHT_VIDEO = -4;
    public static final int RIGHT_VOICE = -5;
    public static final int RIGHT_LINK = -6;
    public static final int RIGHT_PROGRAM_OF_APP = -7;

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
    private ProgramOfAppItem programOfAppItem;
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

        this.setTimestamp(DateUtils.parseDate(message.getCreateTime()).getTime());
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
                Map<String, Object> stringObjectMap = message.getContentMap();
                switch (WXReceiveMsgCodeOfAppEnum.getByCode(message.getAppMsgType())) {
                    case OTHER:
                        break;
                    case LINK:
                        Object desc = stringObjectMap.get("msg.appmsg.des");
                        Object url = stringObjectMap.get("msg.appmsg.url");
                        Object title = stringObjectMap.get("msg.appmsg.title");
                        Object thumbUrl = stringObjectMap.get("msg.appmsg.thumburl");
                        Object sourceName = stringObjectMap.get("msg.appmsg.sourcedisplayname");
                        BufferedImage image = null;
                        if (thumbUrl == null|| thumbUrl.toString().isEmpty()){
                            image = DownloadTools.downloadImgByMsgID(message.getMsgId(),WXMsgUrl.SLAVE_TYPE);
                        }


                        linkAttachmentItem = LinkAttachmentItem.builder()
                                .desc(desc == null?"":desc.toString())
                                .thumbUrl(thumbUrl == null?"":thumbUrl.toString())
                                .image(image)
                                .id(message.getId())
                                .title(title == null?"":title.toString())
                                .url(url == null?"":url.toString())
                                .sourceName(sourceName == null?"":sourceName.toString())
                                .build();
                        break;
                    case PICTURE:
                        Object height = stringObjectMap.get("msg.appmsg.appattach.cdnthumbheight");
                        Object width = stringObjectMap.get("msg.appmsg.appattach.cdnthumbwidth");
                        url = stringObjectMap.get("msg.appmsg.url");
                        title = stringObjectMap.get("msg.appmsg.title");
                        thumbUrl = stringObjectMap.get("msg.appmsg.thumburl");
                        Object sourceIconUrl = stringObjectMap.get("msg.appmsg.weappinfo.weappiconurl");
                        sourceName = stringObjectMap.get("msg.appinfo.appname");
                        programOfAppItem = ProgramOfAppItem.builder()
                                .imageUrl(thumbUrl==null?"":thumbUrl.toString())
                                .id(message.getId())
                                .title(title == null?"":"[小程序]"+title.toString())
                                .url(url == null?"":url.toString())
                                .sourceIconUrl(sourceIconUrl == null?"":sourceIconUrl.toString())
                                .msgId(message.getMsgId())
                                .imageHeight(height==null?0:Integer.parseInt(height.toString()))
                                .imageWidth(width==null?0:Integer.parseInt(width.toString()))
                                .sourceName(sourceName == null?"":sourceName.toString()).build();
                        break;
                    case PROGRAM:
                        height = stringObjectMap.get("msg.appmsg.appattach.cdnthumbheight");
                        width = stringObjectMap.get("msg.appmsg.appattach.cdnthumbwidth");
                       url = stringObjectMap.get("msg.appmsg.url");
                        title = stringObjectMap.get("msg.appmsg.title");
                        thumbUrl = stringObjectMap.get("msg.appmsg.thumburl");
                        sourceIconUrl = stringObjectMap.get("msg.appmsg.weappinfo.weappiconurl");
                        sourceName = stringObjectMap.get("msg.appmsg.sourcedisplayname");
                        programOfAppItem = ProgramOfAppItem.builder()
                                .imageUrl(thumbUrl==null?"":thumbUrl.toString())
                                .id(message.getId())
                                .title(title == null?"":"[小程序]"+title.toString())
                                .url(url == null?"":url.toString())
                                .sourceIconUrl(sourceIconUrl == null?"":sourceIconUrl.toString())
                                .msgId(message.getMsgId())
                                .imageHeight(height==null?0:Integer.parseInt(height.toString()))
                                .imageWidth(width==null?0:Integer.parseInt(width.toString()))
                                .sourceName(sourceName == null?"":sourceName.toString()).build();
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
            //小程序消息
            else if (programOfAppItem != null){
                this.setMessageType(RIGHT_PROGRAM_OF_APP);
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
            //小程序消息
            else if (programOfAppItem != null){
                this.setMessageType(LEFT_PROGRAM_OF_APP);
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
    @Data
    @Builder
    public static class FileAttachmentItem {
        private String id;
        private String fileName;
        private String filePath;
        private String description;
        private String slavePath;
        private Long fileSize;

    }
    @Data
    @Builder
    public static class ImageAttachmentItem {
        private String id;
        private String title;
        private String description;
        private String slavePath;
        private String imagePath;
        private int width;
        private int height;
    }
    @Data
    @Builder
    public static class LinkAttachmentItem {
        /**
         * 消息ID
         */
        private String id;
        /**
         * 缩略图地址
         */
        private String thumbUrl;
        /**
         * 描述
         */
        private String desc;
        /**
         * 标题
         */
        private String title;

        /**
         * 链接地址
         */
        private String url;

        /**
         * 来源名词
         */
        private String sourceName;

        /**
         * 图标Image
         */
        private BufferedImage image;




    }
    @Data
    @Builder
    public static class ProgramOfAppItem {
        /**
         * 消息ID
         */
        private String id;

        /**
         * MsgId微信消息ID
         */
        private String msgId;

        /**
         * 标题
         */
        private String title;

        /**
         * 链接地址
         */
        private String url;

        /**
         * APP名称
         */
        private String sourceName;

        /**
         * APP图标地址
         */
        private String sourceIconUrl;

        /**
         * 图片地址
         */
        private String imageUrl;

        /**
         * 图片宽度
         */
        private int imageWidth;
        /**
         * 图片高度
         */
        private int imageHeight;
    }
    /**
     *
     * @author 舒新胜
     * @date 17/05/2017
     */
    @Data
    @Builder
    public static class VideoAttachmentItem {
        /**
         * 消息ID
         */
        private String id;
        /**
         * 缩略图路径
         */
        private String slaveImgPath;
        /**
         * 缩略图宽度
         */
        private int salveImgWidth;
        /**
         * 缩略图高度
         */
        private int salveImgHeight;
        /**
         * 视频路径
         */
        private String videoPath;
        /**
         * 视频长度 单位s
         */
        private long videoLength;

    }
    /**
     *
     * @author 舒新胜
     * @date 17/05/2017
     */
    @Data
    @Builder
    public  static class VoiceAttachmentItem {
        /**
         * 消息ID
         */
        private String id;
        /**
         * 视频路径
         */
        private String voicePath;
        /**
         * 视频长度 单位毫秒
         */
        private long voiceLength;

    }
}

