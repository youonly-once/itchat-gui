package cn.shu.wechat.mapper;

import cn.shu.wechat.beans.pojo.Status;
import cn.shu.wechat.beans.pojo.StatusExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 7/1/2021 6:24 PM
 */
public interface StatusMapper {
    long countByExample(StatusExample example);

    int deleteByExample(StatusExample example);

    int deleteByPrimaryKey(String name);

    int insert(Status record);

    int insertOrUpdate(Status record);

    int insertOrUpdateSelective(Status record);

    int insertSelective(Status record);

    List<Status> selectByExample(StatusExample example);

    Status selectByPrimaryKey(String name);

    int updateByExampleSelective(@Param("record") Status record, @Param("example") StatusExample example);

    int updateByExample(@Param("record") Status record, @Param("example") StatusExample example);

    int updateByPrimaryKeySelective(Status record);

    int updateByPrimaryKey(Status record);

    int updateBatch(List<Status> list);

    int batchInsert(@Param("list") List<Status> list);
}