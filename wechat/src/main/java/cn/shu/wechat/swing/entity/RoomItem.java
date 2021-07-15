package cn.shu.wechat.swing.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息列表中显示的房间(聊天)条目
 *
 * @author song
 * @date 24/03/2017
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomItem implements Comparable<RoomItem> {
    /**
     * 房间id 对应微信用户的UserName以@开头或@@
     */
    private String roomId;

    /**
     * 房间名
     */
    private String name;

    /**
     * 最后一条消息
     */
    private String lastMessage;

    /**
     * 未读消息数量
     */
    private int unreadCount;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 是否为群聊
     */
    private boolean isGroup;


    @Override
    public int compareTo(RoomItem o) {
        // 注意，不能强制转int, 两个时间相差太远时有可能溢出
        // 忽略结果为0的情况，两个item必有先后，没有相同
        long ret = o.getTimestamp() - this.getTimestamp();
        return ret > 0 ? 1 : -1;
    }
}
