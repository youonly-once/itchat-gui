package cn.shu.wechat.mapper;

import cn.shu.wechat.pojo.entity.Member;
import cn.shu.wechat.pojo.entity.MemberExample;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 6/14/2021 6:42 PM
 */
public interface MemberMapper extends BaseMapper<Member> {
    long countByExample(MemberExample example);

    int deleteByExample(MemberExample example);

    int deleteByPrimaryKey(String username);

    int insertOrUpdate(Member record);

    int insertOrUpdateSelective(Member record);

    int insertSelective(Member record);

    List<Member> selectByExample(MemberExample example);

    Member selectByPrimaryKey(String username);

    int updateByExampleSelective(@Param("record") Member record, @Param("example") MemberExample example);

    int updateByExample(@Param("record") Member record, @Param("example") MemberExample example);

    int updateByPrimaryKeySelective(Member record);

    int updateByPrimaryKey(Member record);

    int updateBatch(List<Member> list);

    int batchInsert(@Param("list") List<Member> list);
}