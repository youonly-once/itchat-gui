package cn.shu.wechat.swing.db.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VideoAttachment extends BasicModel {
    private String id;
    private String title;
    private String description;
    /**
     * 缩略图地址
     */
    private String slavePath;
    /**
     * 完整图片本地地址
     */
    private String imagePath;
    private int width;
    private int height;
    private long imagesize;



}
