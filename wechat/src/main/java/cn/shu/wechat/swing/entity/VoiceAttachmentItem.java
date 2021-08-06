package cn.shu.wechat.swing.entity;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author 舒新胜
 * @date 17/05/2017
 */
@Data
@Builder
public class VoiceAttachmentItem {
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