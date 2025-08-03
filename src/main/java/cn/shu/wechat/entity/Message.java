package cn.shu.wechat.entity;

import cn.shu.wechat.typehandler.LocalDateTimeTextHandler;
import cn.shu.wechat.typehandler.MapToJsonTypeHandler;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 8/7/2021 12:31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Comparable<Message>{

    @TableId
    private String id;

    private String msgId;

    @TableField(exist = false)
    private boolean isGroup;

    private Integer msgType;

    private Integer appMsgType;

    private String msgDesc;

    @TableField(value = "create_time", typeHandler = LocalDateTimeTextHandler.class)
    private LocalDateTime createTime;

    private String plaintext;


    /**
     * 是否删除
     */
    private boolean deleted =false;
    /**
     * 消息内容
     */
    private String content;


    private String oriContent;
    /**
     * 资源文件保存路径
     */
    private String filePath;

    private String msgJson;

    private String fromUsername;

    private String fromRemarkname;

    private String fromNickname;

    private String fromMemberOfGroupUsername;

    private String fromMemberOfGroupNickname;

    private String fromMemberOfGroupDisplayname;

    private String toUsername;

    private String toRemarkname;

    private String toNickname;

    /**
     * 是否是本人发送的消息1是0不是
     */
    private Boolean isSend;

    /**
     * 缩略图路径
     */
    private String slavePath;
    /**
     * 视频缩略图
     */
    @TableField(exist = false)
    @JSONField(serialize = false)
    private transient BufferedImage videoPic;


    /**
     * 消息发送结果
     */
    private String response;

    /**
     * 视频长度 秒
     */
    private Long playLength;

    /**
     * 缩略图高度
     */
    private Integer imgHeight;

    /**
     * 缩略图宽度
     */
    private Integer imgWidth;

    /**
     * 语音长度 毫秒
     */
    private Long voiceLength;
    private String fileName;
    private Long fileSize;
    /**
     * 消息时间
     */
    @TableField(value = "message_time", typeHandler = LocalDateTimeTextHandler.class)
    private LocalDateTime messageTime;


    private String desc ;

    private String url ;


    private String title;

    private String thumbUrl ;

    private String sourceIconUrl ;


    private  String sourceName ;
    /**
     * content map
     */
    @TableField(typeHandler = MapToJsonTypeHandler.class)
    private Map<String, Object> contentMap;


    private boolean isRevoke;

    private String plainName;

    private int progress = 100;

    private boolean isNeedToResend;

    //联系人卡片消息


    private String contactsUserName;

    private String contactsNickName;

    private String contactsId;

    private Byte contactsSex;

    private String contactsProvince;

    private String contactsCity;

    private String contactsHeadImgUrl;

    private String contactsTicket;

    /**
     * 发送当前消息的线程ID
     */
    private long threadId;
    @Override
    public int compareTo(Message o) {
        return this.getMessageTime().compareTo(o.getMessageTime());
    }
}