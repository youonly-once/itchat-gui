package cn.shu.wechat.swing.db.service;

import cn.shu.wechat.swing.db.dao.ImageAttachmentDao;
import cn.shu.wechat.swing.db.model.ImageAttachment;
import org.apache.ibatis.session.SqlSession;

/**
 * Created by 舒新胜 on 08/06/2017.
 */
public class ImageAttachmentService extends BasicService<ImageAttachmentDao, ImageAttachment> {
    public ImageAttachmentService(SqlSession session) {
        dao = new ImageAttachmentDao(session);
        super.setDao(dao);
    }

    public int insertOrUpdate(ImageAttachment attachment) {
        if (exist(attachment.getId())) {
            return update(attachment);
        } else {
            return insert(attachment);
        }
    }

}
