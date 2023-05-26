package cn.shu.wechat.dto.request;

import cn.shu.wechat.dto.response.sync.SyncKey;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WxSyncReq {
    @JSONField(name = "BaseRequest")
    private BaseRequest BaseRequest;

    @JSONField(name = "SyncKey")
    private SyncKey SyncKey;

    @JSONField(name = "rr")
    private Long rr;
}
