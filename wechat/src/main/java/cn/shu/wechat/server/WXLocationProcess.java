package cn.shu.wechat.server;

import java.io.IOException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import utils.HttpUtil;
import utils.JSONUtils;
import weixin.exception.WXException;

/**
 * @author ShuXinSheng
 * @version 创建时间：2020年3月22日 上午11:15:58 类说明
 */
@Log4j2
public class WXLocationProcess {
	/*
	 * 获取用户地理位置
	 */
	static void transLocation(String longitude, String latitude, String userOpenid) {

		double len = getDistance(29.631392, 106.649002, Double.parseDouble(latitude), Double.parseDouble(longitude));
		try {
			String url = "http://api.map.baidu.com/ag/coord/convert?from=0&to=4&x=" + longitude + "&y=" + latitude;
			String reString = HttpUtil.sendGet(url, "");
			// log.info(reString);
			String x = (String) JSONUtils.parseJSON2Map(reString).get("x");
			String y = (String) JSONUtils.parseJSON2Map(reString).get("y");
			// url =
			// "http://api.map.baidu.com/geocoder/v2/?ak=vnW6bjiUPfZI0I0gx1Gnee7cLl37heO4&location="
			// + x + "," + y + "&output=json&pois=1";
			url = "https://api.map.baidu.com/geocoder/v2/?location=" + latitude + "," + longitude
					+ "&output=json&pois=1&ak=vnW6bjiUPfZI0I0gx1Gnee7cLl37heO4";
			reString = HttpUtil.sendPost(url, "");

			// log.info(reString);

			JSONObject resultObject = JSON.parseObject(reString).getJSONObject("result");
			String formatted_address = resultObject.getString("formatted_address");
			JSONArray poisArray = resultObject.getJSONArray("pois");
			String content = "\n地址：" + formatted_address + "\n附近：";
			JSONObject pois = null;
			for (int i = 0, n = poisArray.size(); i < n; i++) {
				pois = poisArray.getJSONObject(i);
				content = content + "\n" + (i + 1) + "、"
						+ /* pois.getString("addr")+"\n\t\t"+ */pois.getString("name");
			}
			SendTemplateMsg sendTemplateMsg = new SendTemplateMsg();

			sendTemplateMsg.sendTemplateMsgLocation("测试", content, len, userOpenid);
		} catch (JSONException | NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 计算两个经纬度之间的距离
	 *
	 * @author
	 *
	 */

	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}

	/**
	 *
	 * @param lat1
	 *            第一个纬度 29.631392 La
	 * @param lng1 第一个经度106.649002
	 * @param lat2 第二个纬度29.631399
	 * @param lng2 第二个经度106.648941
	 * @return 两个经纬度的距离 单位KM
	 *
	 */

	private static double getDistance(double lat1, double lng1, double lat2, double lng2) {
		final double EARTH_RADIUS = 6378.137;// 地球半径,单位千米
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);

		double s = 2 * Math.asin(Math.sqrt(
				Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS * 1000;
		log.info(String.valueOf(s));
		// s = Math.round(s * 10000) / 10000;
		// log.info(String.valueOf(s));
		return s;

	}
}
