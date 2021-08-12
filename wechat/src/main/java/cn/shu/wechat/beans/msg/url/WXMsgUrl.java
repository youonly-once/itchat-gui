package cn.shu.wechat.beans.msg.url;

import cn.shu.wechat.core.Core;
import cn.shu.wechat.enums.URLEnum;
import lombok.Builder;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 8/12/2021 21:17
 */
public abstract class WXMsgUrl {
    public static final String SLAVE_TYPE ="slave";
    public static final String BIG_TYPE ="big";
    protected final String baseUrl = Core.getLoginInfoMap().get("url")+"/webwxgetmsgimg";
    protected String type;
    protected final String skey = (String) Core.getLoginInfoMap().get("skey");
    protected String msgId;

    public WXMsgUrl(String type, String msgId) {
        this.type = type;
        this.msgId = msgId;
    }

    public abstract String getUrl();
}
