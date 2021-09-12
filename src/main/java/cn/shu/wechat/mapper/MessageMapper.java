package cn.shu.wechat.mapper;

import cn.shu.wechat.pojo.entity.Message;
import cn.shu.wechat.pojo.entity.MessageExample;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 8/7/2021 12:31
 */
public interface MessageMapper extends BaseMapper<Message> {
    long countByExample(MessageExample example);

    int deleteByExample(MessageExample example);

    int deleteByPrimaryKey(String id);

    int insertOrUpdate(Message record);

    int insertOrUpdateSelective(Message record);

    int insertSelective(Message record);

    List<Message> selectByExample(MessageExample example);

    Message selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") Message record, @Param("example") MessageExample example);

    int updateByExample(@Param("record") Message record, @Param("example") MessageExample example);

    int updateByPrimaryKeySelective(Message record);

    int updateByPrimaryKey(Message record);

    int updateBatch(List<Message> list);

    int batchInsert(@Param("list") List<Message> list);

    int updateBatchSelective(List<Message> list);

    Message selectLastMessage(String roomId);

    List<Message> selectByPage(@Param("start") int start
            , @Param("end") int end
            , @Param("userName") String userName
            , @Param("remarkName") String remarkName
            , @Param("nickName") String nickName);
}