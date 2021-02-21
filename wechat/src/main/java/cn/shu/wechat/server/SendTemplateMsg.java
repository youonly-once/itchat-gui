package cn.shu.wechat.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSONException;
import lombok.extern.log4j.Log4j2;
import weixin.exception.WXException;
import weixin.utils.WXUntil;

@Log4j2
public class SendTemplateMsg {


	/*
	 * 接收定位消息的分组
	 * 被定为的用户ID
	 */
	public void sendTemplateMsgLocation(String groupName,String content,double remark,String id) throws JSONException, NullPointerException, WXException, IOException {

		// 分组信息
		String access_token=WXUntil.getLocalServerAccessToken();
		// 获取分组ID
		String groupId = WXUntil.getGroupId(groupName,access_token);
		if (groupId == null || groupId.equals("0")) {
			return;
		}
		// 获得分组下所有用户的ID
		List<Object> allUserIdArrray = WXUntil.getGroupUserOpenId(groupId,access_token);
		if (allUserIdArrray == null) {
			return;
		}

		log.info("发送模板消息");
		// 获得用户名与openid关系
		List<Object> user=new ArrayList<Object>();
		user.add(id);
		HashMap<String, Object> userNameAndIdMap = WXUntil.getUserNameAndIdMap(user,"namevalue",access_token);
		if (userNameAndIdMap == null) {
			return;
		}
		HashMap<String, String> map=new HashMap<String, String>();

		map=WXUntil.createDefDataMap("【"+userNameAndIdMap.get(id)+"的位置】\n", content, "",Double.toString(remark),"");
		for (int i = 0; i < allUserIdArrray.size(); i++) {
			WXUntil.sendTemplateMsg(WXUntil.getTemplateData(allUserIdArrray.get(i).toString(), map),access_token);
		}
	}

}