package shu.cn.weichat.utils.enums;

/**
 * 消息类型枚举类
 *
 * @author ShuXinSheng
 * @date 创建时间：2017年5月13日 下午11:53:00
 * @version 1.1
 *
 */
public enum MsgTypeEnum {
	TEXT("Text", "文本消息"),
	PIC("Pic", "图片消息"),
	VOICE("Voice", "语音消息"),
	VIEDO("Viedo", "小视频消息"),
	NAMECARD("NameCard", "名片消息"),
	UNDO("Undo", "撤回消息"),
	ADDFRIEND("AddFriend", "添加好友消息"),
	EMOTION("Emotion", "表情消息"),
	APP("Emotion", "分享链接信息"),
	MEDIA("app", "文件消息"),
	SYSTEM("SYSTEM", "系统消息");//添加成功后会发该消息
	private String type;
	private String code;

	MsgTypeEnum(String type, String code) {
		this.type = type;
		this.code = code;
	}

	public String getType() {
		return type;
	}

	public String getCode() {
		return code;
	}

}
