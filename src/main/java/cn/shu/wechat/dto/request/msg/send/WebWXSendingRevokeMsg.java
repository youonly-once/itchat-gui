package cn.shu.wechat.dto.request.msg.send;


import cn.shu.wechat.dto.request.BaseRequest;

/**
 * @作者 舒新胜
 * @项目 AutoWeChat
 * @创建时间 3/10/2021 2:49 PM
 * <p>
 * 图片消息
 */

public class WebWXSendingRevokeMsg {

    public String ClientMsgId;

    public String SvrMsgId;

    public String ToUserName;

    public BaseRequest BaseRequest = new BaseRequest();

}
