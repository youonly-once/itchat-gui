package cn.shu.wechat.constant;

import lombok.extern.log4j.Log4j2;

public interface WxRespConstant {
    /**
     * 检测是否扫描登录接口 返回状态码
     *
     * @author SXS
     */

    enum CheckLoginResultCodeEnum {

        SUCCESS(200, "登录成功"),

        WAIT_CONFIRM(201, "请在手机上点击确认登录"),

        CANCEL(202, "用户取消"),

        WAIT_SCAN(400, "请使用微信扫一扫以登录,刷新二维码"),

        NONE(408, "请使用微信扫一扫以登录");
        private final Integer code;
        private final String msg;

        CheckLoginResultCodeEnum(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }
        public static CheckLoginResultCodeEnum getByCode(Integer code){
            for (CheckLoginResultCodeEnum value : CheckLoginResultCodeEnum.values()) {
                if (value.code.equals(code)){
                    return value;
                }
            }
            throw new RuntimeException("未知类型："+code);
        }
        public Integer getCode() {
            return code;
        }
        public String getMsg() {
            return msg;
        }

    }

    /**
     * 消息检测接口返回Code码
     */
    @Log4j2
    enum SyncCheckRetCodeEnum {

        SUCCESS(0, "成功"),
        LOGIN_OUT(1102, "退出"),
        LOGIN_OTHERWHERE(1101, "其它地方登陆"),
        UNKOWN(9999, "未知"),
        TICKET_ERROR(-14, "ticket错误"),
        PARAM_ERROR(1, "传入参数错误"),
        NOT_LOGIN_WARN(1100, "未登录提示"),
        LOGIN_ENV_ERROR(1203, "当前登录环境异常，为了安全起见请不要在web端进行登录"),
        TOO_OFEN(1205, "操作频繁");;


        private final Integer code;
        private final String type;

        SyncCheckRetCodeEnum(Integer code, String type) {
            this.code = code;
            this.type = type;
        }
        public static SyncCheckRetCodeEnum getByCode(Integer code){
            for (SyncCheckRetCodeEnum value : SyncCheckRetCodeEnum.values()) {
                if (value.code.equals(code)){
                    return value;
                }
            }
            log.error("未知类型：{}",code);
            return UNKOWN;
        }
        public Integer getCode() {
            return code;
        }

        public String getType() {
            return type;
        }

    }

    /**
     * @作者 舒新胜
     * @项目 weixin
     * @创建时间 3/7/2021 10:36 PM
     */
    enum SyncCheckSelectorEnum {
        NORMAL("0", "正常"),
        NEW_MSG("2", "有新消息"),
        MOD_CONTACT("4", "删除新增好友"),
        A("3", "未知"),
        ADD_OR_DEL_CONTACT("6", "存在删除或者新增的好友信息"),
        ENTER_OR_LEAVE_CHAT("7", "进入或离开聊天界面"),
        DEFAULT("D", "");
        private String code;
        private String type;

        SyncCheckSelectorEnum(String code, String type) {
            this.code = code;
            this.type = type;
        }

        public static SyncCheckSelectorEnum getByCode(String code) {
            for (SyncCheckSelectorEnum value : SyncCheckSelectorEnum.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return SyncCheckSelectorEnum.DEFAULT;
        }

        public String getCode() {
            return code;
        }

        public String getType() {
            return type;
        }
    }

    /**
     * 收到的消息类型
     *
     * @author ShuXinSheng
     * @version 1.1
     * @date 创建时间：2017年4月23日 下午12:15:00
     */
    enum WXReceiveMsgCodeEnum {

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

    /**
     * APP消息的子类型
     *
     * @author ShuXinSheng
     * @version 1.1
     * @date 创建时间：2017年5月13日 下午11:53:00
     */
    enum WXReceiveMsgCodeOfAppEnum {
        OTHER(0, "未知消息"),
        LINK(5, "链接消息"),
        PROGRAM(33, "小程序消息"),
        MUSIC(3, "分享的音乐"),
        PICTURE(8, "搜狗输入法"),
        TRANSFER(2000, "转账"),
        FILE(6, "文件消息");
        private int type;
        private String code;

        WXReceiveMsgCodeOfAppEnum(int type, String code) {
            this.type = type;
            this.code = code;
        }

        public static WXReceiveMsgCodeOfAppEnum getByCode(int type) {
            for (WXReceiveMsgCodeOfAppEnum value : WXReceiveMsgCodeOfAppEnum.values()) {
                if (value.type == type) {
                    return value;
                }
            }
            return OTHER;
        }

        public int getType() {
            return type;
        }

        public String getCode() {
            return code;
        }

    }

    /**
     * 系统通知类型
     *
     * @author ShuXinSheng
     * @version 1.1
     * @date 创建时间：2017年4月23日 下午12:15:00
     */
    enum WXReceiveMsgStatusNotifyCodeEnum {
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
}
