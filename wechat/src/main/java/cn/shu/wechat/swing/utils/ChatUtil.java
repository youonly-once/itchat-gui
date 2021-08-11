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
        if (!Core.getRecentContacts().contains(userId)) {
            // 房间bu存在，直接打开，否则发送请求创建房间
            createDirectChat(userId);
            RoomsPanel.getContext().activeItem(0);
        }else{
            //房间列表激活
            RoomsPanel.getContext().activeItem(userId);
        }
        //右侧面板显示聊天框
        RightPanel.getContext().show(RightPanel.CHAT_ROOM);
        //创建一层Card
        RoomChatPanel.getContext().addPanel(userId);
        RoomChatPanel.getContext().show(userId);

        //控制选项卡切换
        TabOperationPanel.getContext().switchToChatLabel();
        RoomsPanel.getContext().scrollPoint(1);
        //ChatPanel.getContext().enterRoom(user.getUsername());
    }

    /**
     * 创建直接聊天
     *
     * @param userId
     */
    public static void createDirectChat(String userId) {
        // JOptionPane.showMessageDialog(MainFrame.getContext(), "发起聊天", "发起聊天", JOptionPane.INFORMATION_MESSAGE);
        RoomsPanel.getContext().addRoom(userId, "", 0);
        Core.getRecentContacts().add(userId);

    }
}
