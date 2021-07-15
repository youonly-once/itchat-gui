package cn.shu.wechat.swing.db.service;

import cn.shu.wechat.swing.db.dao.FileAttachmentDao;
import cn.shu.wechat.swing.db.model.FileAttachment;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

/**
 * Created by song on 08/06/2017.
 */
public class FileAttachmentService extends BasicService<FileAttachmentDao, FileAttachment> {
    public FileAttachmentService(SqlSession session) {
        dao = new FileAttachmentDao(session);
        super.setDao(dao);
    }

    public int insertOrUpdate(FileAttachment attachment) {
        if (exist(attachment.getId())) {
            return update(attachment);
        } else {
            return insert(attachment);
        }
    }

    public List<FileAttachment> search(String key) {
        return dao.search(key);
    }
}
