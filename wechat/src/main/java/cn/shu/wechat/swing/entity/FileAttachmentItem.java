package cn.shu.wechat.swing.entity;

import cn.shu.wechat.swing.db.model.FileAttachment;
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
public class FileAttachmentItem {
    private String id;
    private String fileName;
    private String filePath;
    private String description;
    private String slavePath;
    private Long fileSize;

}
