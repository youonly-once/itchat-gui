package cn.shu.wechat.constant;

public interface WxReqParamsConstant {

    /**
     * 基本请求参数
     * 1. webWxInit      初始化
     * 2. wxStatusNotify 微信状态通知
     *
     * <p>
     * Created by xiaoxiaomo on 2017/5/7.
     */
    enum BaseParaEnum {

        Uin("Uin", "wxuin"),
        Sid("Sid", "wxsid"),
        Skey("Skey", "skey"),
        DeviceID("DeviceID", "DeviceID");

        private final String para;
        private final String value;

        BaseParaEnum(String para, String value) {
            this.para = para;
            this.value = value;
        }

        public String para() {
            return para;
        }


        public Object value() {
            return value;
        }

    }

    enum LoginParaEnum {

        LOGIN_ICON("loginicon", "true"),
        UUID("uuid", ""),
        TIP("tip", "1"),
        R("r", ""),
        _("_", "");

        private final String para;
        private final String value;

        LoginParaEnum(String para, String value) {
            this.para = para;
            this.value = value;
        }

        public String para() {
            return para;
        }

        public String value() {
            return value;
        }
    }

    /**
     * 状态通知
     * <p>
     * Created by xiaoxiaomo on 2017/5/7.
     */
    enum StatusNotifyParaEnum {

        CODE("Code", "3"),
        FROM_USERNAME("FromUserName", ""),
        TO_USERNAME("ToUserName", ""),
        CLIENT_MSG_ID("ClientMsgId", ""); //时间戳

        private String para;
        private String value;

        StatusNotifyParaEnum(String para, String value) {
            this.para = para;
            this.value = value;
        }

        public String para() {
            return para;
        }

        public String value() {
            return value;
        }
    }

    enum UUIDParaEnum {

        APP_ID("appid", "wx782c26e4c19acffb"),
        FUN("fun", "new"),
        LANG("lang", "zh_CN"),
        _("_", "时间戳");

        private final String para;
        private final String value;

        UUIDParaEnum(String para, String value) {
            this.para = para;
            this.value = value;
        }

        public String para() {
            return para;
        }

        public String value() {
            return value;
        }
    }

    enum SyncCheckParaEnum {
        R("r", ""),
        S_KEY("skey", ""),
        SID("sid", ""),
        UIN("uin", ""),
        DEVICE_ID("deviceid", ""),
        SYNC_KEY("synckey", ""),
        _("_", ""),
        ;
        private final String para;
        private final String value;

        SyncCheckParaEnum(String para, String value) {
            this.para = para;
            this.value = value;
        }

        public String para() {
            return para;
        }

        public String value() {
            return value;
        }
    }

    /**
     * 确认添加好友Enum
     *
     * @author SXS
     * @version 1.1
     * @date 创建时间：2017年6月29日 下午9:47:14
     */
    enum VerifyFriendEnum {

        ADD(2, "添加"),
        ACCEPT(3, "接受");

        private int code;
        private String desc;

        private VerifyFriendEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

    }

    /**
     * 发送消息 类型
     * <p>
     * Created by xiaoxiaomo on 2017/5/6.
     */
    enum WXSendMsgCodeEnum {

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
        public static WXSendMsgCodeEnum getByCode(Integer code){
            for (WXSendMsgCodeEnum value : WXSendMsgCodeEnum.values()) {
                if (value.code.equals(code)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("非法参数："+code);
        }

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }


    }
}
