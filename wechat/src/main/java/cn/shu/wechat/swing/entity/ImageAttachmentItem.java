package cn.shu.wechat.swing.entity;

import cn.shu.wechat.swing.db.model.ImageAttachment;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by 舒新胜 on 17/05/2017.
 */
@Data
@Builder
public class ImageAttachmentItem {
    private String id;
    private String title;
    private String description;
    private String slavePath;
    private String imagePath;
    private int width;
    private int height;
}