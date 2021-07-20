package cn.shu.wechat.swing.db.service;

import cn.shu.wechat.swing.db.dao.CurrentUserDao;
import cn.shu.wechat.swing.db.model.CurrentUser;
import org.apache.ibatis.session.SqlSession;

/**
 * Created by 舒新胜 on 08/06/2017.
 */
public class CurrentUserService extends BasicService<CurrentUserDao, CurrentUser> {
    public CurrentUserService(SqlSession session) {
        dao = new CurrentUserDao(session);
        super.setDao(dao);
    }

    public int insertOrUpdate(CurrentUser currentUser) {
        if (exist(currentUser.getUserId())) {
            return update(currentUser);
        } else {
            return insert(currentUser);
        }
    }

    /*@Override
    public List<CurrentUser> findAll()
    {
        // TODO: 从数据库获取当前登录用户
        List<CurrentUser> list = new ArrayList();
        list.add(new CurrentUser("Ni7bJcX3W8yExKSa3", "舒新胜", "", "", "", "", "舒新胜", "", ""));
        return list;
    }*/
}
