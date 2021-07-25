package cn.shu.wechat.beans.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @作者 舒新胜
 * @项目 AutoWeChat
 * @创建时间 3/10/2021 10:36 PM
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String id;

    private String msgId;

    private Integer msgType;

    private Integer appMsgType;

    private String msgDesc;

    private Date createTime;

    /**
     * 消息内容
     */
    private String content;

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
    private String plaintext;
    /**
     * 是否是本人发送的消息1是0不是
     */
    private Boolean isSend = true;

    /**
     * 消息发送进度
     */
    private int process = 100;

    /**
     * 是否删除
     */
    private boolean deleted =false;

    /**
     * 缩略图Url
     */
    private String slavePath;

    /**
     * 消息发送结果
     */
    private String response;

}