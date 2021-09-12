package cn.shu.wechat.pojo.dto.msg.send;

import cn.shu.wechat.enums.WXSendMsgCodeEnum;

/**
 * @作者 舒新胜
 * @项目 AutoWeChat
 * @创建时间 3/10/2021 2:49 PM
 * <p>
 * 图片消息
 */

public class WebWXSendingPicMsg extends WebWXSendingMsg {

    public WebWXSendingPicMsg() {
        super(WXSendMsgCodeEnum.PIC.getCode());
    }
}
