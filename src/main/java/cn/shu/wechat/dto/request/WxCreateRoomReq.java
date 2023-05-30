package cn.shu.wechat.dto.request;

import cn.shu.wechat.dto.response.sync.MemberList;
import cn.shu.wechat.entity.Contacts;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class WxCreateRoomReq {

    @JSONField(name = "MemberCount")
    private Integer	MemberCount;

    @JSONField(name = "MemberList")
    private List<NewRoomMember> MemberList;

    @JSONField(name = "Topic")
    private String	Topic;

    @JSONField(name = "BaseRequest")
    private BaseRequest	BaseRequest;
    @Data
    @Builder
    public static class NewRoomMember{
        @JSONField(name = "UserName")
        String UserName;
    }
}
