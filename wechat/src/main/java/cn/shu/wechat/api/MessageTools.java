package cn.shu.wechat.api;


import cn.shu.wechat.core.Core;
import cn.shu.wechat.utils.*;
import cn.shu.wechat.utils.enums.ReplyMsgTypeEnum;
import cn.shu.wechat.utils.enums.VerifyFriendEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import cn.shu.wechat.beans.sync.AddMsgList;
import cn.shu.wechat.beans.sync.RecommendInfo;
import cn.shu.wechat.utils.enums.StorageLoginInfoEnum;
import cn.shu.wechat.utils.enums.URLEnum;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.Resource;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 消息处理类
 * 
 * @author SXS
 * @date 创建时间：2017年4月23日 下午2:30:37
 * @version 1.1
 *
 */
@Log4j2
public class MessageTools {

	//炸弹消息队列
	public final static Map<String,Integer> bombMsgMao =new Hashtable<>();

	private static MyHttpClient myHttpClient;

	@Resource
	public static void setMyHttpClient(MyHttpClient myHttpClient) {
		MessageTools.myHttpClient = myHttpClient;
	}


	/**
	 * 根据指定类型发送消息
	 * @param results
	 * @param id id
	 */
	public static void sendMsgByUserId(List<Result> results, String id){
		if(results == null ||results.isEmpty()){
			return;
		}
		for (Result result : results) {
			//result若指定接收人
			if (StringUtils.isNotEmpty(result.toUserName)){
				id = result.toUserName;
			}
			//发送延迟
			if (result.sleep != null && result.sleep > 0){
				SleepUtils.sleep(result.sleep);

			}
 			log.info((bombMsgMao.get(id) == null?"":bombMsgMao.get(id)) +" : "+ LogUtil.printToMeg(id,result.msg));
			switch (result.replyMsgTypeEnum){
				case PIC://图片消息
					sendPicMsgByUserId(id,result.msg);
					break;
				case TEXT://文本消息
					sendTextMsgByUserId(result.msg,id);
					break;
				default:
					sendFileMsgByUserId(id,result.msg);
			}
		}

	}

	/**
	 * 根据UserName发送文本消息
	 *
	 * @author SXS
	 * @date 2017年5月4日 下午11:17:38
	 * @param content
	 * @param toUserName
	 */
	public static void sendTextMsgByUserId( String content, String toUserName) {
		String url = String.format(URLEnum.WEB_WX_SEND_MSG.getUrl(), Core.getLoginInfoMap().get("url"));
		Map<String, Object> msgMap = new HashMap<String, Object>();
		msgMap.put("Type", 1);
		msgMap.put("Content", content);
		msgMap.put("FromUserName", Core.getUserName());
		msgMap.put("ToUserName", toUserName == null ? Core.getUserName() : toUserName);
		msgMap.put("LocalID", new Date().getTime() * 10);
		msgMap.put("ClientMsgId", new Date().getTime() * 10);
		Map<String, Object> paramMap = Core.getParamMap();
		paramMap.put("Msg", msgMap);
		paramMap.put("Scene", 0);
		try {
			String paramStr = JSON.toJSONString(paramMap);
			HttpEntity entity = myHttpClient.doPost(url, paramStr);
			String s = EntityUtils.toString(entity, Consts.UTF_8);
		//	System.out.println(s);
		} catch (Exception e) {
			log.error("webWxSendMsg", e);
		}
	}

