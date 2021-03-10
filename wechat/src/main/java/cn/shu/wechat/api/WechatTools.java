package cn.shu.wechat.api;


import cn.shu.wechat.core.Core;
import cn.shu.wechat.utils.MyHttpClient;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import cn.shu.wechat.enums.StorageLoginInfoEnum;
import cn.shu.wechat.enums.URLEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.*;

/**
 * 微信小工具，如获好友列表等
 * 
 * @author SXS
 * @date 创建时间：2017年5月4日 下午10:49:16
 * @version 1.1
 *
 */
@Log4j2
public class WechatTools {

	/**
	 * <p>
	 * 通过RealName获取本次UserName
	 * </p>
	 * <p>
	 * 如NickName为"yaphone"，则获取UserName=
	 * "@1212d3356aea8285e5bbe7b91229936bc183780a8ffa469f2d638bf0d2e4fc63"，
	 * 可通过UserName发送消息
	 * </p>
	 * 
	 * @author SXS
	 * @date 2017年5月4日 下午10:56:31
	 * @param nickName
	 * @return
	 */
	public static String getUserNameByNickName(String nickName) {
		for (Map.Entry<String, JSONObject> stringJSONObjectEntry : Core.getContactMap().entrySet()) {
			if (stringJSONObjectEntry.getValue().getString("NickName").equals(nickName)) {
				return stringJSONObjectEntry.getValue().getString("UserName");
			}
		}
		return null;
	}

	/**
	 * 返回好友昵称列表
	 * 
	 * @author SXS
	 * @date 2017年5月4日 下午11:37:20
	 * @return
	 */
	public static List<String> getContactNickNameList() {
		List<String> contactNickNameList = new ArrayList<String>();
		for (Map.Entry<String, JSONObject> stringJSONObjectEntry : Core.getContactMap().entrySet()) {
			contactNickNameList.add(stringJSONObjectEntry.getValue().getString("NickName"));
		}

		return contactNickNameList;
	}

	/**
	 * 返回好友完整信息列表
	 * 
	 * @date 2017年6月26日 下午9:45:39
	 * @return
	 */
	public static Map<String,JSONObject> getContactList() {
		return Core.getContactMap();
	}

	/**
	 * 返回群列表
	 * 
	 * @author SXS
	 * @date 2017年5月5日 下午9:55:21
	 * @return
	 */
	public static Map<String,JSONObject> getGroupList() {
		return Core.getGroupMap();
	}

	/**
	 * 获取群ID列表
	 * 
	 * @date 2017年6月21日 下午11:42:56
	 * @return
	 */
	public static Set<String> getGroupIdList() {
		return Core.getGroupIdSet();
	}

	/**
	 * 获取群NickName列表
	 * 
	 * @date 2017年6月21日 下午11:43:38
	 * @return
	 */
	public static Set<String> getGroupNickNameList() {
		return Core.getGroupNickNameSet();
	}

	/**
	 * 根据groupIdList返回群成员列表
	 * 
	 * @date 2017年6月13日 下午11:12:31
	 * @param groupId
	 * @return
	 */
	public static JSONArray getMemberListByGroupId(String groupId) {
		return Core.getGroupMemberMap().get(groupId);
	}

	/**
	 * 退出微信
	 * 
	 * @author SXS
	 * @date 2017年5月18日 下午11:56:54
	 */
	public static void logout() {
		webWxLogout();
	}

	private static boolean webWxLogout() {
		String url = String.format(URLEnum.WEB_WX_LOGOUT.getUrl(),
				Core.getLoginInfoMap().get(StorageLoginInfoEnum.url.getKey()));
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("redirect", "1"));
		params.add(new BasicNameValuePair("type", "1"));
		params.add(
				new BasicNameValuePair("skey", (String) Core.getLoginInfoMap().get(StorageLoginInfoEnum.skey.getKey())));
		try {
			HttpEntity entity = MyHttpClient.doGet(url, params, false, null);
			String text = EntityUtils.toString(entity, Consts.UTF_8); // 无消息
			return true;
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
		return false;
	}

	public static void setUserInfo() {
		for (Map.Entry<String, JSONObject> stringJSONObjectEntry : Core.getContactMap().entrySet()) {
			Core.getUserInfoMap().put(stringJSONObjectEntry.getValue().getString("NickName"), stringJSONObjectEntry.getValue());
			Core.getUserInfoMap().put(stringJSONObjectEntry.getValue().getString("UserName"), stringJSONObjectEntry.getValue());
		}

	}

	/**
	 * 
	 * 根据用户昵称设置备注名称
	 * 
	 * @date 2017年5月27日 上午12:21:40
	 * @param nickName
	 * @param remName
	 */
	public static void remarkNameByNickName(String nickName, String remName) {
		String url = String.format(URLEnum.WEB_WX_REMARKNAME.getUrl(), Core.getLoginInfoMap().get("url"),
				Core.getLoginInfoMap().get(StorageLoginInfoEnum.pass_ticket.getKey()));
		Map<String, Object> msgMap = new HashMap<String, Object>();
		Map<String, Object> msgMap_BaseRequest = new HashMap<String, Object>();
		msgMap.put("CmdId", 2);
		msgMap.put("RemarkName", remName);
		msgMap.put("UserName", Core.getUserInfoMap().get(nickName).get("UserName"));
		msgMap_BaseRequest.put("Uin", Core.getLoginInfoMap().get(StorageLoginInfoEnum.wxuin.getKey()));
		msgMap_BaseRequest.put("Sid", Core.getLoginInfoMap().get(StorageLoginInfoEnum.wxsid.getKey()));
		msgMap_BaseRequest.put("Skey", Core.getLoginInfoMap().get(StorageLoginInfoEnum.skey.getKey()));
		msgMap_BaseRequest.put("DeviceID", Core.getLoginInfoMap().get(StorageLoginInfoEnum.deviceid.getKey()));
		msgMap.put("BaseRequest", msgMap_BaseRequest);
		try {
			String paramStr = JSON.toJSONString(msgMap);
			HttpEntity entity =  MyHttpClient.doPost(url, paramStr);
			// String result = EntityUtils.toString(entity, Consts.UTF_8);
			log.info("修改备注" + remName);
		} catch (Exception e) {
			log.error("remarkNameByUserName", e);
		}
	}

