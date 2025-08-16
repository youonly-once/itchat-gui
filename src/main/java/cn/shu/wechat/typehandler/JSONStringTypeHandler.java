package cn.shu.wechat.typehandler;

import cn.shu.wechat.entity.Contacts;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@MappedTypes(value = {Contacts.class})
@MappedJdbcTypes({JdbcType.VARCHAR})
@Component
public class JSONStringTypeHandler extends BaseTypeHandler<List<Contacts>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Contacts> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, JSON.toJSONString(parameter));
    }

    @Override
    public List<Contacts> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJson(rs.getString(columnName));
    }

    @Override
    public List<Contacts> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJson(rs.getString(columnIndex));
    }

    @Override
    public List<Contacts> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJson(cs.getString(columnIndex));
    }

    private List<Contacts> parseJson(String json) {
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            return JSON.parseObject(json, new TypeReference<List<Contacts>>() {
            });
        } catch (Exception e) {
            // 可记录日志，避免查询失败导致SQL抛异常
            return new ArrayList<>();
        }
    }
}