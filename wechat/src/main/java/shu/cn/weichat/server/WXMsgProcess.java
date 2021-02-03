package shu.cn.weichat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import bean.tuling.response.Results;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.ansj.domain.Result;
import org.ansj.splitWord.analysis.ToAnalysis;

import shu.cn.weichat.utils.CodeParse;
import shu.cn.weichat.utils.SleepUtils;
import utils.HttpUtil;
import utils.TuLingUtil;
import weixin.exception.WXException;
import weixin.utils.WXUntil;

/**
 * @author ShuXinSheng
 * @version 创建时间：2020年3月22日 上午11:02:50
 * 类说明
 */
@Log4j2
public class WXMsgProcess {

	static void processImageMsg(Map<String, String> map,PrintWriter out) throws IOException, JSONException, NullPointerException, WXException {
		String picurl=map.get("PicUrl");
		// 获取参数中的openid
		String userOpenid = map.get("FromUserName");
		// 获取参数中的openid
		String myOpendid = map.get("ToUserName");
		String msgId = map.get("MsgId");
		// 图片解码
		String imageText = CodeParse.parseQRCode(picurl);

		//解码成功
		if (imageText != null && !imageText.trim().isEmpty()) {
			String text=otherMsgProcess(imageText, userOpenid);
			out.println(WXUntil.replyTextMsg(text, userOpenid, myOpendid));
			//解码失败
		} else {
			out.println(WXUntil.replyTextMsg("图片太模糊了...", userOpenid, myOpendid));
		}
	}
	/**
	 * 处理文字消息
	 * @param map 消息内容
	 * @param out
	 * @throws IOException
	 * @throws WXException
	 * @throws NullPointerException
	 * @throws JSONException
	 */
	static void processTextMsg(Map<String, String> map,PrintWriter out) throws IOException, JSONException, NullPointerException, WXException {
		String receContent=map.get("Content");
		String userOpenid = map.get("FromUserName");// 获取参数中的openid
		String myOpendid = map.get("ToUserName");// 获取参数中的openid
		// 去掉空格 全角逗号替换为半角
		receContent = receContent.trim().replace("，", ",");
		String replyContent = getReplyContent(map.get("FromUserName"),receContent);
		log.info("准备回复消息："+replyContent);
		if (!replyContent.isEmpty()) {// 回复消息
			// 图片消息
			if (replyContent.startsWith("img")) {
				out.println(WXUntil.replyImageMsg(replyContent.substring(3),userOpenid, myOpendid));
				// 文章消息
			} else if (replyContent.startsWith("material")) {
				/*  HashMap<String,String> m=new HashMap<String,String>();
				  String[] mStrings=replyContent.substring(8).split(",");
					    m.put("title", mStrings[0]);
					    m.put("thumb_url", mStrings[1]);
					    m.put("digest", mStrings[2]);
					    m.put("url", mStrings[3]);*/
				//out.println(WXUntil.replyMaterialMsg(m,userOpenid, myOpendid));
				//发送客服消息

				//WXUntil.sendCustomMsg(stz.nextToken(), userOpenid, WXUntil.getLocalServerAccessToken());

			} else {
				out.println(WXUntil.replyTextMsg(replyContent, userOpenid, myOpendid));
			}

		}else{
			out.println("success");
		}
	}

	/**
	 * 处理事件消息
	 * @throws WXException
	 *
	 */
	static void processEventMsg(Map<String, String> map,PrintWriter out) throws IOException, WXException {


		//关注事件 或者 帮助菜单
		if ("subscribe".equals(map.get("Event")) || "help".equals(map.get("EventKey"))) {
			out.println(WXUntil.replyTextMsg(WXServletConfig.ALLTIP_STR,map.get("FromUserName"), map.get("ToUserName")));

			// 模板消息事件
		} else if ("TEMPLATESENDJOBFINISH".equals(map.get("Event"))) {
			out.println("success");
			// 获取地理位置事件 推送位置给UP
		} else if ("LOCATION".equals(map.get("Event"))) {

			out.println("success");

			String longitude=map.get("Longitude");
			String latitude =map.get("Latitude");
			WXLocationProcess.transLocation(longitude,latitude,map.get("FromUserName"));


		}
	}

