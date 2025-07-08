package cn.shu.wechat.mapper;

import cn.shu.wechat.entity.LoginInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LoginInfoMapper {

    // 插入记录
    int insert(LoginInfo loginInfo);

    // 根据主键查询（假设 uuid 作为主键）
    LoginInfo selectByUuid(@Param("uuid") String uid);

    // 查询全部
    List<LoginInfo> selectAll();

    // 根据主键更新记录
    int updateByUuid(LoginInfo loginInfo);

    // 根据主键删除记录
    int deleteByUuid(@Param("uuid") String uuid);
}
