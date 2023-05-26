package cn.shu.wechat.dto.request;

import lombok.Data;

@Data
public class WxStatusNotifyReq {

    private BaseRequest BaseRequest;
    private int Code;
    private String FromUserName;
    private String ToUserName;
    private long ClientMsgId;
}