	static String getReplyContent(final String userOpenid,final String receiveContent) throws JSONException, NullPointerException, WXException, IOException {

		//以下不需要密码

		if (receiveContent.indexOf("二维码") != -1 && receiveContent.indexOf("二维码") != 0) {// 条码站点扫描记录

			String imgPath = WXQRCodeProcess.createCodeImage(receiveContent.substring(0, receiveContent.indexOf("二维码")), 2);
			return "img" + WXUntil.uploadCodeImage(WXUntil.getLocalServerAccessToken(),imgPath);
		} else if (receiveContent.indexOf("条形码") != -1 && receiveContent.indexOf("条形码") != 0) {// 条码站点扫描记录

			String imgPath = WXQRCodeProcess.createCodeImage(receiveContent.substring(0, receiveContent.indexOf("条形码")), 1);
			return "img" + WXUntil.uploadCodeImage(WXUntil.getLocalServerAccessToken(),imgPath);
		}
		//用户帮助
		if ( receiveContent.equals("?") || receiveContent.equals("？")
				|| receiveContent.equals("help") || receiveContent.equals("帮助")) {
			return WXServletConfig.ALLTIP_STR;
		}else if(receiveContent.startsWith("手机号码变更通知") || receiveContent.startsWith("电话变更通知")){
			//
			return numberModifyNoti(receiveContent,userOpenid);

		}
		else {// 回复幽默消息 去掉密码
			return otherMsgProcess(receiveContent,userOpenid);

		}
	}
	/**
	 * 用户消息匹配文章、匹配tipstr
	 * @param receiveContent 用户消息
	 * @param userOpenid 用户openid
	 * @return
	 * @throws WXException
	 * @throws NullPointerException
	 * @throws JSONException
	 * @throws IOException
	 */
	static String otherMsgProcess(String receiveContent,String userOpenid) throws JSONException, NullPointerException, WXException, IOException{
		if (receiveContent.isEmpty()) {
			return "不要以逗号开头哦";
		}
		//用户消息是否匹配文章
		Result result=ToAnalysis.parse(receiveContent);
		log.info("分词结果："+result.toString());
		boolean b=WXCircleProcess.matchMaterial(result,userOpenid);
		if (b) {
			return "material";//+str;
		}else{
			//用户消息是否匹配 指定的 tipstr
			/*str=WXCircleProcess.matchTipStr(result);
			if (str!=null) {
				return str;
			}*/
			for (Results results : TuLingUtil.robotMsgTuling(receiveContent).getResults()) {
				return results.getValues().getText();
			}
			return "";
		}
	}
	/**
	 *
	 * @param receiveContent      分词结果
	 * @param userOpenid  用户Openid
	 * @return   公众号文章匹配输入结果
	 */

