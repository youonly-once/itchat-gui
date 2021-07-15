package cn.shu.wechat.utils;

import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.beans.msg.sync.AddMsgList;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.core.MsgCenter;

/**
 * @作者 舒新胜
 * @项目 AutoWeChat
 * @创建时间 4/19/2021 9:34 PM
 */
//@ControllerAdvice(basePackages = "cn.shu.wechat")
public class ExceptionHandler {

    // @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public void handler(Exception e) {
        System.out.println(Thread.currentThread().getName());
        AddMsgList msg = MsgCenter.threadLocalOfMsg.get();
        MsgCenter.threadLocalOfMsg.remove();
        String toUserName = msg.getFromUserName();
        if (msg.getFromUserName().equals(Core.getUserName())) {
            toUserName = msg.getToUserName();
        }

        MessageTools.sendMsgByUserId(
                MessageTools.Message.builder()
                        .content(e.getMessage())
                        .toUserName(toUserName)
                        .build(), toUserName);

    }
}
