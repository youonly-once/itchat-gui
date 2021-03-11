package cn.shu.wechat.beans.msg.send;

import cn.shu.wechat.core.Core;
import cn.shu.wechat.enums.WXSendMsgCodeEnum;

import java.util.Date;
import java.util.Random;

/**
 * @作者 舒新胜
 * @项目 AutoWeChat
 * @创建时间 3/10/2021 2:49 PM
 *
 * 文本消息
 */

public class WebWXSendingTextMsg extends WebWXSendingMsg {

    public WebWXSendingTextMsg() {
        super(WXSendMsgCodeEnum.TEXT.getCode());
    }
}
