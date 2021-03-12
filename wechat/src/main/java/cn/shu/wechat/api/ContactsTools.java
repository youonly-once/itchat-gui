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
 * 微信联系人工具，如获好友昵称、备注等
 * 
 * @author SXS
 * @date 创建时间：2017年5月4日 下午10:49:16
 * @version 1.1
 *
 */
@Log4j2
public class ContactsTools {

	/**
	 * @author SXS
	 * @date 2017年5月4日 下午10:56:31
	 * @param nickName 联系人昵称
	 * @return userName {@code null} 找不到返回
	 */
	public static String getUserNameByNickName(String nickName) {
		for (Map.Entry<String, JSONObject> entry : Core.getContactMap().entrySet()) {
			if (entry.getValue().getString("NickName").equals(nickName)) {
				return entry.getValue().getString("UserName");
			}
		}
		return null;
	}





	/**
	 * 根据用户名获取用户信息
	 *
	 * @param userName 用户UserName
	 * @return 用户信息
	 */
	public static JSONObject getUserByUserName(String userName) {
		Map<String, JSONObject> contactMap = Core.getContactMap();
		return contactMap.get(userName);
	}

	/**
	 * 根据用户名获取用户显示名称
	 *
	 * @param userName 用户UserName
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
	 * @param userName 用户UserName
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
	 * @param userName 用户UserName
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
	 * 包括群、普通联系人
	 *
	 * @param userName 用户UserName
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
	 * 根据用户名获取普通用户昵称
	 *
	 * @param userName 用户UserName
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
	 * @param userName 用户UserName
	 * @return 群名称
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
	 * @param groupName 群UserName
	 * @param userName  成员UserName
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
	 * @param groupName 群UserName
	 * @param userName  成员UserName
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
	 * @param groupName 群UserName
	 * @param userName  成员UserName
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
