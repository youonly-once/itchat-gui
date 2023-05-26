package cn.shu.wechat.typehandler;

import cn.shu.wechat.entity.Contacts;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
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
public class JSONStringTypeHandler<T extends Object> implements TypeHandler<List<T>> {

    /**
     * json字符串转list
     *
     * @param content 字符串
     * @return list
     */
    private List<T> getListByJsonArrayString(String content) {
        if (StringUtils.isEmpty(content)) {
            return new ArrayList<>();
        }
        return JSON.parseObject(content, new TypeReference<ArrayList<T>>() {
        });

    }

    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, List<T> ts, JdbcType jdbcType) throws SQLException {
        if (ts.isEmpty()) {
            preparedStatement.setString(i, null);
        } else {
            preparedStatement.setString(i, JSON.toJSONString(ts));
        }
    }

    @Override
    public List<T> getResult(ResultSet resultSet, String s) throws SQLException {
        return getListByJsonArrayString(resultSet.getString(s));
    }

    @Override
    public List<T> getResult(ResultSet resultSet, int i) throws SQLException {
        return getListByJsonArrayString(resultSet.getString(i));
    }

    @Override
    public List<T> getResult(CallableStatement callableStatement, int i) throws SQLException {
        return getListByJsonArrayString(callableStatement.getString(i));
    }
}
