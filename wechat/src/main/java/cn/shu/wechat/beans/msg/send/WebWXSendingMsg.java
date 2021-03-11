package cn.shu.wechat.beans.msg.send;

import cn.shu.wechat.core.Core;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;
import java.util.Random;

/**
 * @作者 舒新胜
 * @项目 AutoWeChat
 * @创建时间 3/10/2021 2:49 PM
 */

public class WebWXSendingMsg {
    public long ClientMsgId = Long.parseLong(new Date().getTime()+ String.valueOf(new Random().nextLong()).substring(1, 5));

    public int Type = 0;

    public long LocalID=  ClientMsgId;

    public String Content = "";

    public String FromUserName = Core.getUserName();

    public String ToUserName = "";

    public String MediaId = null;



    public WebWXSendingMsg() {

    }
    public WebWXSendingMsg(int type) {
        Type = type;
    }
}
