package cn.shu.wechat.mapper;

import cn.shu.wechat.beans.pojo.Contacts;
import cn.shu.wechat.beans.pojo.ContactsExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 6/14/2021 7:41 PM
 */
public interface ContactsMapper {
    long countByExample(ContactsExample example);

    int deleteByExample(ContactsExample example);

    int deleteByPrimaryKey(String username);

    int insert(Contacts record);

    int insertOrUpdate(Contacts record);

    int insertOrUpdateSelective(Contacts record);

    int insertSelective(Contacts record);

    List<Contacts> selectByExample(ContactsExample example);

    Contacts selectByPrimaryKey(String username);

    int updateByExampleSelective(@Param("record") Contacts record, @Param("example") ContactsExample example);

    int updateByExample(@Param("record") Contacts record, @Param("example") ContactsExample example);

    int updateByPrimaryKeySelective(Contacts record);

    int updateByPrimaryKey(Contacts record);

    int updateBatch(List<Contacts> list);

    int batchInsert(@Param("list") List<Contacts> list);

    List<Contacts> selectGroupMember(String groupName);
}