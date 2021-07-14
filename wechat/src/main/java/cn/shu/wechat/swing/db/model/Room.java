package cn.shu.wechat.swing.db.model;


import cn.shu.wechat.beans.pojo.Contacts;
import cn.shu.wechat.swing.RoomTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * Created by song on 10/03/2017.
 */

/**
 * 房间信息
 */
@Data
public class Room extends BasicModel implements Comparable<Room> {
    private String roomId;
    /**
     * 房间类型  g群  p是个人
     */
    private RoomTypeEnum type;
    private String name;
    private String topic;
    private String muted;
    private List<Contacts> memberList;
    private boolean sysMes;
    private int msgSum;
    private long lastChatAt;
    private String creatorName;
    private String creatorId;
    private String jitsiTimeout;
    private boolean readOnly;
    private boolean archived;
    private boolean defaultRoom;
    private String createdAt;
    private String updatedAt;
    private int unreadCount;
    private int totalReadCount;
    private String lastMessage;


    @Override
    public int compareTo(Room o) {
        if (this.getType().equals(o.getType())) {
            return (int) (this.getLastChatAt() - o.getLastChatAt());
        } else {
            return this.getType().compareTo(o.getType());
        }
    }
}