	/**
	 * 上传多媒体文件到 微信服务器，目前应该支持3种类型: 1. pic 直接显示，包含图片，表情 2.video 3.doc 显示为文件，包含PDF等
	 * 
	 * @author SXS
	 * @date 2017年5月7日 上午12:41:13
	 * @param filePath
	 * @return
	 */
	private static JSONObject webWxUploadMedia(String filePath) {
		File f = new File(filePath);
		if (!f.exists() && f.isFile()) {
			log.info("file is not exist");
			return null;
		}
		//1M以上视频发不出去
		long fileSize = f.length();
		if (f.getName().toLowerCase().endsWith(".mp4")){
			int bitRate = 800000;
			while (fileSize> 1024 * 1024){
				f = MediaUtil.compressionVideo(f,"/compression/"+f.getName()+".mp4",bitRate);
				fileSize = f.length();
				bitRate = (int)(bitRate/2);
			}
		}
		String url = String.format(URLEnum.WEB_WX_UPLOAD_MEDIA.getUrl(), Core.getLoginInfoMap().get("fileUrl"));
		String mimeType = new MimetypesFileTypeMap().getContentType(f);
		String mediaType = "";
		if (mimeType == null) {
			mimeType = "text/plain";
		} else {
			mediaType = mimeType.split("/")[0].equals("image") ? "pic" : "doc";
		}
		 if ("pic".equals(mediaType) && fileSize > 1024 * 1024 ){
			f = MediaUtil.compressImage(f,1024 * 1024 );
		 }
		fileSize = f.length();
		String lastModifieDate = new SimpleDateFormat("yyyy MM dd HH:mm:ss").format(new Date());

		String passTicket = (String) Core.getLoginInfoMap().get("pass_ticket");
		String clientMediaId = String.valueOf(new Date().getTime())
				+ String.valueOf(new Random().nextLong()).substring(0, 4);
		String webwxDataTicket = MyHttpClient.getCookie("webwx_data_ticket");
		if (webwxDataTicket == null) {
			log.error("get cookie webwx_data_ticket error");
			return null;
		}

		Map<String, Object> paramMap = Core.getParamMap();

		paramMap.put("ClientMediaId", clientMediaId);
		paramMap.put("TotalLen", fileSize);
		paramMap.put("StartPos", 0);
		paramMap.put("DataLen", fileSize);
		paramMap.put("MediaType", 4);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

		builder.addTextBody("id", "WU_FILE_0", ContentType.TEXT_PLAIN);
		builder.addTextBody("name", filePath, ContentType.TEXT_PLAIN);
		builder.addTextBody("type", mimeType, ContentType.TEXT_PLAIN);
		builder.addTextBody("lastModifieDate", lastModifieDate, ContentType.TEXT_PLAIN);
		builder.addTextBody("size", String.valueOf(fileSize), ContentType.TEXT_PLAIN);
		builder.addTextBody("mediatype", mediaType, ContentType.TEXT_PLAIN);
		builder.addTextBody("uploadmediarequest", JSON.toJSONString(paramMap), ContentType.TEXT_PLAIN);
		builder.addTextBody("webwx_data_ticket", webwxDataTicket, ContentType.TEXT_PLAIN);
		builder.addTextBody("pass_ticket", passTicket, ContentType.TEXT_PLAIN);
		builder.addBinaryBody("filename", f, ContentType.create(mimeType), filePath);
		HttpEntity reqEntity = builder.build();
		HttpEntity entity = myHttpClient.doPostFile(url, reqEntity);
		if (entity != null) {
			try {
				String result = EntityUtils.toString(entity, Consts.UTF_8);
				return JSON.parseObject(result);
			} catch (Exception e) {
				log.error("webWxUploadMedia 错误： ", e);
			}

		}
		return null;
	}



