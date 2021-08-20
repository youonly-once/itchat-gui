package cn.shu.wechat.beans.pojo;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String id;

    private String msgId;
    private boolean isGroup;
    private Integer msgType;

    private Integer appMsgType;

    private String msgDesc;

    private String createTime;

    private String plaintext;


    /**
     * 是否删除
     */
    private boolean deleted =false;
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

    /**
     * 是否是本人发送的消息1是0不是
     */
    private Boolean isSend;

    /**
     * 缩略图路径
     */
    private String slavePath;

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
    private long timestamp;
    private String desc ;
    private String url ;
    private String title;
    private String thumbUrl ;
    private String sourceIconUrl ;
    private  String sourceName ;
    /**
     * content map
     */
    private Map<String, Object> contentMap;

    private String plainName;
    private int progress = 100;
    private boolean isNeedToResend;
    @Override
    public int compareTo(Message o) {
        return (int) (this.getTimestamp() - o.getTimestamp());

    }
}