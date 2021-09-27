package cn.shu.wechat.swing.utils;

import cn.shu.wechat.core.Core;
import cn.shu.wechat.pojo.entity.Message;
import cn.shu.wechat.swing.panels.RightPanel;
import cn.shu.wechat.swing.panels.RoomChatContainer;
import cn.shu.wechat.swing.panels.RoomsPanel;
import cn.shu.wechat.swing.panels.TabOperationPanel;

import javax.swing.*;
import java.util.List;

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
            RoomChatContainer.getContext().createAndShow(userId);
            //房间不存在，创建左侧聊天房
            createDirectChat(userId);
        } else {
            RoomChatContainer.getContext().createAndShow(userId);
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


    /**
     * 新消息处理
     *
     * @param message 消息
     * @param roomId  房间ID
     * @param lastMsg 最新消息
     * @param count   消息数
     */
    public static void addNewMsg(Message message, String roomId, String lastMsg, int count) {
        SwingUtilities.invokeLater(() -> {

            //刷新消息
            if (message != null) {
                message.setProgress(100);
                message.setIsSend(true);
                //新消息来了后创建房间
                //创建房间的时候会从数据库加载历史消息，由于这次的消息已经写入了数据库，所以不用再添加了

                  RoomChatContainer.getContext().addPanel(roomId);
                  RoomChatContainer.get(roomId).addMessageToEnd(message);
                }
            //新增或选择聊天列表
            RoomsPanel.getContext().addRoomOrOpenRoomNotSwitch(roomId, lastMsg, count);

        });
    }

    /**
     * 新消息处理
     *
     * @param message 消息
     * @param roomId  房间ID
     */
    public static void addNewMsg(Message message, String roomId) {
        addNewMsg(message, roomId, message.getPlaintext(), 0);
    }

    /**
     * 自己发送的新消息
     *
     * @param messages 消息
     */
    public static void addMineNewMsg(List<Message> messages) {
        if (messages == null){
            return;
        }
        for (Message message : messages) {
            addNewMsg(message, message.getToUsername(), message.getPlaintext(), 0);
        }
    }
}