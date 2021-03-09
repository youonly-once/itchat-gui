package cn.shu.wechat.enums;

/**
 * 收到的消息类型
 *
 * @author ShuXinSheng
 * @version 1.1
 * @date 创建时间：2017年4月23日 下午12:15:00
 */
public enum WXReceiveMsgCodeEnum {

    UNKNOWN(0, "未知消息类型"),
    MSGTYPE_TEXT(1, "文本消息类型"),
    MSGTYPE_IMAGE(3, "图片消息"),
    MSGTYPE_VOICE(34, "语音消息"),
    MSGTYPE_VIDEO(43, "小视频消息"),
    MSGTYPE_MICROVIDEO(62, "短视频消息"),
    MSGTYPE_EMOTICON(47, "表情消息"),
    MSGTYPE_APP(49, "APP消息"),
    MSGTYPE_VOIPMSG(50, ""),
    MSGTYPE_VOIPNOTIFY(52, ""),
    MSGTYPE_VOIPINVITE(53, ""),
    MSGTYPE_LOCATION(48, ""),
    MSGTYPE_STATUSNOTIFY(51, "系统通知"),
    MSGTYPE_SYSNOTICE(9999, ""),
    MSGTYPE_POSSIBLEFRIEND_MSG(40, ""),
    MSGTYPE_VERIFYMSG(37, "好友请求消息"),
    MSGTYPE_SHARECARD(42, "名片分享消息"),
    MSGTYPE_SYS(10000, "系统消息"),
    MSGTYPE_RECALLED(10002, "撤回消息"),
    /**
     * 自定义的几种类型
     */

    MSGTYPE_MAP(8888, "地图消息"),
    ;

    private int code;
    private String desc;

    WXReceiveMsgCodeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static WXReceiveMsgCodeEnum getByCode(int code) {
        for (WXReceiveMsgCodeEnum value : WXReceiveMsgCodeEnum.values()) {
            if (value.code == code) {
                return value;
            }
        }
        return UNKNOWN;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
