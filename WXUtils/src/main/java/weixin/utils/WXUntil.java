package weixin.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import database.sqlserver.ConnectDataBaseInter;
import database.sqlserver.ConnectSqlserPool127;
import database.sqlserver.DataBaseOperaUntil;
import lombok.extern.log4j.Log4j2;
import utils.DateUtil;
import utils.HttpUtil;
import utils.JSONUtils;
import utils.Log;
import utils.SendSms;
import weixin.exception.WXException;
import weixin.exception.WXTokenExpireException;
import weixin.infor.bean.JsonRootBean;
import weixin.infor.bean.User_list;
import weixin.templatemsg.bean.TemplateData;
import weixin.templatemsg.bean.WechatTemplate;
@Log4j2
public class WXUntil {
	private final static ConnectDataBaseInter tokenSaveAddr = ConnectSqlserPool127.getInstance();
	private static final Hashtable<String, String> TOKEN = new Hashtable<>(1);
	private static final String token = "TOKEN";
	private static final String update = "UPDATE";

	static {
		log.info("WXUntil 静态代码块执行");
		// TOKEN.put(token, "");
		// TOKEN.put(update, "");
	}

	private WXUntil() {
	}

	/**
	 * if (tokenUpdateTime!=null) { long
	 * diff=GetDate.getInstance().getDateDiff(tokenUpdateTime,GetDate.
	 * getInstance().getCurrDateAndTimeMil()); if (diff!=-1&&diff>=7000000 )
	 * {//7200000国期 200秒误差 return getWeixinServerAccessToken();
	 *
	 * } }
	 *
	 * @throws WXException
	 * @throws NullPointerException
	 * @throws JSONException
	 * @throws IOException
	 */
	synchronized public static String getLocalServerAccessToken()
			throws JSONException, NullPointerException, WXException, IOException {
		log.info("开始获取TOKEN");
		String accessToken = null;
		if (TOKEN.get(token) == null || TOKEN.get(update) == null || tokenIsExpire(TOKEN.get(update))) {// 数据库没有记录
			log.info("内存TOKEN为空或过期");
			String sql = "select * from weixin_accesstoken WHERE id=0";
			List<Map<String, String>> maps = DataBaseOperaUntil.databaseQuery(sql, tokenSaveAddr);
			if (maps == null || maps.isEmpty()) {
				log.info("数据库TOKEN为空");
			} else {
				String tokenUpdatedate = maps.get(0).get("updatedate");
				accessToken = maps.get(0).get("access_token");
				if (accessToken == null || tokenUpdatedate == null) {
					log.info("数据库TOKEN为空");
				} else if (tokenIsExpire(tokenUpdatedate)) {
					log.info("数据库TOKEN过期");
				} else {
					log.info("返回数据库TOKEN");
					TOKEN.put(token, accessToken);
					TOKEN.put(update, tokenUpdatedate);
					return TOKEN.get(token);
				}
			}
			accessToken = getWeixinServerAccessToken();
			setLocalServerAccessToken(accessToken);

		} else {
			log.info("返回内存TOKEN");
			return TOKEN.get(token);
		}
		log.info("获取本地token");

		// TODO Auto-generated method stub
		accessToken = TOKEN.get(token);
		if (accessToken == null) {
			throw new WXException("获取TOKEN失败");
		}
		return accessToken;
	}

	/**
	 * 保存获取的token
	 *
	 * @param accessToken
	 */
	private static void setLocalServerAccessToken(String accessToken) {
		String tokenUpdatedate = DateUtil.getCurrDateAndTimeMil();
		TOKEN.put(token, accessToken);
		TOKEN.put(update, tokenUpdatedate);
		String sql = "update  weixin_accesstoken set access_token='" + accessToken + "',updatedate='" + tokenUpdatedate
				+ "' where id='0'";
		boolean result = false;
		log.info("保存token:" + accessToken);
		result = DataBaseOperaUntil.databaseUpdate(sql, tokenSaveAddr);
		if (!result) {
			log.info("保存token 失败:" + sql);
		}

	}

