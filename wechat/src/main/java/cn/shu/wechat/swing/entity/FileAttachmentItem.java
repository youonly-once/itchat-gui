package cn.shu.wechat.swing.entity;

import cn.shu.wechat.swing.db.model.FileAttachment;
import lombok.Data;

/**
 * Created by 舒新胜 on 17/05/2017.
 */

@Data
public class FileAttachmentItem {
    private String id;
    private String title;
    private String link;
    private String description;
    private String slavePath;

    public FileAttachmentItem() {

    }

    public FileAttachmentItem(String link) {

        this.link = link;
    }

    public FileAttachmentItem(FileAttachment fa) {
        this.id = fa.getId();
        this.title = fa.getTitle();
        this.link = fa.getLink();
        this.description = fa.getDescription();
    }

    /*public FileAttachmentItem(FileAttachment fa)
    {
        this.id = fa.getId();
        this.title = fa.getTitle();
        this.link = fa.getLink();
        this.description = fa.getDescription();
    }*/


}
