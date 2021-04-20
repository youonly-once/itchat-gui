package cn.shu.wechat.api;


import cn.shu.wechat.core.Core;
import cn.shu.wechat.utils.CommonTools;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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
	 * 根据用户名获取用户信息
	 *
	 * @param userName 用户UserName
	 * @return 用户信息
	 */
	private static JSONObject getContactByUserName(String userName) {
		Map<String, JSONObject> contactMap = Core.getMemberMap();
		return contactMap.get(userName);
	}

	/**
	 * 根据用户名获取用户显示名称
	 * 有备注显示备注，无备注显示昵称
	 * 群则直接显示昵称
	 * @param userName 用户UserName
	 * @return 备注
	 */
	public static String getContactDisplayNameByUserName(String userName) {
		String remarkNameByPersonUserName = getContactRemarkNameByUserName(userName);
		if (StringUtils.isNotEmpty(remarkNameByPersonUserName)){
			return remarkNameByPersonUserName;
		}
		String nickNameByPersonUserName = getContactNickNameByUserName(userName);
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
	public static String getContactRemarkNameByUserName(String userName) {
		//群只有备注 没有昵称
		if (userName.startsWith("@@")){
			return getContactNickNameByUserName(userName);
		}
		JSONObject contactByUserName = getContactByUserName(userName);
		if(contactByUserName == null){
			return null;
		}
		return CommonTools.emojiFormatter(contactByUserName.getString("RemarkName"));
	}



	/**
	 * 根据用户名获取普通用户昵称
	 *
	 * @param userName 用户UserName
	 * @return 备注
	 */
	public static String getContactNickNameByUserName(String userName) {
		JSONObject contactByUserName = getContactByUserName(userName);
		if(contactByUserName == null){
			return null;
		}
		return CommonTools.emojiFormatter(contactByUserName.getString("NickName"));
	}


	/**
	 * 获取群成员
	 *
	 * @param groupName 群UserName
	 * @param userName  成员UserName
	 * @return 成员
	 */
	private static JSONObject getMemberOfGroup(String groupName, String userName) {
		Map<String, JSONObject> groupMemeberMap = Core.getGroupMap();
		JSONObject group = groupMemeberMap.get(groupName);
		if (group == null || userName ==null) {
			return null;
		}
		JSONArray memberList = group.getJSONArray("MemberList");
		if (memberList == null || memberList.size() <= 0){
			return null;
		}
		try {
			for (Object member : memberList) {
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
				? CommonTools.emojiFormatter(groupUserOfGroup.getString("NickName"))
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
			return CommonTools.emojiFormatter(displayName);
		}
		displayName = groupUserOfGroup.getString("NickName");
		if (!StringUtils.isEmpty(displayName)){
			return CommonTools.emojiFormatter(displayName);
		}
		return userName;
	}

}
