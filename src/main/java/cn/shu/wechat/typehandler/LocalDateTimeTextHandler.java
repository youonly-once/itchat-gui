package cn.shu.wechat.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(LocalDateTime.class)
@Component
public class LocalDateTimeTextHandler extends BaseTypeHandler<LocalDateTime> {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter FORMATTER_OLD =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i,
                                    LocalDateTime parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.format(FORMATTER));
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String str = rs.getString(columnName);
        try {
            return str != null ? LocalDateTime.parse(str, FORMATTER) : null;
        } catch (DateTimeParseException e) {
            return LocalDateTime.parse(str, FORMATTER_OLD);
        }
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String str = rs.getString(columnIndex);
        try {
            return str != null ? LocalDateTime.parse(str, FORMATTER) : null;
        } catch (DateTimeParseException e) {
            return LocalDateTime.parse(str, FORMATTER_OLD);
        }
    }

    @Override
    public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String str = cs.getString(columnIndex);
        try {
            return str != null ? LocalDateTime.parse(str, FORMATTER) : null;
        } catch (DateTimeParseException e) {
            return LocalDateTime.parse(str, FORMATTER_OLD);
        }
    }
}