	/**
	 * 根据用户id发送图片消息
	 * 
	 * @author SXS
	 * @date 2017年5月7日 下午10:34:24
	 * @param userId
	 * @param filePath
	 * @return
	 */
	public static boolean sendPicMsgByUserId(String userId, String filePath) {
		JSONObject responseObj = webWxUploadMedia(filePath);
		if (responseObj != null) {
			String mediaId = responseObj.getString("MediaId");
			if (mediaId != null) {
				return webWxSendMsgImg(userId, mediaId);
			}
		}
		return false;
	}
	/**
	 * 发送图片消息，内部调用
	 * 
	 * @author SXS
	 * @date 2017年5月7日 下午10:38:55
	 * @return
	 */
	private static boolean webWxSendMsgImg(String userId, String mediaId) {
		String url = String.format("%s/webwxsendmsgimg?fun=async&f=json&pass_ticket=%s", Core.getLoginInfoMap().get("url"),
				Core.getLoginInfoMap().get("pass_ticket"));
		Map<String, Object> msgMap = new HashMap<String, Object>();
		msgMap.put("Type", 3);
		msgMap.put("MediaId", mediaId);
		msgMap.put("FromUserName", Core.getUserSelf().getString("UserName"));
		msgMap.put("ToUserName", userId);
		String clientMsgId = String.valueOf(new Date().getTime())
				+ String.valueOf(new Random().nextLong()).substring(1, 5);
		msgMap.put("LocalID", clientMsgId);
		msgMap.put("ClientMsgId", clientMsgId);
		Map<String, Object> paramMap = Core.getParamMap();
		paramMap.put("BaseRequest", Core.getParamMap().get("BaseRequest"));
		paramMap.put("Msg", msgMap);
		String paramStr = JSON.toJSONString(paramMap);
		HttpEntity entity = myHttpClient.doPost(url, paramStr);
		if (entity != null) {
			try {
				String result = EntityUtils.toString(entity, Consts.UTF_8);
				return JSON.parseObject(result).getJSONObject("BaseResponse").getInteger("Ret") == 0;
			} catch (Exception e) {
				log.error("webWxSendMsgImg 错误： ", e);
			}
		}
		return false;

	}
	/**
	 * 发送图片消息，内部调用
	 *
	 * @author SXS
	 * @date 2017年5月7日 下午10:38:55
	 * @return
	 */

