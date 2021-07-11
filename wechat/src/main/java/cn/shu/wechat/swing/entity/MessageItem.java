package cn.shu.wechat.swing.entity;

import cn.shu.wechat.swing.app.Launcher;
import cn.shu.wechat.swing.db.model.FileAttachment;
import cn.shu.wechat.swing.db.model.ImageAttachment;
import cn.shu.wechat.swing.db.model.Message;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by song on 20/03/2017.
 */

@Data
public class MessageItem implements Comparable<MessageItem>
{
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

    /*List<FileAttachmentItem> fileAttachments = new ArrayList<>();
    List<ImageAttachmentItem> imageAttachments = new ArrayList<>();*/

    private FileAttachmentItem fileAttachment;
    private ImageAttachmentItem imageAttachment;

    public MessageItem()
    {
    }

    public MessageItem(Message message, String currentUserId)
    {
        this();
        this.setId(message.getId());
        this.setMessageContent(message.getMessageContent());
        this.setGroupable(message.isGroupable());
        this.setRoomId(message.getRoomId());
        this.setSenderId(message.getSenderId());
        this.setSenderUsername(message.getSenderUsername());
        this.setTimestamp(message.getTimestamp());
        this.setUpdatedAt(message.getUpdatedAt());
        this.setNeedToResend(message.isNeedToResend());
        this.setProgress(message.getProgress());
        this.setDeleted(message.isDeleted());

        boolean isFileAttachment = false;
        boolean isImageAttachment = false;

        if (message.getFileAttachmentId() != null)
        {
            isFileAttachment = true;

            FileAttachment fa = Launcher.fileAttachmentService.findById(message.getFileAttachmentId());
            this.fileAttachment = new FileAttachmentItem(fa);
        }
        if (message.getImageAttachmentId() != null)
        {
            isImageAttachment = true;

            ImageAttachment ia = new ImageAttachment();
            ia.setDescription("DESC");
            ia.setHeight(500);
            ia.setWidth(400);
            ia.setTitle("sasd");
            ia.setImagesize(200);
            ia.setId(UUID.randomUUID().toString());
            ia.setImageUrl(message.getImageAttachmentId());
            this.imageAttachment = new ImageAttachmentItem(ia);
        }

        /*for (FileAttachment fa : message.getFileAttachments())
        {
            this.fileAttachments.add(new FileAttachmentItem(fa));
        }

        for (ImageAttachment ia : message.getImageAttachments())
        {
            this.imageAttachments.add(new ImageAttachmentItem(ia));
        }*/

        if (message.isSystemMessage())
        {
            this.setMessageType(SYSTEM_MESSAGE);
        }
        else
        {
            // 自己发的消息
            if (message.getSenderId().equals(currentUserId))
            {
                // 文件附件
                if (isFileAttachment)
                {
                    this.setMessageType(RIGHT_ATTACHMENT);
                }
                // 图片消息
                else if (isImageAttachment)
                {
                    this.setMessageType(RIGHT_IMAGE);
                }
                // 普通文本消息
                else
                {
                    this.setMessageType(RIGHT_TEXT);
                }
            }
            else
            {
                // 文件附件
                if (isFileAttachment)
                {
                    this.setMessageType(LEFT_ATTACHMENT);
                }
                // 图片消息
                else if (isImageAttachment)
                {
                    this.setMessageType(LEFT_IMAGE);
                }
                // 普通文本消息
                else
                {
                    this.setMessageType(LEFT_TEXT);
                }
            }
        }
    }

    @Override
    public int compareTo( MessageItem o)
    {
        return (int) (this.getTimestamp() - o.getTimestamp());

    }
}

