package cn.shu.wechat.utils.enums;

/**
 * 返回结构枚举类
 * <p>
 * Created by xiaoxiaomo on 2017/5/6.
 */
public enum ReplyMsgTypeEnum {

    TEXT(1, "文本消息"),
    PIC(3, "图片消息"),
    VOICE(4, "语音消息"),
    APP(6, "APP消息"),
    VIDEO(5, "图片消息");
    private Integer code;
    private String msg;

    ReplyMsgTypeEnum(Integer code, String msg) {
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
