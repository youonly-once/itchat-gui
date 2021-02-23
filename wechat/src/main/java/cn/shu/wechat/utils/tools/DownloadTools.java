package cn.shu.wechat.utils.tools;

import cn.shu.IMsgHandlerFaceImpl;
import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.api.WechatTools;
import cn.shu.wechat.core.Core;
import lombok.extern.log4j.Log4j2;
import cn.shu.wechat.beans.sync.AddMsgList;
import cn.shu.wechat.utils.MyHttpClient;
import cn.shu.wechat.utils.enums.MsgTypeEnum;
import cn.shu.wechat.utils.enums.URLEnum;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * 下载工具类
 * 
 * @author SXS
 * @date 创建时间：2017年4月21日 下午11:18:46
 * @version 1.1
 *
 */
@Log4j2
public class DownloadTools {

	/**
	 * 处理下载任务
	 * 
	 * @author SXS
	 * @date 2017年4月21日 下午11:00:25
	 * @param msg
	 * @param msgTypeEnum
	 * @param path
	 * @return
	 */
	public static Object getDownloadFn(AddMsgList msg, MsgTypeEnum msgTypeEnum, String path) {
		Map<String, String> headerMap = new HashMap<String, String>();
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		String url = "";
		switch (msgTypeEnum) {
			case PIC:
			case EMOTION:
				url = String.format(URLEnum.WEB_WX_GET_MSG_IMG.getUrl(), (String) Core.getLoginInfoMap().get("url"));
				break;
			case VOICE:
				url = String.format(URLEnum.WEB_WX_GET_VOICE.getUrl(), (String) Core.getLoginInfoMap().get("url"));
				break;
			case VIDEO:
				headerMap.put("Range", "bytes=0-");
				url = String.format(URLEnum.WEB_WX_GET_VIEDO.getUrl(), (String) Core.getLoginInfoMap().get("url"));
				break;
			case APP:
			case MEDIA:
				headerMap.put("Range", "bytes=0-");
				url = String.format(URLEnum.WEB_WX_GET_MEDIA.getUrl(), (String) Core.getLoginInfoMap().get("fileUrl"));
				params.add(new BasicNameValuePair("sender", msg.getFromUserName()));
				params.add(new BasicNameValuePair("mediaid", msg.getMediaId()));
				params.add(new BasicNameValuePair("filename", msg.getFileName()));
				break;
		}
		params.add(new BasicNameValuePair("msgid",String.valueOf( msg.getNewMsgId())));
		params.add(new BasicNameValuePair("skey", (String) Core.getLoginInfoMap().get("skey")));
		HttpEntity entity = MyHttpClient.doGet(url, params, true, headerMap);
		try {
			OutputStream out = new FileOutputStream(path);
			byte[] bytes = EntityUtils.toByteArray(entity);
			out.write(bytes);
			out.flush();
			out.close();
		} catch (Exception e) {
			log.info(e.getMessage());
			return false;
		}
		return null;
	};

	/**
	 * 下载头像
	 * @param relativeUrl 头像地址
	 * @param userName 用户名
	 * @return 返回保存全路径
	 */
	public static String downloadHeadImg(String relativeUrl,String userName){

	String remarkNameByUserName = WechatTools.getRemarkNameByUserName(userName);
			if (userName.startsWith("@@")){
			remarkNameByUserName = WechatTools.getRemarkNameByGroupUserName(userName);
		}
		if (StringUtils.isEmpty(remarkNameByUserName)){
				remarkNameByUserName = userName;
		}
		remarkNameByUserName = DownloadTools.replace(remarkNameByUserName);

		String savePath = IMsgHandlerFaceImpl.savePath + "/headimg/"+ File.separator + remarkNameByUserName + File.separator;
		File file = new File(savePath);
		try {

			if (!file.exists()){
				file.mkdirs();
			}
			file = new File(savePath  + UUID.randomUUID() + ".jpg");
			if (!file.exists()){
				file.createNewFile();
			}
			String url= String.format(URLEnum.WEB_WX_GET_HEAD_IMAGE.getUrl(),relativeUrl);
			HttpEntity entity = MyHttpClient.doGet(url,null,false,null);
			OutputStream out = new FileOutputStream(file);
			byte[] bytes = EntityUtils.toByteArray(entity);
			out.write(bytes);
			out.flush();
			out.close();
		} catch (Exception e) {
			log.info(e.getMessage());

		}
		return file.getAbsolutePath();
	}

	/**
	 * 不可建立文件夹的字符
	 */
	public static String replace(String string) {
		string = string.replace("/", "").
		replace("|", "").
		replace("\\", "").
		replace("*", "").
		replace(":", "").
		replace("\"", "").
		replace("?", "").
		replace("<", "").
				replace("<", "").
				replace(" ", "").
				replace("\n", "").
				replace("\r", "").
				replace("\t", "").
		replace(">", "");
		return string;

	}
}
