package cn.shu.wechat.core;

import java.util.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import cn.shu.wechat.enums.parameters.BaseParaEnum;


/**
 * 核心存储类，全局只保存一份，单例模式
 *
 * @author SXS
 * @date 创建时间：2017年4月23日 下午2:33:56
 * @version 1.1
 *
 */
@Data
public class Core {

/*	private static Core instance;

	private Core() {

	}

	public static Core getInstance() {
		if (instance == null) {
			synchronized (Core.class) {
				instance = new Core();
			}
		}
		return instance;
	}*/

	static private boolean alive = false;
	static private int memberCount = 0;

	static private String indexUrl;

	static private String userName;
	static private String nickName;
	// 登陆账号自身信息
	static private JSONObject userSelf;
	// 好友+群聊+公众号+特殊账号
	static private Map<String,JSONObject> memberMap = new HashMap<>();
	// 好友
	static private Map<String,JSONObject> contactMap = new HashMap<>();
	// 群
	static private Map<String,JSONObject> groupMap = new HashMap<>();
	// 群聊成员字典
	static private Map<String, JSONArray> groupMemberMap = new HashMap<String, JSONArray>();
	// 公众号／服务号
	static private Map<String,JSONObject> publicUsersMap = new HashMap<>();
	// 特殊账号
	static private Map<String,JSONObject> specialUsersMap = new HashMap<>();
	// 群ID列表
	static private Set<String> groupIdSet = new HashSet<>();
	// 群NickName列表
	static private Set<String> groupNickNameSet = new HashSet<String>();
	//用户信息
	static private Map<String, JSONObject> userInfoMap = new HashMap<>();
	//登录信息
	static  Map<String, Object> loginInfoMap = new HashMap<String, Object>();
	//好友头像路径
	static private Map<String, String> contactHeadImgPath = new HashMap<String, String>();
	// CloseableHttpClient MyHttpClient = HttpClients.createDefault();

	static private String uuid = null;

	static private boolean useHotReload = false;
	static private String hotReloadDir = "itchat.pkl";
	static private int receivingRetryCount = 5;

	static private long lastNormalRetcodeTime; // 最后一次收到正常retcode的时间，秒为单位
	/**
	 * 请求参数
	 */
	public static Map<String, Object> getParamMap() {

		return new HashMap<String, Object>(1) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			{
				Map<String, String> map = new HashMap<String, String>();
				for (BaseParaEnum baseRequest : BaseParaEnum.values()) {
					map.put(baseRequest.para(), Core.getLoginInfoMap().get(baseRequest.value()).toString());
				}
				put("BaseRequest", map);
			}
		};
	}

	public static boolean isAlive() {
		return alive;
	}

	public static void setAlive(boolean alive) {
		Core.alive = alive;
	}

	public static int getMemberCount() {
		return memberCount;
	}

	public static void setMemberCount(int memberCount) {
		Core.memberCount = memberCount;
	}

	public static String getIndexUrl() {
		return indexUrl;
	}

	public static void setIndexUrl(String indexUrl) {
		Core.indexUrl = indexUrl;
	}

	public static String getUserName() {
		return userName;
	}

	public static void setUserName(String userName) {
		Core.userName = userName;
	}

	public static String getNickName() {
		return nickName;
	}

	public static void setNickName(String nickName) {
		Core.nickName = nickName;
	}


	public static JSONObject getUserSelf() {
		return userSelf;
	}

	public static void setUserSelf(JSONObject userSelf) {
		Core.userSelf = userSelf;
	}

	public static Map<String, JSONObject> getMemberMap() {
		return memberMap;
	}

	public static void setMemberMap(Map<String, JSONObject> memberMap) {
		Core.memberMap = memberMap;
	}

	public static Map<String, JSONObject> getContactMap() {
		return contactMap;
	}

	public static void setContactMap(Map<String, JSONObject> contactMap) {
		Core.contactMap = contactMap;
	}

	public static Map<String, JSONObject> getGroupMap() {
		return groupMap;
	}

	public static void setGroupMap(Map<String, JSONObject> groupMap) {
		Core.groupMap = groupMap;
	}

	public static Map<String, JSONArray> getGroupMemberMap() {
		return groupMemberMap;
	}

	public static void setGroupMemberMap(Map<String, JSONArray> groupMemberMap) {
		Core.groupMemberMap = groupMemberMap;
	}

	public static Map<String, JSONObject> getPublicUsersMap() {
		return publicUsersMap;
	}

	public static void setPublicUsersMap(Map<String, JSONObject> publicUsersMap) {
		Core.publicUsersMap = publicUsersMap;
	}

	public static Map<String, JSONObject> getSpecialUsersMap() {
		return specialUsersMap;
	}

	public static void setSpecialUsersMap(Map<String, JSONObject> specialUsersMap) {
		Core.specialUsersMap = specialUsersMap;
	}

	public static Set<String> getGroupIdSet() {
		return groupIdSet;
	}

	public static void setGroupIdSet(Set<String> groupIdSet) {
		Core.groupIdSet = groupIdSet;
	}

	public static Set<String> getGroupNickNameSet() {
		return groupNickNameSet;
	}

	public static void setGroupNickNameSet(Set<String> groupNickNameSet) {
		Core.groupNickNameSet = groupNickNameSet;
	}

	public static Map<String, JSONObject> getUserInfoMap() {
		return userInfoMap;
	}

	public static void setUserInfoMap(Map<String, JSONObject> userInfoMap) {
		Core.userInfoMap = userInfoMap;
	}

	public static Map<String, Object> getLoginInfoMap() {
		return loginInfoMap;
	}

	public static void setLoginInfoMap(Map<String, Object> loginInfoMap) {
		Core.loginInfoMap = loginInfoMap;
	}

	public static Map<String, String> getContactHeadImgPath() {
		return contactHeadImgPath;
	}

	public static void setContactHeadImgPath(Map<String, String> contactHeadImgPath) {
		Core.contactHeadImgPath = contactHeadImgPath;
	}

	public static String getUuid() {
		return uuid;
	}

	public static void setUuid(String uuid) {
		Core.uuid = uuid;
	}

	public static boolean isUseHotReload() {
		return useHotReload;
	}

	public static void setUseHotReload(boolean useHotReload) {
		Core.useHotReload = useHotReload;
	}

	public static String getHotReloadDir() {
		return hotReloadDir;
	}

	public static void setHotReloadDir(String hotReloadDir) {
		Core.hotReloadDir = hotReloadDir;
	}

	public static int getReceivingRetryCount() {
		return receivingRetryCount;
	}

	public static void setReceivingRetryCount(int receivingRetryCount) {
		Core.receivingRetryCount = receivingRetryCount;
	}

	public static long getLastNormalRetcodeTime() {
		return lastNormalRetcodeTime;
	}

	public static void setLastNormalRetcodeTime(long lastNormalRetcodeTime) {
		Core.lastNormalRetcodeTime = lastNormalRetcodeTime;
	}
}
