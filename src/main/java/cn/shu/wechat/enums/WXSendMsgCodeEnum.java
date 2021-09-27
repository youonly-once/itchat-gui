package cn.shu.wechat.enums;

/**
 * 发送消息 类型
 * <p>
 * Created by xiaoxiaomo on 2017/5/6.
 */
public enum WXSendMsgCodeEnum {

    TEXT(1, "文本消息"),
    PIC(3, "图片消息"),
    VOICE(4, "语音消息"),
    EMOTION(47, "表情消息"),
    APP(6, "APP消息"),
    CARD(42, "APP消息"),
    MAP(48, "地图消息"),
    VIDEO(43, "视频消息");
    private final Integer code;
    private final String msg;

    WXSendMsgCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }


}
