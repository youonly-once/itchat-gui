package weixin.utils;

import lombok.extern.log4j.Log4j2;

/**
* @author ShuXinSheng
* @version ����ʱ�䣺2019��1��26�� ����4:45:43
* ��˵��
*/
@Log4j2
public class WXConfig {


	/*	public final static String appId = "wxd931372d8ff734ea";
	public final static String secret = "6386843710f044caa82f55e027ae9bd6";*/
	public final static String APPID = "wx515561ada65f1c58";
	public final static String SECRET = "59652b163668f4fa2e9404a664d9f465";
	// ��������������е�tokenpackage weixin.utils;
	//
	///**
	//* @author ShuXinSheng
	//* @version 创建时间：2019年1月26日 下午4:45:43
	//* 类说明
	//*/
	//@Log4j2 public class WXConfig {
	//
	//
	//	/*	public final static String appId = "wxd931372d8ff734ea";
	//	public final static String secret = "6386843710f044caa82f55e027ae9bd6";*/
	//	public final static String APPID = "wx515561ada65f1c58";
	//	public final static String SECRET = "59652b163668f4fa2e9404a664d9f465";
	//	// 这里填基本配置中的token
	//	public final static String TOKEN = "cqgt_weixin";
	//
	//	public final static class Url{
	//
	//		// 获取基础支持的access_token
	//		public static final String getToken= "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential"
	//		+ "&appid="+ APPID
	//		+ "&secret=" + SECRET;
	//		// 获取临时素材(视频不能使用https协议)
	//		public static final String GET_TMP_MATERIAL = "https://api.weixin.qq.com/cgi-bin/media/get?access_token=%s&media_id=%s";
	//		// 获取临时素材(视频)
	//		public static final String GET_TMP_MATERIAL_VIDEO = "http://api.weixin.qq.com/cgi-bin/media/get?access_token=%s&media_id=%s";
	//		//获取分组用户
	//		public static final String getGroupUserIDUrl = "https://api.weixin.qq.com/cgi-bin/user/get?access_token=%s"
	//				+ "&next_openid=%s";
	//		public static final String getUserInfoUrl = "https://api.weixin.qq.com/cgi-bin/user/info/batchget?access_token=%s";
	//		//获取单个用户信息
	//		public static final String GET_SINGLE_USER_INFO = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=%s&openid=%s&lang=zh_CN";
	//		//获取素材
	//		public static final String GET_Material ="https://api.weixin.qq.com/cgi-bin/material/batchget_material?access_token=%s";
	//		//发送模板消息
	//		public static final String sendTemplateMsgUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s";
	//		//给用户添加组
	//		public static final String ADD_GROUP="https://api.weixin.qq.com/cgi-bin/tags/members/batchtagging?access_token=%s";
	//		//从组中移除用户
	//		public static final String REMOVE_GROUP="https://api.weixin.qq.com/cgi-bin/tags/members/batchuntagging?access_token=%s";
	//		//设置用户备注
	//		public static final String  SET_REMARK = "https://api.weixin.qq.com/cgi-bin/user/info/updateremark?access_token=%s";
	//
	//		//回复用户消息 XML
	//		public static final String replyMsg_XML = "<xml>" + "<ToUserName><![CDATA[%s]]></ToUserName>"// 回复用户时，这里是用户的openid；但用户发送过来消息这里是微信公众号的原始id
	//				+ "<FromUserName><![CDATA[%s]]></FromUserName>" // 这里填写微信公众号
	//		// 的原始id；用户发送过来时这里是用户的openid
	//				+ "<CreateTime>%s</CreateTime>"// 这里可以填创建信息的时间，目前测试随便填也可以
	//				+ "<MsgType><![CDATA[text]]></MsgType>"// 文本类型，text，可以不改
	//				+ "<Content><![CDATA[%s]]></Content>"// 文本内容，我喜欢你
	//		// + "<MsgId>1234567890123456</MsgId> "//消息id，随便填，但位数要够
	//				+ " </xml>";
	//
	//		public static final String UPLOAD_IMG = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token=%s&type=image";
	//	}
	//
	//
	//}
	public final static String TOKEN = "cqgt_weixin";
	
	public final static class Url{
		
		// ��ȡ����֧�ֵ�access_token
		public static final String getToken= "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential" 
		+ "&appid="+ APPID 
		+ "&secret=" + SECRET;
		// ��ȡ��ʱ�ز�(��Ƶ����ʹ��httpsЭ��)
		public static final String GET_TMP_MATERIAL = "https://api.weixin.qq.com/cgi-bin/media/get?access_token=%s&media_id=%s";
		// ��ȡ��ʱ�ز�(��Ƶ)
		public static final String GET_TMP_MATERIAL_VIDEO = "http://api.weixin.qq.com/cgi-bin/media/get?access_token=%s&media_id=%s";
		//��ȡ�����û�
		public static final String getGroupUserIDUrl = "https://api.weixin.qq.com/cgi-bin/user/get?access_token=%s"
				+ "&next_openid=%s";
		public static final String getUserInfoUrl = "https://api.weixin.qq.com/cgi-bin/user/info/batchget?access_token=%s";
		//��ȡ�����û���Ϣ
		public static final String GET_SINGLE_USER_INFO = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=%s&openid=%s&lang=zh_CN";
		//��ȡ�ز�
		public static final String GET_Material ="https://api.weixin.qq.com/cgi-bin/material/batchget_material?access_token=%s";
		//����ģ����Ϣ
		public static final String sendTemplateMsgUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s";
		//���û������
		public static final String ADD_GROUP="https://api.weixin.qq.com/cgi-bin/tags/members/batchtagging?access_token=%s";
		//�������Ƴ��û�
		public static final String REMOVE_GROUP="https://api.weixin.qq.com/cgi-bin/tags/members/batchuntagging?access_token=%s";
		//�����û���ע
		public static final String  SET_REMARK = "https://api.weixin.qq.com/cgi-bin/user/info/updateremark?access_token=%s";
		
		//�ظ��û���Ϣ XML
		public static final String replyMsg_XML = "<xml>" + "<ToUserName><![CDATA[%s]]></ToUserName>"// �ظ��û�ʱ���������û���openid�����û����͹�����Ϣ������΢�Ź��ںŵ�ԭʼid
				+ "<FromUserName><![CDATA[%s]]></FromUserName>" // ������д΢�Ź��ں�
		// ��ԭʼid���û����͹���ʱ�������û���openid
				+ "<CreateTime>%s</CreateTime>"// ������������Ϣ��ʱ�䣬Ŀǰ���������Ҳ����
				+ "<MsgType><![CDATA[text]]></MsgType>"// �ı����ͣ�text�����Բ���
				+ "<Content><![CDATA[%s]]></Content>"// �ı����ݣ���ϲ����
		// + "<MsgId>1234567890123456</MsgId> "//��Ϣid��������λ��Ҫ��
				+ " </xml>";
		
		public static final String UPLOAD_IMG = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token=%s&type=image";
	}


}
