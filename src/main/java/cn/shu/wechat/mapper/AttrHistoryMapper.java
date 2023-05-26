package cn.shu.wechat.mapper;

import cn.shu.wechat.entity.AttrHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @作者 舒新胜
 * @项目 weixin
 * @创建时间 2/19/2021 4:02 PM
 */
public interface AttrHistoryMapper extends BaseMapper<AttrHistory> {
    int deleteByPrimaryKey(Integer id);

    int insertOrUpdate(AttrHistory record);

    int insertOrUpdateSelective(AttrHistory record);

    int insertSelective(AttrHistory record);

    AttrHistory selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AttrHistory record);

    int updateByPrimaryKey(AttrHistory record);

    int updateBatch(List<AttrHistory> list);

    int batchInsert(@Param("list") List<AttrHistory> list);

    List<Map<String, Object>> selectUpdateInfoCount(int top);

    List<Map<String, Object>> selectUpdateAttrCount(int top);

    List<AttrHistory> selectByAll(AttrHistory record);
}