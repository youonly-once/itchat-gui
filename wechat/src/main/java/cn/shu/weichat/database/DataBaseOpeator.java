package cn.shu.weichat.database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import database.sqlserver.ConnectSqlserPool127;
import database.sqlserver.DataBaseOperaUntil;
import lombok.extern.log4j.Log4j2;
import cn.shu.weichat.server.WXServletConfig;
import utils.DateUtil;
import weixin.utils.WXUntil;

@Log4j2
public class DataBaseOpeator {
	private final static DataBaseOpeator dataBaseOpeator = new DataBaseOpeator();


	private DataBaseOpeator() {
		// TODO Auto-generated constructor stub
	}

	public static DataBaseOpeator getInstance() {
		return dataBaseOpeator;
	}

	/*
	 * 直接回复
	 */
	public String queryScanRecord(String code) {
		code=code.trim();//去掉空格
		if (!codeCheck(code)) {
			return "码格式不正确";
		}
		// 若报错没有返回结果集，需要再存储过程begin后添加:SET NOCOUNT ON
		log.info("查询" + code + "扫描记录");
		HashMap<Integer, String> parammeters=new HashMap<>();
		parammeters.put(1,code);
		String pro="{call dbo.QRCodeScan(?)}";
		//if (code.substring(11, 13).equals("PJ")) {
		//	pro="{call dbo.QRCodeScanCOSMO(?)}";
		//}
		List<Map<String, String>> maps=DataBaseOperaUntil.databaseQueryProcedure(pro, ConnectSqlserPool127.getInstance(),parammeters);
		StringBuffer msStringBuffer = new StringBuffer();
		for (Map<String, String> map : maps) {
			msStringBuffer.append(map.get("info").toString() + "\n");
		}
		return "查询编码：" + code + "\n" + msStringBuffer.toString();

	}

	/*
	 * 发送模板消息
	 */
	public HashMap<String, String> queryAnjianData(String code) {
		// 若报错没有返回结果集，需要再存储过程begin后添加:SET NOCOUNT ON
		code=code.trim();//去掉空格
		if (!codeCheck(code)) {

			return WXUntil.createDefDataMap("查询安检仪数据", "码格式不正确\n"+WXServletConfig.TIP_STR[4], "","","");
		}
		log.info("查询" + code + "安检数据");
		if (code==null||code.isEmpty()) {
			return WXUntil.createDefDataMap("查询安检仪数据", "格式错误\n"+WXServletConfig.TIP_STR[4], "","","");
		}
		String time = "";

		String time1 = null;
		String time2 = null;

		HashMap<Integer, String> parammeters=new HashMap<>();
		parammeters.put(1,code);
		List<Map<String, String>> maps=DataBaseOperaUntil.databaseQueryProcedure("{call dbo.QueryAnjianData(?)}",ConnectSqlserPool127.getInstance(),parammeters);

		StringBuffer msStringBuffer = new StringBuffer();
		for (Map<String, String> map : maps) {
			msStringBuffer.append(map.get("test") + "数据：" + map.get("testdata") + "，结果："
					+ map.get("result") + "\n");
			if (map.get("test").substring(2).equals("1")) {
				time1 = "安检一：" + map.get("testdate");
			} else if (map.get("test").substring(2).equals("2")) {
				time2 = "安检二：" + map.get("testdate");
			}
		}
		if(msStringBuffer.length()==0){
			msStringBuffer.append("\n无安检仪检测数据\n");
		}
		//log.info(msStringBuffer.toString());
		time = "最近一次的测试数据\n" + time1 + "\n" + time2;
		return WXUntil.createDefDataMap("查询安检仪数据", msStringBuffer.toString(), time,"","");


	}

