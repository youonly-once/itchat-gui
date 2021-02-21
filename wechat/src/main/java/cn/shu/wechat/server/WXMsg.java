package cn.shu.wechat.server;

import lombok.extern.log4j.Log4j2;

/**
 * @author ShuXinSheng
 * @version 创建时间：2020年3月24日 下午5:37:58
 * 类说明
 */
@Log4j2
public class WXMsg {
	/**
	 * @return the toUserName
	 */
	public String getToUserName() {
		return ToUserName;
	}
	/**
	 * @param toUserName the toUserName to set
	 */
	public void setToUserName(String toUserName) {
		ToUserName = toUserName;
	}
	/**
	 * @return the fromUserName
	 */
	public String getFromUserName() {
		return FromUserName;
	}
	/**
	 * @param fromUserName the fromUserName to set
	 */
	public void setFromUserName(String fromUserName) {
		FromUserName = fromUserName;
	}
	/**
	 * @return the createTime
	 */
	public String getCreateTime() {
		return CreateTime;
	}
	/**
	 * @param createTime the createTime to set
	 */
	public void setCreateTime(String createTime) {
		CreateTime = createTime;
	}
	/**
	 * @return the msgType
	 */
	public String getMsgType() {
		return MsgType;
	}
	/**
	 * @param msgType the msgType to set
	 */
	public void setMsgType(String msgType) {
		MsgType = msgType;
	}
	/**
	 * @return the content
	 */
	public String getContent() {
		return Content;
	}
	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		Content = content;
	}
	/**
	 * @return the msgId
	 */
	public String getMsgId() {
		return MsgId;
	}
	/**
	 * @param msgId the msgId to set
	 */
	public void setMsgId(String msgId) {
		MsgId = msgId;
	}
	/**
	 * <xml>
	 <ToUserName><![CDATA[toUser]]></ToUserName>
	 <FromUserName><![CDATA[fromUser]]></FromUserName>
	 <CreateTime>1348831860</CreateTime>
	 <MsgType><![CDATA[text]]></MsgType>
	 <Content><![CDATA[this is a test]]></Content>
	 <MsgId>1234567890123456</MsgId>
	 </xml>
	 */
	private String ToUserName;
	private String FromUserName;
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WXMsg [ToUserName=" + ToUserName + ", FromUserName=" + FromUserName + ", CreateTime=" + CreateTime
				+ ", MsgType=" + MsgType + ", Content=" + Content + ", MsgId=" + MsgId + "]";
	}
	private String CreateTime;
	private String MsgType;
	private String Content;
	private String MsgId;

}