	private static boolean tokenIsExpire(String tokenUpdatedate) {

		try {
			long diff = DateUtil.getDateDiff(tokenUpdatedate, DateUtil.getCurrDateAndTimeMil());
			if (diff != -1 && diff >= 7200000) {// 7200000国期 200秒误差
				return true;
			} else {
				return false;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return true;

		}

	}

	public static String getLocalServerjsticket(String access_token) throws JSONException, NullPointerException,
			WXException, IOException, ParseException, NumberFormatException, WXTokenExpireException {

		String jsticket = null;
		String sql = "select * from weixin_accesstoken where id=1";
		List<Map<String, String>> maps = DataBaseOperaUntil.databaseQuery(sql, tokenSaveAddr);
		if (maps!=null && !maps.isEmpty()) {

			String jsticketUpdateTime = maps.get(0).get("updatedate");
			jsticket = maps.get(0).get("access_token");
			if (jsticket != null && !jsticket.isEmpty() && jsticketUpdateTime!=null) {
				long diff;
				diff = DateUtil.getDateDiff(jsticketUpdateTime, DateUtil.getCurrDateAndTimeMil());
				if (diff < 7200000) {// 7200000国期 200秒误差
					log.info("获取数据库的js ticket：" + jsticket);
					return jsticket;
				}

			}

		}
		// 重新获取Token并保存到数据库
		jsticket = getWXSerjsapiticket(access_token);
		log.info("获取Server的js ticket：" + jsticket);
		return jsticket;

	}

	/**
	 * 获取WEIXIN ACCESS_TOKEN
	 *
	 * @return
	 * @throws WXException
	 * @throws NullPointerException
	 * @throws JSONException
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	private static String getWeixinServerAccessToken() throws IOException, NumberFormatException, WXException {// 从微信重新获取

		// 调用接口，获取微信返回的JSON
		String resultJson = HttpUtil.sendPost(WXConfig.Url.getToken, "");
		log.info("获取token 返回 json：" + resultJson);
		/**
		 * {"access_token":"access_token","expires_in":7200}
		 */
		// 处理JSON 提取token
		Map<String, Object> accessTokenMap = JSONUtils.parseJSON2Map(resultJson);

		if (accessTokenMap.get("access_token") != null) {// 微信返回错误
			String access_token = accessTokenMap.get("access_token").toString();
			setLocalServerAccessToken(access_token);
			return access_token;

		} else {
			String code = accessTokenMap.get("errcode").toString();
			throw new WXException(Integer.valueOf(code));
		}
	}

	private static String getWXSerjsapiticket(String access_token)
			throws IOException, NumberFormatException, WXException, WXTokenExpireException {

		// 通过郑州服务器获取、IP固定
		// 郑州服务器获取成功后直接保存到数据库，再重新获取数据库
		/*
		 * String getUrl="http://218.28.1.150:8893/getWXToken?type=jsticket";
		 * if(HttpUtil.sendPost(getUrl, "").equals("OK")){ return
		 * getLocalServerAccessToken(false); }
		 */

		String ticket_url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" + access_token
				+ "&type=jsapi";
		String result = HttpUtil.sendGet(ticket_url, "");
		log.info("get jsticket result:" + result);
		Map<String, Object> map = JSONUtils.parseJSON2Map(result);
		String errcode = map.get("errcode").toString();
		// token expire
		if (errcode.equals("0")) {
			String jsticket = map.get("ticket").toString();
			setLocalServerjsapiticket(jsticket);
			return jsticket;
		} else if (tokenExpire(errcode)) {
			throw new WXTokenExpireException();

		} else {
			throw new WXException(errcode);
		}

	}

	/**
	 * 保存jsticket到服务器
	 */
	private static void setLocalServerjsapiticket(String ticket) {
		log.info("保存js ticket:" + ticket);
		String sql = "update  weixin_accesstoken set access_token='" + ticket + "',updatedate='"
				+ DateUtil.getCurrDateAndTimeMil() + "' where id='1'";
		if (!DataBaseOperaUntil.databaseUpdate(sql, tokenSaveAddr)) {
			log.info("保存js ticket到数据库失败：");
		}
	}