	/**
	 * 根据用户id发送文件
	 * 
	 * @author SXS
	 * @date 2017年5月7日 下午11:57:36
	 * @param userId
	 * @param filePath
	 * @return
	 */
	public static boolean sendFileMsgByUserId(String userId, String filePath) {
		String title = new File(filePath).getName();
		Map<String, String> data = new HashMap<String, String>();
		data.put("appid", Config.API_WXAPPID);
		data.put("title", title);
		data.put("totallen", "");
		data.put("attachid", "");
		data.put("type", "6"); // APPMSGTYPE_ATTACH
		data.put("fileext", title.split("\\.")[1]); // 文件后缀
		JSONObject responseObj = webWxUploadMedia(filePath);
		if (responseObj != null) {
			data.put("totallen", responseObj.getString("StartPos"));
			data.put("attachid", responseObj.getString("MediaId"));
		} else {
			log.error("sednFileMsgByUserId 错误: ", data);
		}
		return webWxSendAppMsg(userId, data);
	}
	/**
	 * 根据用户id发送文件
	 *
	 * @author SXS
	 * @date 2017年5月7日 下午11:57:36
	 * @param userId
	 * @return
	 */
	public static boolean sendAppMsgByUserId(String userId) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("appid", Config.API_WXAPPID);
		data.put("title", "测试");
		data.put("totallen", "");
		data.put("attachid", "");
		data.put("type", "5"); // APPMSGTYPE_ATTACH
		//data.put("fileext", ""); // 文件后缀
		data.put("url","www.baidu.com");
/*		JSONObject responseObj = webWxUploadMedia(filePath);
		if (responseObj != null) {
			data.put("totallen", responseObj.getString("StartPos"));
			data.put("attachid", responseObj.getString("MediaId"));
		} else {
			log.error("sednFileMsgByUserId 错误: ", data);
		}*/
		return webWxSendAppMsg(userId, data);
	}



	/**
	 * 内部调用
	 * 暂时不能使用
	 * @author SXS
	 * @date 2017年5月10日 上午12:21:28
	 * @param userId
	 * @param data
	 * @return
	 */
	private static boolean webWxSendAppMsg(String userId, Map<String, String> data) {
		String url = String.format("%s/webwxsendappmsg?fun=async&f=json&pass_ticket=%s", Core.getLoginInfoMap().get("url"),
				Core.getLoginInfoMap().get("pass_ticket"));
		String clientMsgId = String.valueOf(new Date().getTime())
				+ String.valueOf(new Random().nextLong()).substring(1, 5);
		String content = "<appmsg appid='wxeb7ec651dd0aefa9' sdkver=''><title>" + data.get("title")
				+ "</title><des></des><action></action><type>6</type><content></content><url></url><lowurl></lowurl>"
				+ "<appattach><totallen>" + data.get("totallen") + "</totallen><attachid>" + data.get("attachid")
				+ "</attachid><fileext>" + data.get("fileext") + "</fileext></appattach><extinfo></extinfo></appmsg>";
		Map<String, Object> msgMap = new HashMap<String, Object>();
		msgMap.put("Type", data.get("type"));
		msgMap.put("Content", content);
		msgMap.put("FromUserName", Core.getUserSelf().getString("UserName"));
		msgMap.put("ToUserName", userId);
		msgMap.put("LocalID", clientMsgId);
		msgMap.put("ClientMsgId", clientMsgId);
		/*
		 * Map<String, Object> paramMap = new HashMap<String, Object>();
		 * 
		 * @SuppressWarnings("unchecked") Map<String, Map<String, String>>
		 * baseRequestMap = (Map<String, Map<String, String>>)
		 * Core.getLoginInfo() .get("baseRequest"); paramMap.put("BaseRequest",
		 * baseRequestMap.get("BaseRequest"));
		 */

		Map<String, Object> paramMap = Core.getParamMap();
		paramMap.put("Msg", msgMap);
		paramMap.put("Scene", 0);
		String paramStr = JSON.toJSONString(paramMap);
		HttpEntity entity = myHttpClient.doPost(url, paramStr);
		if (entity != null) {
			try {
				String result = EntityUtils.toString(entity, Consts.UTF_8);
				return JSON.parseObject(result).getJSONObject("BaseResponse").getInteger("Ret") == 0;
			} catch (Exception e) {
				log.error("错误: ", e);
			}
		}
		return false;
	}

	/**
	 * 被动添加好友
	 * 
	 * @date 2017年6月29日 下午10:08:43
	 * @param msg
	 * @param accept
	 *            true 接受 false 拒绝
	 */
	public static void addFriend(AddMsgList msg, boolean accept) {
		if (!accept) { // 不添加
			return;
		}
		int status = VerifyFriendEnum.ACCEPT.getCode(); // 接受好友请求
		RecommendInfo recommendInfo = msg.getRecommendInfo();
		String userName = recommendInfo.getUserName();
		String ticket = recommendInfo.getTicket();
		// 更新好友列表
		// TODO 此处需要更新好友列表
		// Core.getContactList().add(msg.getJSONObject("RecommendInfo"));

		String url = String.format(URLEnum.WEB_WX_VERIFYUSER.getUrl(), Core.getLoginInfoMap().get("url"),
				String.valueOf(System.currentTimeMillis() / 3158L), Core.getLoginInfoMap().get("pass_ticket"));

		List<Map<String, Object>> verifyUserList = new ArrayList<Map<String, Object>>();
		Map<String, Object> verifyUser = new HashMap<String, Object>();
		verifyUser.put("Value", userName);
		verifyUser.put("VerifyUserTicket", ticket);
		verifyUserList.add(verifyUser);

		List<Integer> sceneList = new ArrayList<Integer>();
		sceneList.add(33);

		JSONObject body = new JSONObject();
		body.put("BaseRequest", Core.getParamMap().get("BaseRequest"));
		body.put("Opcode", status);
		body.put("VerifyUserListSize", 1);
		body.put("VerifyUserList", verifyUserList);
		body.put("VerifyContent", "");
		body.put("SceneListCount", 1);
		body.put("SceneList", sceneList);
		body.put("skey", Core.getLoginInfoMap().get(StorageLoginInfoEnum.skey.getKey()));

		String result = null;
		try {
			String paramStr = JSON.toJSONString(body);
			HttpEntity entity = myHttpClient.doPost(url, paramStr);
			result = EntityUtils.toString(entity, Consts.UTF_8);
		} catch (Exception e) {
			log.error("webWxSendMsg", e);
		}

		if (StringUtils.isBlank(result)) {
			log.error("被动添加好友失败");
		}

		log.debug(result);

	}
	/**
	 * 回复的消息类型封装
	 */
	@Builder
	public static class Result{
		//消息类型
		private final ReplyMsgTypeEnum replyMsgTypeEnum;
		//文本消息或图片、文件路径
		private final String msg;
		//延迟发送
		private final Long sleep;
		//消息接收者
		private final String toUserName;
	}
}
