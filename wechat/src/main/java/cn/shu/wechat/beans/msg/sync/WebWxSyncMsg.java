/**
 * Copyright 2021 bejson.com
 */
package cn.shu.wechat.beans.msg.sync;

import lombok.Data;

import java.util.List;

/**
 * Auto-generated: 2021-02-22 13:35:59
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class WebWxSyncMsg {

    private BaseResponse BaseResponse;
    private int AddMsgCount;
    private List<AddMsgList> AddMsgList;
    private int ModContactCount;
    private List<ModContactList> ModContactList;
    private int DelContactCount;
    private List<DelContactList> DelContactList;
    private int ModChatRoomMemberCount;
    // private List<String> ModChatRoomMemberList;
    private Profile Profile;
    private int ContinueFlag;
    private SyncKey SyncKey;
    private String SKey;
    private SyncCheckKey SyncCheckKey;

}