	/**
	 * 获取分组ID
	 *
	 * @groupName 分组名 access_token token
	 */
	public static String getGroupId(String groupName, String access_token) throws WXException, IOException {
		// 获得所有分组信息
		String getGroupIDUrl = "https://api.weixin.qq.com/cgi-bin/tags/get?access_token=" + access_token;
		// 调用接口获得分组信息
		String resultForGroupId = HttpUtil.sendPost(getGroupIDUrl, "");

		// 处理JSON 提取token
		Map<String, Object> groupIDMap = JSONUtils.parseJSON2Map(resultForGroupId);
		if (!groupIDMap.containsKey("tags") && tokenExpire(groupIDMap.get("errcode").toString())) {// 可能accesstoken过期
			log.info("获取分组 <" + groupName + ">ID时token过期,正在重新获取...");
			return getGroupId(groupName, getLocalServerAccessToken());
		}
		JSONArray jsona = JSONArray.parseArray(groupIDMap.get("tags").toString());
		String id = "0";
		for (int i = 0; i < jsona.size(); i++) {
			Map<String, Object> group = JSONUtils.parseJSON2Map(jsona.getString(i));
			String name = group.get("name").toString();
			if (name.equals(groupName)) {
				id = group.get("id").toString();
				break;
			}
		}
		log.info("获取分组<" + groupName + ">ID==" + id);
		return id;

	}

	/*
	 * 获取分组下所有用户列表
	 */
	public static List<Object> getGroupUserOpenId(String groupId, String access_token) throws WXException, IOException {
		String getGroupUserIDUrl = "https://api.weixin.qq.com/cgi-bin/user/tag/get?access_token=" + access_token;
		String JsonStr = "{   \"tagid\" : " + groupId + ",   \"next_openid\":\"\")";
		String resultForUserId = HttpUtil.sendPost(getGroupUserIDUrl, JsonStr);

		Map<String, Object> groupUserIDMap = JSONUtils.parseJSON2Map(resultForUserId);
		// 分组没人
		if (groupUserIDMap.containsKey("count") && groupUserIDMap.get("count").toString().equals("0")) {
			log.info("获取<" + groupId + ">用户列表为空");
			return null;
		}
		if (!groupUserIDMap.containsKey("data") && groupUserIDMap.containsKey("errcode")
				&& tokenExpire(groupUserIDMap.get("errcode").toString())) {
			log.info("获取<" + groupId + ">用户列表时token过期，正在重新获取...");
			return getGroupUserOpenId(groupId, getLocalServerAccessToken());

		}
		@SuppressWarnings("unchecked")
		Map<String, Object> map2 = (Map<String, Object>) groupUserIDMap.get("data");//
		List<Object> useridArray = JSONArray.parseArray(map2.get("openid").toString(),Object.class);
		log.info("组 <" + groupId + ">用户列表：" + useridArray);
		return useridArray;

	}

	// 获取微信服务器中生成的媒体文件

	// 由于视频使用的是http协议，而图片、语音使用http协议，故此处需要传递media_id和type
	/**
	 *
	 * @param media_id
	 * @param type
	 * @param filepath
	 * @param access_token
	 * @return
	 * @throws IOException
	 */
	public static String getTmpFile(String media_id, String type, String filepath, String access_token)
			throws IOException {

		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {

			String url = null;
			// 视频是http协议
			if ("video".equalsIgnoreCase(type)) {
				url = String.format(WXConfig.Url.GET_TMP_MATERIAL_VIDEO, access_token, media_id);
			} else {
				url = String.format(WXConfig.Url.GET_TMP_MATERIAL, access_token, media_id);

			}
			URL u = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setRequestMethod("POST");
			conn.connect();
			bis = new BufferedInputStream(conn.getInputStream());
			String content_disposition = conn.getHeaderField("content-disposition");
			// 微信服务器生成的文件名称
			String file_name = "";
			String[] content_arr = content_disposition.split(";");
			if (content_arr.length == 2) {
				String tmp = content_arr[1];
				int index = tmp.indexOf("\"");
				file_name = tmp.substring(index + 1, tmp.length() - 1);
			}

			filepath = filepath + file_name;
			log.info("文件保存路径：" + filepath);
			// 生成不同文件名称
			bos = new BufferedOutputStream(new FileOutputStream(filepath));
			byte[] buf = new byte[2048];
			int length;
			while ((length = bis.read(buf)) != -1) {
				bos.write(buf, 0, length);
				length = bis.read(buf);
			}
			return filepath;
		} finally {
			try {
				if (bos != null)
					bos.close();
				if (bis != null)
					bis.close();
			} catch (Exception e) {
				// TODO: handle exception
			}

		}

	}

