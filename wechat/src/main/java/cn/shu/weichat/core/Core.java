package cn.shu.weichat.core;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import cn.shu.weichat.utils.MyHttpClient;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import cn.shu.weichat.beans.BaseMsg;
import cn.shu.weichat.utils.enums.parameters.BaseParaEnum;


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

	private static Core instance;

	private Core() {

	}

	public static Core getInstance() {
		if (instance == null) {
			synchronized (Core.class) {
				instance = new Core();
			}
		}
		return instance;
	}

	boolean alive = false;
	private int memberCount = 0;

	private String indexUrl;

	private String userName;
	private String nickName;
	//消息队列（阻塞、线程安全）
	private LinkedBlockingQueue<BaseMsg> msgList = new LinkedBlockingQueue<>();
	// 登陆账号自身信息
	private JSONObject userSelf;
	// 好友+群聊+公众号+特殊账号
	private Map<String,JSONObject> memberMap = new HashMap<>();
	// 好友
	private Map<String,JSONObject> contactMap = new HashMap<>();
	// 群
	private Map<String,JSONObject> groupMap = new HashMap<>();
	// 群聊成员字典
	private Map<String, JSONArray> groupMemberMap = new HashMap<String, JSONArray>();
	// 公众号／服务号
	private Map<String,JSONObject> publicUsersMap = new HashMap<>();
	// 特殊账号
	private Map<String,JSONObject> specialUsersMap = new HashMap<>();
	// 群ID列表
	private Set<String> groupIdSet = new HashSet<>();
	// 群NickName列表
	private Set<String> groupNickNameSet = new HashSet<String>();
	//用户信息
	private Map<String, JSONObject> userInfoMap = new HashMap<>();
	//登录信息
	Map<String, Object> loginInfoMap = new HashMap<String, Object>();
	// CloseableHttpClient httpClient = HttpClients.createDefault();
	MyHttpClient myHttpClient = MyHttpClient.getInstance();
	String uuid = null;

	boolean useHotReload = false;
	String hotReloadDir = "itchat.pkl";
	int receivingRetryCount = 5;

	private long lastNormalRetcodeTime; // 最后一次收到正常retcode的时间，秒为单位

	/**
	 * 请求参数
	 */
	public Map<String, Object> getParamMap() {
		return new HashMap<String, Object>(1) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			{
				Map<String, String> map = new HashMap<String, String>();
				for (BaseParaEnum baseRequest : BaseParaEnum.values()) {
					map.put(baseRequest.para(), getLoginInfoMap().get(baseRequest.value()).toString());
				}
				put("BaseRequest", map);
			}
		};
	}
	public String  getRemarkNameByUserName(String username) {
		Map<String,JSONObject> object;
		if(username.startsWith("@@")){
			object= groupMap;
		}else {
			object= contactMap;
		}
		JSONObject  jsonObject = object.get(username);
		if (jsonObject ==null)return username;
		if (username.equals(jsonObject.getString("UserName")) ) {
			if(!jsonObject.getString("RemarkName").equals("")){
				return jsonObject.getString("RemarkName");
			}
			return jsonObject.getString("NickName") ;
		}

		return username;
	}

}
