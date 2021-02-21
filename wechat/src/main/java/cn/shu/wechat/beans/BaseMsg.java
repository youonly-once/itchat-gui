package cn.shu.wechat.beans;

import lombok.Data;

import java.io.Serializable;

/**
 * 收到的微信消息
 * 
 * @author SXS
 * @date 创建时间：2017年7月3日 下午10:28:06
 * @version 1.1
 *
 */
@Data
public class BaseMsg implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int subMsgType;
	private int voiceLength;
	private String fileName;
	private int imgHeight;
	private String toUserName;
	private int hasProductId;
	private int imgStatus;
	private String url;
	private int imgWidth;
	private int forwardFlag;
	private int status;
	private String Ticket;
	/** 推荐消息报文 **/
	private RecommendInfo recommendInfo;
	private long createTime;
	private String newMsgId;
	/** 文本消息内容 **/
	private String text;
	/** 消息类型 **/
	private int msgType;


	/** 是否为群消息 **/
	//@自己的人，如果有
	private String mentionMeUserNickName;
	private boolean mentionMe;
	private Boolean groupMsg;
	private String msgId;
	private int statusNotifyCode;
	private AppInfo appInfo;
	private int appMsgType;
	private String Type;
	private int playLength;
	private String mediaId;
	private String content;
	private String memberName;
	private String statusNotifyUserName;
	/** 消息发送者ID **/
	private String fromUserName;
	private String oriContent;
	private String fileSize;

}
