package cn.shu.wechat.mapper;

import cn.shu.wechat.entity.Contacts;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 6/14/2021 7:41 PM
 */
public interface ContactsMapper extends BaseMapper<Contacts> {


    int deleteByPrimaryKey(String username);

    int insertOrUpdate(Contacts record);

    int insertOrUpdateSelective(Contacts record);

    int insertSelective(Contacts record);

    Contacts selectByPrimaryKey(String username);

    int updateByPrimaryKeySelective(Contacts record);

    int updateByPrimaryKey(Contacts record);

    int updateBatch(List<Contacts> list);

    int batchInsert(@Param("list") List<Contacts> list);

    List<Contacts> selectGroupMember(String groupName);
}