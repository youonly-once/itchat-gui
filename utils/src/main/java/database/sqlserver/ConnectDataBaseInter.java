package database.sqlserver;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author ShuXinSheng
 * @version 创建时间：2020年3月20日 上午11:17:29
 * 类说明
 */
public interface ConnectDataBaseInter {
	//数据库连接池最小连接数
	public static final int minConnectCount=0;
	//数据库连接池最大连接数
	public static final  int maxConnectCount=2;

	/**
	 * 外部获取一个数据库连接
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public abstract Connection getConnect()throws ClassNotFoundException,SQLException;


	/**
	 * 外部关闭数据库连接
	 * @param connection
	 */
	public abstract void close(Connection connection);

}
