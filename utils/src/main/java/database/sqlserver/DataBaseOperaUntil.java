package database.sqlserver;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.extern.log4j.Log4j2;
import utils.DateUtil;
import utils.Log;

/**
 * @author ShuXinSheng
 * @version 创建时间：2019年2月16日 上午11:42:33 类说明
 */

@Log4j2
public class DataBaseOperaUntil {

    /**
     * 数据库查询操作
     */
    public static List<Map<String, String>> databaseQuery(String sql, ConnectDataBaseInter ConnectDataBaseInter) {
        // 若报错没有返回结果集，需要再存储过程begin后添加:SET NOCOUNT ON
        Connection con = null;
        Statement statement = null;
        ResultSet result = null;

        try {
            con = ConnectDataBaseInter.getConnect();
            statement = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            if (statement == null) {
                log.info("error：statement is null");
                return null;
            }
            result = statement.executeQuery(sql);

            return ResultSet2Map(result);
        } catch (ClassNotFoundException | SQLException | NullPointerException e) {

            e.printStackTrace();
            log.info("error：" + e.getMessage());
            return null;
        } finally {
            close(statement, result);
            ConnectDataBaseInter.close(con);
        }
    }

    /*
     * 数据库查询操作
     */
    public static List<Map<String, String>> databaseQueryProcedure(String procedure,
                                                                   ConnectDataBaseInter connectSqlserPool,HashMap<Integer, String> parammeters) {
        // 若报错没有返回结果集，需要再存储过程begin后添加:SET NOCOUNT ON
        Connection con = null;
        CallableStatement statement = null;
        ResultSet result = null;

        try {
            con = connectSqlserPool.getConnect();
            statement = con.prepareCall(procedure);// 存储过程
            if (statement == null) {
                log.info("error：statement is null");
                return null;
            }
            if (parammeters!=null) {
                for (Entry<Integer, String> entry : parammeters.entrySet()) {
                    statement.setString(entry.getKey(), entry.getValue());//设置存储过程参数
                }
            }

            result = statement.executeQuery();
            return ResultSet2Map(result);
        } catch (ClassNotFoundException | SQLException | NullPointerException e) {

            e.printStackTrace();
            log.info("error：" + e.getMessage());
            return null;
        } finally {
            close(statement, result);
            connectSqlserPool.close(con);
        }
    }

    /*
     * 数据库更新操作
     */
    public static boolean databaseUpdate(String sql, ConnectDataBaseInter  connectDataBaseInter) {
        // 若报错没有返回结果集，需要再存储过程begin后添加:SET NOCOUNT ON
        Connection con = null;
        Statement statement = null;
        int result = 0;

        try {
            con = connectDataBaseInter.getConnect();
            statement = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            if (statement == null) {
                log.info("error：statement is null");
                return false;
            }
            result = statement.executeUpdate(sql);
            if (result < 1) {
                log.info("error：result < 1");

                return false;
            }else{
                log.info("更新成功："+result);
            }
            return true;

        } catch (ClassNotFoundException | SQLException | NullPointerException e) {

            e.printStackTrace();
            log.info("error：" + e.getMessage());
            return false;
        } finally {
            close(statement, null);
            connectDataBaseInter.close(con);
        }
    }
    private static void close(Statement statement,ResultSet result){
        try {
            if (statement != null) {
                statement.close();
            }
            if (result != null) {
                result.close();
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    private static List<Map<String, String>> ResultSet2Map(ResultSet resultSet) throws SQLException {

        Map<String, String> map;
        List<Map<String, String>> list = new ArrayList<>();
        while (resultSet.next()) {
            map = new HashMap<>();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnCount = resultSetMetaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                map.put(resultSetMetaData.getColumnName(i), resultSet.getString(i));

            }
            list.add(map);
        }
        return list;
    }
    /*
     * 保存用户信息
     */
    public static boolean saveUserInfo(Map<String, Object> userInfo){

        String sql="select openid as c from weixin_users where openid='"+userInfo.get("openid")+"'";
        List<Map<String, String>> maps=DataBaseOperaUntil.databaseQuery(sql, ConnectSqlserPool127.getInstance());

        if (maps==null) {
            log.info("保存用户信息失败：" + userInfo);
            return false;
        }
        //数据库无记录，插入
        if (maps.isEmpty()) {
            log.info("新增用户信息：" + userInfo);
            sql="insert into weixin_users (openid,realname,nickname,sex ,province,city ,country,headimgurl,"+userInfo.get("EventKey")+") "
                    + "values('"
                    +userInfo.get("openid")+"','"
                    +userInfo.get("realname")+"','"
                    +userInfo.get("nickname")+"','"
                    +userInfo.get("sex")+"','"
                    +userInfo.get("province")+"','"
                    +userInfo.get("city")+"','"
                    +userInfo.get("country")+"','"
                    +userInfo.get("headimgurl")+"',1)";

            return DataBaseOperaUntil.databaseUpdate(sql, ConnectSqlserPool127.getInstance());
        }else{//更新用户信息
            log.info("更新用户信息：" + userInfo);
            sql="update weixin_users set "
                    +"realname='"+userInfo.get("realname")+"',"
                    +"nickname='"+userInfo.get("nickname")+"',"
                    +"sex='"+userInfo.get("sex")+"',"
                    +"province='"+userInfo.get("province")+"',"
                    +"city='"+userInfo.get("city")+"',"
                    +"country='"+userInfo.get("country")+"',"
                    +"headimgurl='"+userInfo.get("headimgurl")+"',"
                    +"updatedate='"+DateUtil.getCurrDateAndTimeMil()+"',"
                    +"updatecount=updatecount+1,"
                    +userInfo.get("EventKey")+"="+userInfo.get("EventKey")+"+1"
                    +" where openid='"+userInfo.get("openid")+"'";
            return DataBaseOperaUntil.databaseUpdate(sql, ConnectSqlserPool127.getInstance());
        }
    }
}
