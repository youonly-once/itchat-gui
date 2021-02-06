package cn.shu.weichat.utils.enums;

/**
 * 消息类型枚举类
 *
 * @author ShuXinSheng
 * @date 创建时间：2017年5月13日 下午11:53:00
 * @version 1.1
 *
 */
public enum MsgTypeOfAppEnum {
	UNKNOWN(0, "未知消息"),
	FAVOURITE(5, "收藏消息"),
	FILE(6, "文件消息");
	private int type;
	private String code;

	MsgTypeOfAppEnum(int type, String code) {
		this.type = type;
		this.code = code;
	}
	public static MsgTypeOfAppEnum getByCode(int type){
		for (MsgTypeOfAppEnum value : MsgTypeOfAppEnum.values()) {
			if (value.type == type){
				return value;
			}
		}
		return UNKNOWN;
	}

	public int getType() {
		return type;
	}

	public String getCode() {
		return code;
	}

}
