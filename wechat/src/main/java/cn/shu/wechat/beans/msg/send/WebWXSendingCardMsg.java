package cn.shu.wechat.beans.msg.send;

import cn.shu.wechat.enums.WXSendMsgCodeEnum;

/**
 * @作者 舒新胜
 * @项目 AutoWeChat
 * @创建时间 3/10/2021 2:49 PM
 *
 * 图片消息
 */

public class WebWXSendingCardMsg extends WebWXSendingMsg {

    public WebWXSendingCardMsg() {
        super( WXSendMsgCodeEnum.CARD.getCode());
    }
}
