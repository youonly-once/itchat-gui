package cn.shu.wechat.server;

import java.util.Map;

import database.sqlserver.DataBaseOperaUntil;
import lombok.extern.log4j.Log4j2;
import weixin.utils.WXUntil;

/**
 * @author ShuXinSheng
 * @version 创建时间：2020年3月22日 上午11:18:43
 * 类说明
 */
@Log4j2
public class WXUserInfoProcess {

	static void saveUserInfo(Map<String, String> map){
		//保存用户信息
		if(map!=null &&map.containsKey("FromUserName")){
			new Thread(new Runnable(){

				@Override
				public void run() {

					// TODO Auto-generated method stub
					try {
						Map<String, Object> userInfo=WXUntil.getUserInfoSingle(map.get("FromUserName").toString(), WXUntil.getLocalServerAccessToken());
						//System.out.println(map);
						userInfo.put("realname", userInfo.get("remark"));
						if("http://cqgt.hongzhicn.com:8895/".equals(map.get("EventKey"))){
							userInfo.put("EventKey", "view_home");
						}else if("http://cqgt.hongzhicn.com:8895/DayProductReport.aspx".equals(map.get("EventKey"))){
							userInfo.put("EventKey", "view_day");
						}else if("http://cqgt.hongzhicn.com:8895/DowntimeQueryPieChart.html".equals(map.get("EventKey"))){
							userInfo.put("EventKey", "view_stop");
						}else if("http://cqgt.hongzhicn.com:8895/HourProductReport.aspx".equals(map.get("EventKey"))){
							userInfo.put("EventKey", "view_hour");
						}else if("http://cqgt.hongzhicn.com:8895/ProjectBadDetailReport.aspx".equals(map.get("EventKey"))){
							userInfo.put("EventKey", "view_engineer");
						}else if("https://mp.weixin.qq.com/mp/homepage?__biz=MzU3MDY5MTEwNw==&hid=1&sn=3a13fa54cb71ca661e0e7d7cb085eea7&scene=18".equals(map.get("EventKey"))){
							userInfo.put("EventKey", "view_scanhelp");
						}else if("https://mp.weixin.qq.com/mp/homepage?__biz=MzU3MDY5MTEwNw==&hid=2&sn=cd2e7b3d4c1d1a6edc50906e3fff7dd5&scene=18".equals(map.get("EventKey"))){
							userInfo.put("EventKey", "view_officehelp");
						}else if("pic_query".equals(map.get("EventKey"))){
							userInfo.put("EventKey", "pic_query");
						}else if("scancode_query".equals(map.get("EventKey"))){
							userInfo.put("EventKey", "scancode_query");
						}else if("scancode_queryanjianyi".equals(map.get("EventKey"))){
							userInfo.put("EventKey", "scancode_queryanjianyi");
						}else if("scancode_querysimaheyi".equals(map.get("EventKey"))){
							userInfo.put("EventKey", "scancode_querysimaheyi");
						}else if("help".equals(map.get("EventKey"))){
							userInfo.put("EventKey", "help");
						}else if("helihua".equals(map.get("EventKey"))){
							userInfo.put("EventKey", "helihua");
						}else {
							userInfo.put("EventKey", "other");
						}
						DataBaseOperaUntil.saveUserInfo(userInfo);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}).start();
		}
	}
}
