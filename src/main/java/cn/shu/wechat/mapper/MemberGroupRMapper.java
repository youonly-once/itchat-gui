package cn.shu.wechat.mapper;

import cn.shu.wechat.pojo.entity.MemberGroupR;
import cn.shu.wechat.pojo.entity.MemberGroupRExample;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 6/14/2021 7:47 PM
 */
public interface MemberGroupRMapper extends BaseMapper<MemberGroupR> {
    long countByExample(MemberGroupRExample example);

    int deleteByExample(MemberGroupRExample example);

    int deleteByPrimaryKey(String id);


    int insertOrUpdate(MemberGroupR record);

    int insertOrUpdateSelective(MemberGroupR record);

    int insertSelective(MemberGroupR record);

    List<MemberGroupR> selectByExample(MemberGroupRExample example);

    MemberGroupR selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") MemberGroupR record, @Param("example") MemberGroupRExample example);

    int updateByExample(@Param("record") MemberGroupR record, @Param("example") MemberGroupRExample example);

    int updateByPrimaryKeySelective(MemberGroupR record);

    int updateByPrimaryKey(MemberGroupR record);

    int updateBatch(List<MemberGroupR> list);

    int batchInsert(@Param("list") List<MemberGroupR> list);
}