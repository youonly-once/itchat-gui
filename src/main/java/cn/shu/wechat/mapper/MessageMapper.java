package cn.shu.wechat.mapper;

import cn.shu.wechat.entity.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 8/7/2021 12:31
 */
public interface MessageMapper extends BaseMapper<Message> {

    int deleteByPrimaryKey(String id);

    int insertOrUpdate(Message record);

    int insertOrUpdateSelective(Message record);

    int insertSelective(Message record);

    Message selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Message record);

    int updateByPrimaryKey(Message record);

    int updateBatch(List<Message> list);

    int batchInsert(@Param("list") List<Message> list);

    Message selectLastMessage(String roomId);

    List<Message> selectByPage(@Param("start") int start
            , @Param("end") int end
            , @Param("userName") String userName
            , @Param("remarkName") String remarkName
            , @Param("nickName") String nickName);

    /**
     * 搜索文件
     * @param key 关键词
     * @return 消息列表
     */
    List<Message> searchFileByName(String key);

    /**
     * 查询群成员消息数量
     * @param fromUserName 群Username
     * @param fromNickName 群昵称
     * @param fromRemarkName 群备注
     * @return list
     */
    List<Map<String,Object>> selectGroupUserMessageCount(@Param("fromUserName") String fromUserName,
                                                   @Param("fromNickName") String fromNickName,
                                                   @Param("fromRemarkName") String fromRemarkName,
                                                   @Param("count") int count);

    /**
     * 查询用户发送消息数量
     * @param fromUserName Username
     * @param fromNickName 昵称
     * @param fromRemarkName 备注
     * @return list
     */
    List<Map<String,Object>> selectUserMessageCount(@Param("fromUserName") String fromUserName,
                                                   @Param("fromNickName") String fromNickName,
                                                   @Param("fromRemarkName") String fromRemarkName);

    /**
     * 按类型分组统计数据
     * @param fromUserName Username
     * @param fromNickName 昵称
     * @param fromRemarkName 备注
     * @return list
     */
    List<Map<String,Object>> groupByType(@Param("fromUserName") String fromUserName,
                                                    @Param("fromNickName") String fromNickName,
                                                    @Param("fromRemarkName") String fromRemarkName);

    /**
     * 按内容汇总统计数据
     * @param fromUserName Username
     * @param fromNickName 昵称
     * @param fromRemarkName 备注
     * @return list
     */
    List<Map<String,Object>> groupByContent(@Param("fromUserName") String fromUserName,
                                                    @Param("fromNickName") String fromNickName,
                                                    @Param("fromRemarkName") String fromRemarkName);

}