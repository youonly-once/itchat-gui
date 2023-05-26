package cn.shu.wechat.core;

import cn.shu.wechat.dto.response.sync.SyncCheckKey;
import cn.shu.wechat.dto.response.sync.SyncKey;
import cn.shu.wechat.dto.request.BaseRequest;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.util.stream.Collectors;

@Getter
@Setter
public class LoginResultData{
        private String url;
        private String fileUrl;
        private String syncUrl;
        private String deviceId;
        private Integer inviteStartCount;
        private SyncKey syncKeyObject;
        private String syncKey;
        private SyncCheckKey syncCheckKey;
        @JSONField(name ="pass_ticket")
        private String passTicket;
        @JSONField(name ="BaseRequest")
        private BaseRequest baseRequest;



    public void setSyncKeyObject(SyncKey syncKeyObject) {
        this.syncKey = syncKeyObject.getList()
                .stream()
                .map(e -> e.getKey() + "_" + e.getVal()).collect(Collectors.joining("|"));
        this.syncKeyObject = syncKeyObject;
    }
}