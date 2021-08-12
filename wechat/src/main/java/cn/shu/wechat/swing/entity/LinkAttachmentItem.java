package cn.shu.wechat.swing.entity;

import lombok.Builder;
import lombok.Data;

import java.awt.image.BufferedImage;

/**
 *
 * @author 舒新胜
 * @date 17/05/2017
 */
@Data
@Builder
public class LinkAttachmentItem {
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