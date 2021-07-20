package cn.shu.wechat.swing.entity;

import cn.shu.wechat.swing.db.model.ImageAttachment;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by 舒新胜 on 17/05/2017.
 */
@Data
@NoArgsConstructor
public class VideoAttachmentItem {
    private String id;
    private String title;
    private String description;
    private String slavePath;
    private String imagePath;
    private int width;
    private int height;
    private long imagesize;

    public VideoAttachmentItem(String imageUrl) {
        this.imagePath = imageUrl;
    }

    public VideoAttachmentItem(ImageAttachment ia) {
        this.id = ia.getId();
        this.title = ia.getTitle();
        this.description = ia.getDescription();
        this.slavePath = ia.getSlavePath();
        this.imagePath = ia.getImagePath();
        this.width = ia.getWidth();
        this.height = ia.getHeight();
        this.imagesize = ia.getImagesize();
    }

}