	/*
	 * 获取所有用户openid
	 */
	public static List<Object> getAllUserOpenId(String next_openid, String access_token)
			throws JSONException, NullPointerException, WXException, IOException {
		String getGroupUserIDUrl = String.format(WXConfig.Url.getGroupUserIDUrl, access_token, next_openid);
		String resultForUserId = HttpUtil.sendPost(getGroupUserIDUrl, "");
		log.info("获取所有粉丝ID返回结果：" + resultForUserId);
		Map<String, Object> groupUserIDMap = JSONUtils.parseJSON2Map(resultForUserId);
		String code = groupUserIDMap.get("errcode").toString();
		// 返回错误码
		if (!"0".equals(code)) {
			// token 过期
			if (tokenExpire(code)) {
				log.info("获取所有粉丝ID时Token过期，正在重新获取");
				access_token = getLocalServerAccessToken();
				return getAllUserOpenId(next_openid, access_token);
			} else {
				throw new WXException(code);
			}
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> map2 = (Map<String, Object>) groupUserIDMap.get("data");//
		// 获得openid数组
		List<Object> useridArray = JSONArray.parseArray(map2.get("openid").toString(),Object.class);
		// 获取1000，说明可能还有没获取到的
		if (Integer.valueOf(groupUserIDMap.get("count").toString()) == 1000) {
			// 继续获取，并合并后返回
			useridArray.addAll(getAllUserOpenId(groupUserIDMap.get("next_openid").toString(), access_token));
		}
		log.info("获取所有粉丝ID Result_JSONArray ：" + useridArray);

		return useridArray;
	}

	public static boolean tokenExpire(String errorcode) {
		if (errorcode.equals("40001") || errorcode.equals("40014") || errorcode.equals("42001")) {
			log.info("token超时");
			return true;
		} else if (errorcode.equals("40164")) {// 通知我 ip地址加入白名单
			SendSms.sendSms1to1("15723468981", WXException.getMessag(Integer.valueOf(errorcode)));
		}
		return false;
	}

	/**
	 * // 获取所有分组下的用户的openid与名称对应关系
	 *
	 * @param allUserIdArrray
	 * @param tag
	 * @param access_token
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String, Object> getUserNameAndIdMap(List<Object> allUserIdArrray, String tag,
															  String access_token) throws IOException {

		HashMap<String, Object> userNameAndIdMap = new HashMap<String, Object>();

		String getUserInfoUrl = String.format(WXConfig.Url.getUserInfoUrl, access_token);
		String resultJSON = HttpUtil.sendPost(getUserInfoUrl, createGetUserInfoJson(allUserIdArrray));
		String user_info_list = null;
		List<Map<String, Object>> userInfoList = null;
		user_info_list = JSON.parseObject(resultJSON).get("user_info_list").toString();
		userInfoList = JSONUtils.toList(user_info_list);

		for (Map<String, Object> map : userInfoList) {
			// subscribe 用户是否订阅该公众号标识，值为0时，代表此用户没有关注该公众号，拉取不到其余信息。
			if (map.containsKey("subscribe") && map.get("subscribe").toString().equals("1")) {
				String remark = (String) map.get("remark");
				String openid = (String) map.get("openid");

				if (tag.equals("idvalue")) {
					if (remark.equals("")) {// 备注为空，返回昵称
						continue;
						// userNameAndIdMap.put((String) map.get("nickname"),
						// map.get("openid"));
					} else {// 多个同名的
						if (userNameAndIdMap.containsKey(remark)) {
							userNameAndIdMap.put(remark, userNameAndIdMap.get(remark) + "," + openid);
						} else {
							userNameAndIdMap.put(remark, openid);
						}
					}
				} else if (tag.equals("namevalue")) {
					if (remark.equals("")) {// 备注为空，返回昵称
						continue;
						// userNameAndIdMap.put((String) map.get("openid"),
						// map.get("nickname"));
					} else {
						userNameAndIdMap.put(openid, remark);

					}
				}

			}

		}
		log.info("获取真实姓名与openId对应关系：" + userNameAndIdMap);
		return userNameAndIdMap;
	}

	/**
	 * 获取用户信息的post JSON
	 *
	 * @param allUserIdArrray
	 * @return
	 */
	private static String createGetUserInfoJson(List<Object> allUserIdArrray) {
		JsonRootBean rootBean = new JsonRootBean();
		List<User_list> userListList = new ArrayList<User_list>();

		for (int i = 0; i < allUserIdArrray.size(); i++) {
			User_list userList = new User_list();
			userList.setLang("zh_CN");
			userList.setOpenid(allUserIdArrray.get(i).toString());
			userListList.add(userList);
		}
		rootBean.setUser_list(userListList);

		String toString = JSONUtils.toJSONString(rootBean);
		JSONArray toStringArray = JSONArray.parseArray(toString);
		toString = toStringArray.get(0).toString();
		log.info("获取用户详细信息的POST_JSON：" + toString);
		return toString;
	}

	/**
	 * 获取单个用户的信息
	 *
	 * @param openid
	 * @param access_token
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> getUserInfoSingle(String openid, String access_token) throws Exception {

		log.info("获取用户详细信息Single:" + openid);
		String url = String.format(WXConfig.Url.GET_SINGLE_USER_INFO, access_token, openid);
		String result = HttpUtil.sendPost(url, "");
		Map<String, Object> map = JSONUtils.parseJSON2Map(result);

		String code = map.get("errcode").toString();
		if ("0".equals(code)) {
			return map;
		} else if (tokenExpire(code)) {
			// access-token可能过期
			log.info("获取用户详细信息时TOKEN，正在重新获取");
			// 使用新的token并重发
			return getUserInfoSingle(openid, getLocalServerAccessToken());
		} else {
			throw new WXException(code);
		}
	}

	/**
	 *
	 * @param access_token
	 * @param type
	 * @param offest
	 * @param count
	 * @return title=ID
	 * @throws IOException
	 */
	public static HashMap<String, String> getMaterialID2Title(String access_token, String type, String offest,
															  String count) throws IOException {
		HashMap<String, String> idToTitle = new HashMap<>();
		String url = String.format(WXConfig.Url.GET_Material, access_token);
		String poststr = "{\"type\":\"" + type + "\",\"offset\":\"" + offest + "\",\"count\":\"" + count + "\"}";
		String result = HttpUtil.sendPost(url, poststr);
		log.info("素材列表：" + result);
		JSONObject jsonObject = JSON.parseObject(result);
		JSONArray itemArray = JSONArray.parseArray(jsonObject.get("item").toString());
		for (Object object : itemArray) {
			JSONObject item = JSON.parseObject(object.toString());
			JSONArray contentList = JSONArray.parseArray(JSON.parseObject(item.getString("content")).getString("news_item"));
			for (Object object2 : contentList) {
				JSONObject detailObject = JSON.parseObject(object2.toString());
				idToTitle.put(detailObject.getString("title"),
						detailObject.getString("title") + "," + detailObject.getString("thumb_url") + ","
								+ detailObject.getString("digest") + "," + detailObject.getString("url") + ","
								+ item.getString("media_id"));
			}
		}

		log.info("素材列表：" + idToTitle.toString());
		return idToTitle;
	}

	/**
	 * 发送模板消息
	 *
	 * @param templateData
	 * @param access_token
	 * @return
	 * @throws JSONException
	 * @throws NullPointerException
	 * @throws WXException
	 * @throws IOException
	 */
	public static String sendTemplateMsg(String templateData, String access_token)
			throws JSONException, NullPointerException, WXException, IOException {

		String sendTemplateMsgUrl = String.format(WXConfig.Url.sendTemplateMsgUrl, access_token);

		if (templateData == null) {
			new NullPointerException("POST JSON templateData is Null");
		}
		// 发送
		String resultJSON = HttpUtil.sendPost(sendTemplateMsgUrl, templateData);

		Map<String, Object> resultHashMap = JSONUtils.parseJSON2Map(resultJSON);

		String errcode = resultHashMap.get("errcode").toString();
		if (tokenExpire(errcode)) {
			log.info("发送模板消息时token过期");
			return sendTemplateMsg(templateData, getLocalServerAccessToken());
		}
		return errcode.toString();

	}

	/**
	 * 为用户添加分组
	 *
	 * @param groupName
	 * @param userOpenid
	 * @param status
	 * @return
	 * @throws JSONException
	 * @throws NullPointerException
	 * @throws WXException
	 * @throws IOException
	 */
	public static String setGroupTag(String groupName, String userOpenid, int status)
			throws JSONException, NullPointerException, WXException, IOException {

		// 保证最快速度回复用户，采用线程操作

		String access_token = getLocalServerAccessToken();
		String groupId = getGroupId(groupName, access_token);
		// TODO Auto-generated method stub
		String url;
		if (status == 1) {// 添加
			url = WXConfig.Url.ADD_GROUP;
		} else {// 取消
			url = WXConfig.Url.REMOVE_GROUP;
		}
		String data = "{\"openid_list\":[\"" + userOpenid + "\"],\"tagid\" : " + groupId + " }";// 粉丝列表
		if (!myDoPost(url, data, access_token)) {
			return "设置失败，请联系管理员(更新用户组状态失败)";
		} else {
			return "设置成功";
		}
	}

	/**
	 * 设置用户备注
	 */
	public static String setUserRemark(String realName, String userOpenid)
			throws JSONException, NullPointerException, WXException, IOException {

		log.info("设置用户（" + userOpenid + "）备注（" + realName + "）");
		String accessToken = getLocalServerAccessToken();
		String data = "{\"openid\":\"" + userOpenid + "\",\"remark\":\"" + realName + "\"}";
		if (!myDoPost(WXConfig.Url.SET_REMARK, data, accessToken)) {
			return "设置失败，请联系管理员(更新用户组状态失败)";
		} else {
			return "设置成功";
		}

	}

	private synchronized static boolean myDoPost(String url, String data, String access_token)
			throws WXException, IOException {
		String newUrl = String.format(url, access_token);
		log.info(Thread.currentThread().getName() + " POSTING:\n\t" + newUrl + "\n\t" + data);// 打印出来
		String resultStr = HttpUtil.sendPost(newUrl, data);

		log.info(Thread.currentThread().getName() + " POST Result str:" + resultStr);// 打印出来

		Map<String, Object> map = JSONUtils.parseJSON2Map(resultStr);
		String errcode = map.get("errcode").toString();
		if ("0".equals(errcode)) {
			return true;
		} else if (tokenExpire(errcode)) {// token过期
			// 重新POST
			access_token = getLocalServerAccessToken();
			return myDoPost(url, data, access_token);

		} else {
			throw new WXException(errcode);
		}

	}

	/**
	 * 回复文本消息
	 *
	 * @param replyContent
	 * @param userOpenid
	 * @param myOpedid
	 * @return
	 */
	public static String replyTextMsg(String replyContent, String userOpenid, String myOpedid) {

		String replyMsg = String.format(WXConfig.Url.replyMsg_XML, userOpenid, myOpedid, new Date().getTime(),
				replyContent);
		log.info("回复消息：" + replyContent);

		return replyMsg;

	}

	/**
	 * 回复图片消息
	 *
	 * @param imageId
	 * @param userOpenid
	 * @param myOpedid
	 * @return
	 */
	public static String replyImageMsg(String imageId, String userOpenid, String myOpedid) {
		String replyMsg = "<xml>" + "<ToUserName><![CDATA[" + userOpenid + "]]></ToUserName>"// 回复用户时，这里是用户的openid；但用户发送过来消息这里是微信公众号的原始id
				+ "<FromUserName><![CDATA[" + myOpedid + "]]></FromUserName>"// 这里填写微信公众号
				// 的原始id；用户发送过来时这里是用户的openid
				+ "<CreateTime>" + new Date().getTime() + "</CreateTime>"// 这里可以填创建信息的时间，目前测试随便填也可以
				+ "<MsgType><![CDATA[image]]></MsgType>"// 文本类型，text，可以不改
				+ "<Image><MediaId><![CDATA[" + imageId + "]]></MediaId></Image>"// 文本内容，我喜欢你
				// + "<MsgId>1234567890123456</MsgId> "//消息id，随便填，但位数要够
				+ " </xml>";
		log.info("回复图片消息：" + imageId);
		return replyMsg;

	}

	/**
	 * 回复文章消息
	 *
	 * @param userOpenid
	 * @param myOpedid
	 * @return
	 */
	public static String replyMaterialMsg(HashMap<String, String> material, String userOpenid, String myOpedid) {
		String replyMsg = "<xml>" + "<ToUserName><![CDATA[" + userOpenid + "]]></ToUserName>"// 回复用户时，这里是用户的openid；但用户发送过来消息这里是微信公众号的原始id
				+ "<FromUserName><![CDATA[" + myOpedid + "]]></FromUserName>"// 这里填写微信公众号
				// 的原始id；用户发送过来时这里是用户的openid
				+ "<CreateTime>" + new Date().getTime() + "</CreateTime>"// 这里可以填创建信息的时间，目前测试随便填也可以
				+ "<MsgType><![CDATA[news]]></MsgType>"// 文本类型，text，可以不改
				+ " <ArticleCount>1</ArticleCount>" + "  <Articles>" + " <item>" + "  <Title><![CDATA["
				+ material.get("title") + "]]></Title>" + "  <Description><![CDATA[" + material.get("digest")
				+ "]]></Description>" + "  <PicUrl><![CDATA[" + material.get("thumb_url") + "]]></PicUrl>"
				+ "  <Url><![CDATA[" + material.get("url") + "]]></Url>" + " </item>" + " </Articles>"
				// + "<MsgId>1234567890123456</MsgId> "//消息id，随便填，但位数要够
				+ " </xml>";
		log.info("回复图文消息：" + material);
		return replyMsg;

	}

	/**
	 * 客服图文消息
	 *
	 * @param materialId
	 * @param userOpenid
	 * @param access_token
	 * @throws IOException
	 */
	public static void sendCustomMpnewsMsg(String materialId, String userOpenid, String access_token)
			throws IOException {
		String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + access_token;
		String postStr = "{" + "\"touser\":\"" + userOpenid + "\"," + "\"msgtype\":\"mpnews\"," + " \"mpnews\":" + "{"
				+ "    \"media_id\":\"" + materialId + "\"" + "}" + "}";
		log.info(materialId);
		log.info("发送客服消息：" + HttpUtil.sendPost(url, postStr));

	}

	/**
	 * 客服文本消息
	 *
	 * @param access_token
	 * @throws IOException
	 */
	public static void sendCustomTextMsg(String text, String toUser, String access_token) throws IOException {
		String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + access_token;
		String postStr = "{" + "\"touser\":\"" + toUser + "\"," + "\"msgtype\":\"text\"," + " \"text\":" + "{"
				+ "    \"content\":\"" + text + "\"" + "}" + "}";
		log.info(text);
		log.info("发送客服消息：" + HttpUtil.sendPost(url, postStr));

	}

	/**
	 * 客服图片消息
	 * @param access_token
	 * @throws IOException
	 */
	public static void sendCustomImgtMsg(String media_id, String toUser, String access_token) throws IOException {
		String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + access_token;
		String postStr = "{" + "\"touser\":\"" + toUser + "\"," + "\"msgtype\":\"image\"," + " \"image\":" + "{"
				+ "    \"media_id\":\"" + media_id + "\"" + "}" + "}";
		log.info("发送客服图片消息：" + HttpUtil.sendPost(url, postStr));

	}

	/**
	 * 上传图片到微信服务器
	 *
	 * @param path
	 *            本地文件路径
	 * @return 微信图片ID
	 * @throws WXException
	 * @throws IOException
	 */
	public static String uploadCodeImage(String accessToken, String path) throws WXException, IOException {
		String msgId = null;
		File file = new File(path);
		String url = String.format(WXConfig.Url.UPLOAD_IMG, accessToken);
		String resultJson = HttpUtil.sendFileWeixin(url, file);
		log.info("上传图片返回：" + resultJson);

		Map<String, Object> map = utils.JSONUtils.parseJSON2Map(resultJson);
		String code = map.get("errcode").toString();
		msgId = map.get("media_id").toString();
		if ("0".equals(code)) {
			return msgId;
		} else if (tokenExpire(code)) {
			log.info("上传图片时token过期");
			return uploadCodeImage(getLocalServerAccessToken(), path);

		} else {
			throw new WXException(code);
		}
	}

	public static HashMap<String, String> createDefDataMap(String title, String content, String time, String remark,
														   String url) {
		HashMap<String, String> map = new HashMap<String, String>();
		map = new HashMap<String, String>();
		map.put("title", "【" + title + "】\n");
		map.put("title_color", "#FF0000");
		map.put("content", "\n" + content);
		map.put("content_color", "#173177");
		map.put("senddate", DateUtil.getCurrDateAndTime());
		map.put("senddate_color", "#173177");
		map.put("time", time);
		map.put("time_color", "#173177");
		map.put("remark", remark);
		map.put("remark_color", "#173177");
		map.put("url", url);
		map.put("template_id", "CUe7bolnt5vS9YwSwqkJcTsuhJxnmDo0cRo7OASN5Vw");
		return map;
	}

	// 模板消息的JSON
	public static String getTemplateData(String openid, Map<String, String> dataMap) throws WXException {
		String toString = null;
		try {

			// 封装基础数据
			WechatTemplate wechatTemplate = new WechatTemplate();
			wechatTemplate.setTemplate_id(dataMap.get("template_id"));
			wechatTemplate.setTouser(openid);
			wechatTemplate.setUrl(dataMap.get("url"));
			Map<String, TemplateData> mapdata = new HashMap<String, TemplateData>();
			// 封装模板数据
			TemplateData first = new TemplateData();
			first.setValue(dataMap.get("title"));
			first.setColor(dataMap.get("title_color"));
			mapdata.put("first", first);

			TemplateData keyword1 = new TemplateData();
			String contentString = dataMap.get("content");
			// 服务器的\n被转义为\\n了
			// 现在同样要把\转义为\\后替换 为\\ \\ n
			contentString = contentString.replaceAll("\\\\n", "\n");
			keyword1.setValue(contentString);
			keyword1.setColor(dataMap.get("content_color"));
			mapdata.put("keyword1", keyword1);

			TemplateData keyword2 = new TemplateData();
			// SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			// String format = formatter.format(new Date().getDate());
			keyword2.setValue(dataMap.get("senddate"));
			keyword2.setColor(dataMap.get("senddate_color"));
			mapdata.put("keyword2", keyword2);

			TemplateData keyword3 = new TemplateData();
			// SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			// String format = formatter.format(new Date().getDate());
			keyword3.setValue(dataMap.get("time"));
			keyword3.setColor(dataMap.get("time_color"));
			mapdata.put("keyword3", keyword3);

			TemplateData remark = new TemplateData();
			remark.setValue(dataMap.get("remark"));
			remark.setColor(dataMap.get("remark_color"));
			mapdata.put("remark", remark);

			wechatTemplate.setData(mapdata);
			toString = JSONUtils.toJSONString(wechatTemplate);
			JSONArray toStringArray = new JSONArray();
			toStringArray = JSONArray.parseArray(toString);
			toString = toStringArray.get(0).toString();

		} catch (NullPointerException e) {
			throw new WXException("创建模板数据失败");
		}
		return toString;
	}
}
