package cn.shu.wechat.beans.msg.url;

import lombok.Builder;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 8/12/2021 21:21
 */

public class WXImgUrl extends WXMsgUrl{
    public WXImgUrl(String type, String msgId) {
        super(type, msgId);
    }

    @Override
    public String getUrl() {
        String url = baseUrl;
        url+="&skey="+ skey;
        url+="&MsgId="+msgId;
        if (type != null){
            url+="&type="+ type;
        }
        return url;
    }
}
