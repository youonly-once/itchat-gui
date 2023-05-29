package cn.shu.wechat.dto.response;

import cn.shu.wechat.dto.response.sync.MemberList;
import cn.shu.wechat.entity.Contacts;
import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
public class WxCreateRoomResp {


    private BaseResponse	BaseResponse;

    private String	Topic;


    private String	PYInitial;


    private String	QuanPin;


    private Integer	MemberCount;


    private List<Contacts> MemberList;


    private String	ChatRoomName;


    private String	BlackList;
}
