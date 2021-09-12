package cn.shu.wechat.pojo.dto.msg.send;

import cn.shu.wechat.pojo.dto.msg.BaseRequest;

import java.util.Random;

/**
 * @作者 舒新胜
 * @项目 AutoWeChat
 * @创建时间 3/10/2021 2:49 PM
 * <p>
 * 图片消息
 */
public class WebWXSendingNotifyMsg {

    public int Code;
    public long ClientMsgId = Long.parseLong(System.currentTimeMillis() + String.valueOf(new Random().nextLong()).substring(1, 5));
    public String ToUserName;
    public String FromUserName;
    public BaseRequest BaseRequest = new BaseRequest();

}
