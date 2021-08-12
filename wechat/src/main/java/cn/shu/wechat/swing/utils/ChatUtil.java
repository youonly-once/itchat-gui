package cn.shu.wechat.swing.utils;

import cn.shu.wechat.beans.pojo.Contacts;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.swing.panels.RightPanel;
import cn.shu.wechat.swing.panels.RoomChatPanel;
import cn.shu.wechat.swing.panels.RoomsPanel;
import cn.shu.wechat.swing.panels.TabOperationPanel;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 7/23/2021 20:01
 */
public class ChatUtil {
    /**
     * 打开或创建新聊天房
     */
    public static void openOrCreateDirectChat(String userId) {
        //注意先后顺序，否则可能不能激活
        if (!Core.getRecentContacts().contains(userId)) {
            //创建一层聊天面板
            RoomChatPanel.getContext().createAndShow(userId);
            //房间不存在，创建左侧聊天房
            createDirectChat(userId);
        }else{
            RoomChatPanel.getContext().createAndShow(userId);
            //房间列表激活 //TODO有问题
            RoomsPanel.getContext().activeItem(userId);
        }

        //右侧面板显示聊天框
        RightPanel.getContext().show(RightPanel.CHAT_ROOM);
        //控制选项卡切换
        TabOperationPanel.getContext().switchToChatLabel();
    }

    /**
     * 创建直接聊天
     *
     * @param userId
     */
    public static void createDirectChat(String userId) {
        RoomsPanel.getContext().addRoom(userId);
        Core.getRecentContacts().add(userId);
    }
}