	static String numberModifyNoti(String receiveContent,final String userOpenid) {

		StringTokenizer stringTokenizer=new StringTokenizer(receiveContent, ",");
		if (stringTokenizer.countTokens()!=5) {
			return "格式不正确！";
		}
		stringTokenizer.nextToken();
		final String name=stringTokenizer.nextToken();
		final String numberOld=stringTokenizer.nextToken();
		final String numberNew=stringTokenizer.nextToken();

		if(!(orPhoneNumber(numberOld) && orPhoneNumber(numberNew))){

			return "手机号码格式不正确！";
		}
		if(numberOld.equals(numberNew)){
			return "新旧号码一致！";
		}

		new Thread(new Runnable() {

			@Override
			public void run() {



				String title=" "+name+" 手机号码变更通知";
				String content="\n\t姓名    ：" + name + "\n"
						+"\t旧号码：" + numberOld + "\n"
						+"\t新号码：" + numberNew + "\n";
				String time="";
				//获得我的昵称
				List<Object> list=new ArrayList<>();
				list.add(userOpenid);
				try {
					WXUntil.getLocalServerAccessToken();
				} catch (JSONException | NullPointerException | WXException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				HashMap<String, Object> userRemark;
				try {
					userRemark = WXUntil.getUserNameAndIdMap(list, "namevalue",WXUntil.getLocalServerAccessToken());

					if(userRemark==null || userRemark.get(userOpenid)==null){
						time="通过微信公众号命令发送";
					}else{
						time="<"+userRemark.get(userOpenid).toString()+"> 通过微信公众号命令发送";
					}
				} catch (JSONException | NullPointerException | WXException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String remark="\n\t请各位领导同事惠存";


				// 获取所有粉丝信息



				// 获得分组下所有用户的ID
				List<Object> allUserIdArrray;
				try {
					String access_token=WXUntil.getLocalServerAccessToken();
					allUserIdArrray = WXUntil.getAllUserOpenId("", access_token);
					if (allUserIdArrray == null) {
						content="\n\t发送失败:get group error\n";
					}
					for (int i = 0,n=allUserIdArrray.size(); i <n ; i++) {
						String teString=WXUntil.getTemplateData(allUserIdArrray.get(i).toString(), WXUntil.createDefDataMap(title, content, time,remark,""));
						WXUntil.sendTemplateMsg(teString,WXUntil.getLocalServerAccessToken());
					}
				} catch (JSONException | NullPointerException | WXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}





			}
		}).start();
		// TODO Auto-generated method stub
		return "正在通知所有人,请稍后...";
	}
	private static   boolean orPhoneNumber(String phoneNumber) {
		if (phoneNumber == null || phoneNumber.trim().isEmpty())
			return false;
		String regex = "^1[3|4|5|8][0-9]\\d{8}$";
		return phoneNumber.matches(regex);
	}



	/*
	 * 语音转文字
	 */
	private String voice2Text( Map<String, String> map ,String mediaId,String format, String token) throws WXException, IOException {
		String text = "";
		/*
		 * if (errcode.equals("0")) { return true; } else if
		 * (weixinBasicInfo.tokenExpire(errcode)) {// token过期
		 */ // 重新POST
		String userOpenid=map.get("FromUserName");// 获取参数中的openid
		String receiveMsgId=map.get("MsgId");
		String uploadurl = "http://api.weixin.qq.com/cgi-bin/media/voice/addvoicetorecofortext?" + "access_token="
				+ token + "&format=" + format + "&voice_id=" + mediaId + "&lang=zh_CN";
		String transurl = "http://api.weixin.qq.com/cgi-bin/media/voice/queryrecoresultfortext?" + "access_token="
				+ token + "&voice_id=" + mediaId + "&lang=zh_CN";

		// 上传语音
		String resultStr = HttpUtil.sendPost(uploadurl, "");
		log.info(resultStr);
		JSONObject jsonObject = JSON.parseObject(resultStr);

		// 上传成功 获取文字结果
		String errcode = jsonObject.getString("errcode");
		if (errcode.equals("0")) {
			String resultStr1 = HttpUtil.sendPost(transurl, "");
			log.info(resultStr);
			JSONObject jsonObject1 = JSON.parseObject(resultStr1);
			// 根据文字做相应处理
			if (jsonObject1.containsKey("errcode")) {
				// token过期重新尝试
				if (WXUntil.tokenExpire(jsonObject1.getString("errcode"))) {
					token=WXUntil.getLocalServerAccessToken();
					if(token!=null){

						voice2Text(map,mediaId, format, token);
					}else{//获取token失败 5秒后重试一次
						SleepUtils.sleep(5000);
						token=WXUntil.getLocalServerAccessToken();
						if(token!=null){
							voice2Text(map,mediaId, format, token);
						}
					}

				}
			} else {
				//text = getReplyContent(userOpenid,jsonObject1.getString("result"));
			}

			// token过期重新尝试
		} else if (WXUntil.tokenExpire(errcode)) {
			token=WXUntil.getLocalServerAccessToken();
			voice2Text(map,mediaId, format, token);
		} else {
			text = "语音识别失败：" + jsonObject.getString("errmsg");
		}


		log.info(text);
		return text;
	}
}
