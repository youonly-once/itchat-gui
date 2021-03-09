package cn.shu.wechat.beans.pojo;

import java.util.Date;

import lombok.Builder;
import lombok.Data;

/**
 * @作者 舒新胜
 * @项目 weixin
 * @创建时间 3/9/2021 1:01 PM
 */
@Data
@Builder
public class Message {
    private String id;

    private String msgId;

    private Integer msgType;

    private Integer appMsgType;

    private String msgDesc;

    private Date createTime;

    private String content;

    private String msgJson;

    private String fromUsername;

    private String toUsername;

    private String fromRemarkname;

    private String toRemarkname;

    private String fromNickname;

    private String toNickname;

    private String fromMemberOfGroupNickname;

    private String fromMemberOfGroupDisplayname;

    private String fromMemberOfGroupUsername;

    /**
     * 是否是本人发送的消息1是0不是
     */
    private Boolean isSend;
}