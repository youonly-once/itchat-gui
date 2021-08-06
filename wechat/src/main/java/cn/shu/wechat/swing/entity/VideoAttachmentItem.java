package cn.shu.wechat.swing.entity;

import cn.shu.wechat.swing.db.model.ImageAttachment;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author 舒新胜
 * @date 17/05/2017
 */
@Data
@Builder
public class VideoAttachmentItem {
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