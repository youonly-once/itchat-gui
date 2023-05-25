package cn.shu.wechat.enums;

/**
 * 系统通知类型
 *
 * @author ShuXinSheng
 * @version 1.1
 * @date 创建时间：2017年4月23日 下午12:15:00
 */
public enum WXReceiveMsgStatusNotifyCodeEnum {
    READED(1, "已读"),
    ENTER_SESSION(2, "进入会话"),
    INITED(3, "sync mobile chatList"),
    SYNC_CONV(4, "会话同步"),
    QUIT_SESSION(5, "退出会话"),
    DEFAULT(0,"未知")
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
