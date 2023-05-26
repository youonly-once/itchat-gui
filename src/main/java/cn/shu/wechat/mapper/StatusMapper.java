package cn.shu.wechat.mapper;

import cn.shu.wechat.entity.Status;
import cn.shu.wechat.entity.StatusExample;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 7/1/2021 6:24 PM
 */
public interface StatusMapper extends BaseMapper<Status> {
    long countByExample(StatusExample example);

    int deleteByExample(StatusExample example);

    int deleteByPrimaryKey(String name);

    int insertOrUpdate(Status record);

    int insertOrUpdateSelective(Status record);

    /**
     * SQLLite
     *
     * @param record
     * @return
     */
    int insertOrUpdateSelectiveForSqlite(Status record);

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