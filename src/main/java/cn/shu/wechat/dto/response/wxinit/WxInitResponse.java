/**
  * Copyright 2023 bejson.com 
  */
package cn.shu.wechat.dto.response.wxinit;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.dto.response.BaseResponse;
import lombok.Data;

import java.util.List;

@Data
public class WxInitResponse {

    private BaseResponse BaseResponse;
    private int Count;
    private List<Contacts> ContactList;
    private cn.shu.wechat.dto.response.sync.SyncKey SyncKey;
    private Contacts User;
    private String ChatSet;
    private String SKey;
    private long ClientVersion;
    private long SystemTime;
    private int GrayScale;
    private int InviteStartCount;
    private int MPSubscribeMsgCount;
    private List<MPSubscribeMsgList> MPSubscribeMsgList;
    private long ClickReportInterval;

    @Data
    public static class MPSubscribeMsgList {

        private String UserName;
        private int MPArticleCount;
        private List<MPArticleList> MPArticleList;
        private long Time;
        private String NickName;
    }

    @Data
    public static class MPArticleList {

        private String Title;
        private String Digest;
        private String Cover;
        private String Url;

    }

}