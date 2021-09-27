package cn.shu.wechat.enums;

/**
 * 系统通知类型
 *
 * @author ShuXinSheng
 * @version 1.1
 * @date 创建时间：2017年4月23日 下午12:15:00
 */
public enum WXReceiveMsgStatusNotifyCodeEnum {

    DEFAULT(0, "默认"),
    READ(2, "已读通知"),
    NOTIFY_USER_NAME(4, "通知最近联系人列表"),

    ;

    private int code;
    private String desc;

    WXReceiveMsgStatusNotifyCodeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static WXReceiveMsgStatusNotifyCodeEnum getByCode(int code) {
        for (WXReceiveMsgStatusNotifyCodeEnum value : WXReceiveMsgStatusNotifyCodeEnum.values()) {
            if (value.code == code) {
                return value;
            }
        }
        return DEFAULT;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
