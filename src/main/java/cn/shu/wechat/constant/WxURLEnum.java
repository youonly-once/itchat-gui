package cn.shu.wechat.constant;

/**
 * URL
 * Created by SXS on 2017/5/6.
 */
public enum WxURLEnum {


    cAPI_qrcode("https://wx2.qq.com/l/", "API_qrcode"),
    BASE_URL("https://login.weixin.qq.com/", "基本的URL"),
    UUID_URL("https://login.weixin.qq.com/" + "/jslogin", "获取UUID"),
    QRCODE_URL("https://login.weixin.qq.com/" + "/qrcode/", "获取二维码"),
    STATUS_NOTIFY_URL("%s/webwxstatusnotify?lang=zh_CN&pass_ticket=%s", "微信状态通知"),
    LOGIN_URL(BASE_URL.url + "/cgi-bin/mmwebwx-bin/login", "登陆URL"),
    INIT_URL("%s/webwxinit?r=%s&pass_ticket=%s&lang=zh_CN", "初始化URL"),
    SYNC_CHECK_URL("%s/synccheck", "检查心跳URL"),
    WEB_WX_SYNC_URL("%s/webwxsync?sid=%s&skey=%s", "web微信消息同步URL"),
    WEB_WX_CREATE_ROOM("/webwxcreatechatroom?r=%s", "创建群聊"),
    WEB_WX_GET_CONTACT("%s/webwxgetcontact", "web微信获取联系人信息URL"),
    WEB_WX_SEND_MSG("%s/webwxsendmsg", "发送消息URL"),
    WEB_WX_UPLOAD_MEDIA("%s/webwxuploadmedia?f=json", "上传文件到服务器"),
    WEB_WX_GET_MSG_IMG("%s/webwxgetmsgimg", "下载图片消息"),
    WEB_WX_GET_VOICE("%s/webwxgetvoice", "下载语音消息"),
    WEB_WX_GET_VIEDO("%s/webwxgetvideo", "下载视频消息"),
    WEB_WX_PUSH_LOGIN("%s/webwxpushloginurl", "不扫码登陆"),
    WEB_WX_LOGOUT("%s/webwxlogout", "退出微信"),
    WEB_WX_BATCH_GET_CONTACT("%s/webwxbatchgetcontact?type=ex&r=%s&lang=zh_CN&pass_ticket=%s", "查询群信息"),
    WEB_WX_REMARKNAME("%s/webwxoplog", "修改好友备注"),
    WEB_WX_VERIFYUSER("%s/webwxverifyuser?r=%s&lang=zh_CN&pass_ticket=%s", "被动添加好友"),
    WEB_WX_GET_MEDIA("%s/webwxgetmedia", "下载文件"),
    WEB_WX_GET_HEAD_IMAGE_THUM("https://wx2.qq.com/%s", "下载头像"),
    WEB_WX_GET_HEAD_IMAGE_BIG("https://wx2.qq.com/%s&type=big", "下载缩略图"),
    WEB_WX_REVOKE_MSG("%s/webwxrevokemsg?lang=zh_CN&pass_ticket=%s", "撤回消息"),
    WEB_WX_SEND_PIC_MSG("%s/webwxsendmsgimg?fun=async&f=json&pass_ticket=%s", "图片消息"),
    WEB_WX_SEND_VIDEO_MSG("%s/webwxsendvideomsg?fun=async&f=json&pass_ticket=%s", "图片消息"),
    WEB_WX_SEND_EMOTION_MSG("%s/webwxsendemoticon?fun=sys", "表情消息"),
    WEB_WX_SEND_APP_MSG("%s/webwxsendappmsg?fun=async&f=json&pass_ticket=%s", "APP消息"),
    WEB_WX_SEND_NOTIFY_MSG("%s/webwxstatusnotify?lang=zh_CN", "状态通知");

    private String url;
    private String msg;

    WxURLEnum(String url, String msg) {
        this.url = url;
        this.msg = msg;
    }


    public String getUrl() {
        return url;
    }

}
