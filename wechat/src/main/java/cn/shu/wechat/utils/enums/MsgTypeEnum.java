package cn.shu.wechat.utils.enums;

/**
 * 消息类型枚举类
 *
 * @author ShuXinSheng
 * @date 创建时间：2017年5月13日 下午11:53:00
 * @version 1.1
 *
 */
public enum MsgTypeEnum {
	UNKNOWN("UNKNOWN", "未知消息"),
	TEXT("TEXT", "文本消息"),
	PIC("PIC", "图片消息"),
	VOICE("VOICE", "语音消息"),
	VIDEO("VIDEO", "小视频消息"),
	NAMECARD("NAMECARD", "名片消息"),
	UNDO("UNDO", "撤回消息"),
	ADDFRIEND("ADDFRIEND", "添加好友消息"),
	EMOTION("EMOTION", "表情消息"),
	APP("APP", "APP消息"),
	FAVOURITEOFAPP("FAVOURITEOFAPP", "APP收藏消息消息"),
	PROGRAMOFAPP("PROGRAMOFAPP", "APP小程序消息"),
	MEDIA("MEDIA", "文件消息"),
	MAP("MAP", "地图消息"),
	SYSTEM("SYSTEM", "系统消息");//添加成功后会发该消息
	private String type;
	private String code;

	MsgTypeEnum(String type, String code) {
		this.type = type;
		this.code = code;
	}
	public static  MsgTypeEnum getByCode(String type){
		for (MsgTypeEnum value : MsgTypeEnum.values()) {
			if (value.type == type){
				return value;
			}
		}
		return UNKNOWN;
	}

	public String getType() {
		return type;
	}

	public String getCode() {
		return code;
	}

}