	/**
	 * 获取微信在线状态
	 * 
	 * @date 2017年6月16日 上午12:47:46
	 * @return
	 */
	public static boolean getWechatStatus() {
		return Core.isAlive();
	}

	/**
	 * 根据用户名获取用户信息
	 *
	 * @param userName 用户名@
	 * @return 用户信息
	 */
	public static JSONObject getUserByUserName(String userName) {
		Map<String, JSONObject> contactMap = Core.getContactMap();
		return contactMap.get(userName);
	}

	/**
	 * 根据用户名获取用户显示名称
	 *
	 * @param userName 用户名@
	 * @return 备注
	 */
	public static String getUserDisplayNameByUserName(String userName) {
		String remarkNameByPersonUserName = getUserRemarkNameByUserName(userName);
		if (StringUtils.isNotEmpty(remarkNameByPersonUserName)){
			return remarkNameByPersonUserName;
		}
		String nickNameByPersonUserName = getUserNickNameByUserName(userName);
		if (nickNameByPersonUserName != null){
			return nickNameByPersonUserName;
		}
		return userName;
	}

	/**
	 * 根据用户名获取用户备注
	 *
	 * @param userName 用户名@
	 * @return 备注
	 */
	public static String getUserRemarkNameByUserName(String userName) {
		JSONObject contactByUserName = getUserByUserName(userName);
		if(contactByUserName == null){
			return null;
		}
		return contactByUserName.getString("RemarkName");
	}

	/**
	 * 根据用户名获取用户备注
	 *
	 * @param userName 用户名@
	 * @return 备注
	 */
	public static String getRemarkNameByUserName(String userName) {
		if (userName.startsWith("@@")){
			return getGroupDisplayNameByUserName(userName);
		}else{
			return getUserRemarkNameByUserName(userName);
		} }

	/**
	 * 根据用户名获取用户昵称
	 *
	 * @param userName 用户名@
	 * @return 备注
	 */
	public static String getNickNameByUserName(String userName) {
		if (userName.startsWith("@@")){
			return getGroupDisplayNameByUserName(userName);
		}else{
			return getUserNickNameByUserName(userName);
		}
	}

	/**
	 * 根据用户名获取用户昵称
	 *
	 * @param userName 用户名@
	 * @return 备注
	 */
	public static String getUserNickNameByUserName(String userName) {
		JSONObject contactByUserName = getUserByUserName(userName);
		if(contactByUserName == null){
			return null;
		}
		return contactByUserName.getString("NickName");
	}

	/**
	 * 根据群名获取群备注
	 *
	 * @param userName 群名@
	 * @return 群备注
	 */
	public static String getGroupDisplayNameByUserName(String userName) {
		JSONObject jsonObject1 = Core.getGroupMap().get(userName);
		if (jsonObject1 == null){
			return null;
		}
		if(jsonObject1.getString("NickName") == null){
			return userName;
		}
		return jsonObject1.getString("NickName");

	}

	/**
	 * 根据用户名获取备注名
	 * 判断是群还是用户
	 * @param userName
	 * @return
	 */
	public static String getDisplayNameByUserName(String userName) {
		if (userName.startsWith("@@")){
			//群
			return getGroupDisplayNameByUserName(userName);
		}else{
			//个人
			return getUserDisplayNameByUserName(userName);
		}

	}
	/**
	 * 获取群成员
	 *
	 * @param groupName 群名@
	 * @param userName  成员名@
	 * @return 成员
	 */
	public static JSONObject getMemberOfGroup(String groupName, String userName) {
		Map<String, JSONArray> groupMemeberMap = Core.getGroupMemberMap();
		JSONArray members = groupMemeberMap.get(groupName);
		if (members == null || userName ==null) {
			return null;
		}
		try {
			for (Object member : members) {
				JSONObject memberJson = (JSONObject) member;
				if (userName.equals(memberJson.getString("UserName"))) {
					return memberJson;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.warn(e.getMessage());
		}
		return null;
	}
	/**
	 * 获取群成员昵称
	 *
	 * @param groupName 群名@
	 * @param userName  成员名@
	 * @return 成员昵称
	 */
	public static String getMemberNickNameOfGroup(String groupName, String userName) {
		JSONObject groupUserOfGroup = getMemberOfGroup(groupName, userName);
		return groupUserOfGroup != null
				? groupUserOfGroup.getString("NickName")
				: null;


	}

	/**
	 * 获取群成员显示名称
	 *
	 * @param groupName 群名@
	 * @param userName  成员名@
	 * @return 群成员显示名称
	 */
	public static String getMemberDisplayNameOfGroup(String groupName, String userName) {
		JSONObject groupUserOfGroup = getMemberOfGroup(groupName, userName);
		if (groupUserOfGroup == null){
			return "";
		}
		String displayName = groupUserOfGroup.getString("DisplayName");
		if (!StringUtils.isEmpty(displayName)){
			return displayName;
		}
		displayName = groupUserOfGroup.getString("NickName");
		if (!StringUtils.isEmpty(displayName)){
			return displayName;
		}
		return userName;
	}

}
