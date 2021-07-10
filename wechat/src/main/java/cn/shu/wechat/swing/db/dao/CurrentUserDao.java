package cn.shu.wechat.swing.db.dao;

import cn.shu.wechat.swing.db.model.CurrentUser;
import cn.shu.wechat.swing.db.service.CurrentUserService;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

/**
 * Created by song on 08/06/2017.
 */
public  class CurrentUserDao extends BasicDao
{
    public CurrentUserDao(SqlSession session)
    {
        super(session, CurrentUserDao.class);
    }
}