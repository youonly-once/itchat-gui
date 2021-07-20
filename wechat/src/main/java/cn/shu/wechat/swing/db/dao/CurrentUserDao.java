package cn.shu.wechat.swing.db.dao;

import org.apache.ibatis.session.SqlSession;

/**
 * Created by 舒新胜 on 08/06/2017.
 */
public class CurrentUserDao extends BasicDao {
    public CurrentUserDao(SqlSession session) {
        super(session, CurrentUserDao.class);
    }
}