	/*
	 * 发送模板消息
	 */
	public HashMap<String, String> querySimaheyiData(String code) {
		code=code.trim();//去掉空格
		if (!codeCheck(code)) {

			return WXUntil.createDefDataMap("查询四码合一数据", "码格式不正确\n"+WXServletConfig.TIP_STR[4], "","","");
		}
		log.info("查询" + code + "四码合一数据");
		if (code==null||code.isEmpty()) {
			return WXUntil.createDefDataMap("查询四码合一数据", "格式错误\n"+WXServletConfig.TIP_STR[4], "","","");
		}
		String time = "";



		// 若报错没有返回结果集，需要再存储过程begin后添加:SET NOCOUNT ON

		HashMap<Integer, String> parammeters=new HashMap<>();
		parammeters.put(1,code);
		List<Map<String, String>> maps=DataBaseOperaUntil.databaseQueryProcedure("{call dbo.QuerySimaheyiData(?)}", ConnectSqlserPool127.getInstance(),parammeters);

		StringBuffer msStringBuffer = new StringBuffer();
		String title = "";
		for (Map<String, String> map : maps) {
			msStringBuffer.append("\t条码    ：" + map.get("WorkUser_BarCode") + "\n");
			msStringBuffer.append("\t型号    ：" + map.get("PROD_CODE") + "\n");
			for(int i=1;i<=7;i++)
			{
				if (map.get("TestName"+i)!=null) {
					if (map.get("TestName"+i).length()==2) {
						msStringBuffer.append("\t"+map.get("TestName"+i)+"    ：" + map.get("TestValue"+i) + "\n");
					}else {
						msStringBuffer.append("\t"+map.get("TestName"+i)+"：" + map.get("TestValue"+i) + "\n");
					}

				}
			}
		/*		msStringBuffer.append("\t条码    ：" + map.get("Barcode") + "\n");
				msStringBuffer.append("\t二维码：" + map.get("QR_code") + "\n");
				msStringBuffer.append("\tLogo   ：" + map.get("Logo") + "\n");
				msStringBuffer.append("\t旋钮左：" + map.get("Knob_left") + "\n");
				msStringBuffer.append("\t旋钮右：" + map.get("Knob_right") + "\n");
				msStringBuffer.append("\t面板    ：" + map.get("Control_panel") + "\n");
				msStringBuffer.append("\t手把    ：" + map.get("ECstick") + "\n");
			//	msStringBuffer.append("\t结果    ：" + map.get("Result") + "\n");
*/				title="四码合一检测结果："+(map.get("TestResult")==null?"无":(map.get("TestResult").equals("1")?"OK":map.get("TestResult")));
			time = "检测时间："+map.get("TestTime");

		}
		if(msStringBuffer.length()==0){
			msStringBuffer.append("\n无四码合一检测数据\n");
		}
		time = "最近一次测试的数据\n" + time;
		//log.info(msStringBuffer.toString());
		return WXUntil.createDefDataMap(title, msStringBuffer.toString(), time,"","");

	}
	/*
	 * 查询停机时
	 */
	public String getStopTimeInfo(String queryInfo) {
		queryInfo = queryInfo.replace("～", "~");
		StringTokenizer stringTokenizer = new StringTokenizer(queryInfo, ",");
		String realName = null;
		String date = null;

		stringTokenizer.nextToken();
		realName = stringTokenizer.nextToken();
		date = stringTokenizer.nextToken();


		// 格式错误
		if (realName == null || date == null) {
			return "格式错误\n"+WXServletConfig.TIP_STR[5];
		}
		log.info("查询" + realName + date + "停机时");
		String dateBegin = "";
		String dateEnd = "";
		;

		if (date.equals("当日")) {
			String[] currDay = DateUtil.getCurrDay();
			dateBegin = currDay[0];
			dateEnd = currDay[1];
		} else if (date.equals("当周")) {
			dateBegin = DateUtil.getCurrWeekBegin();
			dateEnd = DateUtil.getCurrDateAndTimeMil();
		} else if (date.equals("当月")) {
			dateBegin = DateUtil.getCurrMonthBegin();
			dateEnd = DateUtil.getCurrDateAndTimeMil();
		} else {
			int pos = date.indexOf("~");
			try {
				dateBegin = DateUtil.getYear() + "-" + date.substring(0, pos) + " 00:00:00.000";
				dateEnd = DateUtil.getYear() + "-" + date.substring(pos + 1) + " 00:00:00.000";
				log.info(dateBegin);
				log.info(dateEnd);
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");// 用大写的YYYY会导致转换问题有问题
				simpleDateFormat.parse(dateBegin);
				dateEnd = DateUtil.getdDayAdd1(simpleDateFormat.parse(dateEnd));
			} catch (IndexOutOfBoundsException | ParseException e) {
				// TODO: handle exception

				return "日期格式不正确\n"+WXServletConfig.TIP_STR[5];
			}
		}


		String sql = "select * from ReportForStopTime,users where ReportForStopTime.Update_Name=users.username and "
				+ "OfflineTimeInfo >=" + "'" + dateBegin + "' and OfflineTimeInfo <'" + dateEnd + "' and SolveMan='"
				+ realName + "' and TimeForStop BETWEEN 300 AND 25200 AND DelMark<>1 order by OfflineTimeInfo desc";
		log.info("查询停机时 sql:" + sql);

		String content = "";
		int i = 1;
		double totalStopTime = 0;
		double line1Time = 0;
		double line2Time = 0;
		double line3Time = 0;
		double line5Time = 0;
		int line1 = 0;
		int line2 = 0;
		int line3 = 0;
		int line5 = 0;
		List<Map<String, String>> maps =DataBaseOperaUntil.databaseQuery(sql,ConnectSqlserPool127.getInstance());
		for (Map<String, String> map : maps) {


			double stopTime = Math.round(Double.parseDouble(map.get("TimeForStop")) / 60);
			String line =map.get("FLineNo").substring(6);
			if (line.equals("一线")) {
				line1++;
				line1Time += stopTime;
			} else if (line.equals("二线")) {
				line2++;
				line2Time += stopTime;
			} else if (line.equals("三线")) {
				line3++;
				line3Time += stopTime;
			} else if (line.equals("五线")) {
				line5++;
				line5Time += stopTime;
			}
			totalStopTime = stopTime + totalStopTime;
			content = content + "\n" + i + "、" + map.get("OfflineTimeInfo").substring(0, 16) + "\n\t\t"
					+ map.get("StopReason") + "停线" + stopTime + "分钟" + "\n\t\t" + line
					+ map.get("realname").trim() + "统计";
			i++;
		}
		//###########################cosmo
		sql = "select * from base_stop_statistics where last_update_date >='"
				+ dateBegin
				+ "' and last_update_date <'"
				+ dateEnd
				+ "' and greater_config=1 and Person_name='"+realName+"' order by last_update_date desc";
		maps =DataBaseOperaUntil.databaseQuery(sql,ConnectSqlserPool127.getInstance());
		for (Map<String, String> map : maps) {


			double stopTime = Double.parseDouble(map.get("downtime"));
			String line =map.get("line_code");
			if (line.equals("PH")) {
				line="一线";
				line1++;
				line1Time += stopTime;
			} else if (line.equals("PJ")) {
				line="二线";
				line2++;
				line2Time += stopTime;
			} else if (line.equals("PK")) {
				line="三线";
				line3++;
				line3Time += stopTime;
			} else if (line.equals("TN")) {
				line="五线";
				line5++;
				line5Time += stopTime;
			}
			totalStopTime = stopTime + totalStopTime;
			content = content + "\n" + i + "、" + map.get("before_scan_time").substring(0, 16) + "\n\t\t"
					+ map.get("Reason_info") + "停线" + stopTime + "分钟" + "\n\t\t" + line
					+ map.get("last_update_by").trim() + "统计";
			i++;
		}
		if (content == null || content.isEmpty()) {
			return date + "没有停机时";
		}
		content = content + "\n\n其中：" + "\n\t\t一线" + line1 + "条，共计" + line1Time + "分钟" + "\n\t\t二线" + line2 + "条，共计"
				+ line2Time + "分钟" + "\n\t\t三线" + line3 + "条，共计" + line3Time + "分钟" + "\n\t\t五线" + line5 + "条，共计"
				+ line5Time + "分钟" + "\n总计停线：" + totalStopTime + "分钟";
		// log.info("获取停机时信息的结果：" + content);
		return content;

	}
	private boolean codeCheck(String code){

		return code.matches("^[A-Za-z0-9]+");


	}

}
