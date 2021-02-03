package database.sqlserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

import lombok.extern.log4j.Log4j2;
import lombok.extern.log4j.Log4j2;
import utils.Log;
/**
 * 采用纯JAVA的JDBC驱动程序与Sqlserver数据库连接
 * @author 11307
 *
 */
@Log4j2
public class ConnectSqlserPool127 implements ConnectDataBaseInter{
	private final String url = "jdbc:sqlserver://localhost:1433;databasename=Test";
	private final String username = "sa";
	private final String password = "aass";
	//数据库连接池
	private static final  LinkedList<Connection> connectPool=new LinkedList<Connection>();
	//单例
	private final static ConnectDataBaseInter CONNECT_SQLSER=new ConnectSqlserPool127();
	//获取单例
	public static ConnectDataBaseInter getInstance(){
		return CONNECT_SQLSER;
	}
	/**
	 * 初始化
	 */
	private ConnectSqlserPool127 (){
		initConnect();

	}
	/**
	 * 初始化数据库连接池
	 */
	private void initConnect(){
		try {
			for(int i=0;i<minConnectCount;i++){
				log.info("创建数据库连接池:"+(i+1));
				connectPool.add(buildConnect());
			}
		} catch ( SQLException e) {
			log.info("接数据库失败：" + e.toString() + "\n");
			e.printStackTrace();
		}catch (ClassNotFoundException e) {
			log.info("接数据库失败：" + e.toString() + "\n");
			e.printStackTrace();
		}
	}
	/**
	 * 创建数据库连接
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private Connection buildConnect() throws ClassNotFoundException,SQLException{

		// **************第一步：加载ODBC驱动********
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		// **************第二步：连接数据库****************

		return DriverManager.getConnection(url, username,password);
	}
	/**
	 * 获取连接池中的对象
	 */
	public synchronized Connection getConnect() throws ClassNotFoundException,SQLException{

		//没有连接数了
		if(connectPool.isEmpty()){
			//System.out.println(connectPool.size()+"");
			return buildConnect();

		}else {
			Connection connection=connectPool.removeFirst();
			if(connection.isValid(5)){
				//System.out.println(connectPool.size()+"");
				return connection;
			}else{
				//System.out.println(connectPool.size()+"");

				return buildConnect();
			}

		}

	}
	public synchronized void close(Connection connection){
		if(connection==null){
			return;
		}
		if(connectPool.size()<maxConnectCount){
			connectPool.add(connection);
			//System.out.println(connectPool.size()+"");
		}else{
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
