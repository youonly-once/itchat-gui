package cn.shu.wechat.utils;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.entity.Message;
import cn.shu.wechat.swing.panels.RightPanel;
import cn.shu.wechat.swing.panels.chat.ChatPanelContainer;
import cn.shu.wechat.swing.panels.left.TabOperationPanel;
import cn.shu.wechat.swing.panels.left.tabcontent.RoomsPanel;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 7/23/2021 20:01
 */
public final class ChatUtil {
    private ChatUtil() {

    }

    /**
     * 打开或创建新聊天房
     */
    public static void openOrCreateDirectChat(String userId) {
        //注意先后顺序，否则可能不能激活
        if (!Core.getRecentContacts().contains(userId)) {
            //创建一层聊天面板
            ChatPanelContainer.getContext().createAndShow(userId);
            //房间不存在，创建左侧聊天房

            createDirectChat(userId);

        } else {
            ChatPanelContainer.getContext().createAndShow(userId);
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
        RoomsPanel.getContext().addRoomFirst(userId);
        Core.getRecentContacts().add(userId);
    }

    public static void main(String[] args) {
        System.out.println(LocalDateTime.now());
    }
    /**
     * 新消息处理
     *
     * @param message 消息
     * @param roomId  房间ID
     * @param lastMsg 最新消息
     * @param count   消息数
     */
    public static void addNewMsg(Message message, String roomId, String lastMsg, int count,Boolean isMute,Boolean hasNewMsg) {
        SwingUtilities.invokeLater(() -> {

            //刷新消息
            if (message != null) {
                message.setProgress(100);
                //新消息来了后创建房间
                //创建房间的时候会从数据库加载历史消息，由于这次的消息已经写入了数据库，所以不用再添加了

                //ChatPanelContainer.getContext().addPanel(roomId);
                if (ChatPanelContainer.getContext().isCurrentRoom(roomId)) {
                    ChatPanelContainer.get(roomId).addMessageToEnd(message);
                }
            }
            //新增或选择聊天列表
            RoomsPanel.getContext().addRoomOrUpdateRoom(roomId, lastMsg, count, isMute, hasNewMsg, isAtMe(roomId, lastMsg));

            RoomsPanel.getContext().updateUnreadTotalCount();
        });
    }

    public static Boolean isAtMe(String roomId, String lastMsg) {
        //如果是 群 判断是否有人@我
        //Contacts contacts = roomItem.getContacts();
        AtomicBoolean isAtMe = new AtomicBoolean(false);
        Contacts contacts = Core.getMemberMap().get(roomId);
        if (contacts != null && ContactsTools.isRoomContact(contacts)) {
            if ((StringUtils.isNotEmpty(Core.getUserSelf().getRemarkname()) && lastMsg.contains("@" + Core.getUserSelf().getRemarkname()))
                    || lastMsg.contains("@所有人")
                    || (StringUtils.isNotEmpty(Core.getNickName()) && lastMsg.contains("@" + Core.getNickName()))) {
                return true;
            } else if (contacts.getMemberlist() != null) {
                contacts.getMemberlist().stream().filter(e -> e.getUsername().equals(Core.getUserName())).findAny().ifPresent(e -> {
                    if (StringUtils.isNotEmpty(e.getDisplayname()) && lastMsg.contains("@" + e.getDisplayname())) {
                        isAtMe.set(true);
                    }
                });
            }
        }
        return isAtMe.get();
    }

    /**
     * 新消息处理
     *
     * @param message 消息
     * @param roomId  房间ID
     */
    public static void addNewMsg(Message message, String roomId) {
        addNewMsg(message, roomId, message.getPlaintext(), 0,null,false);
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
            addNewMsg(message, message.getToUsername(), message.getPlaintext(), 0, null,false);
        }
    }

    /**
     * 删除消息
     * @param item 消息
     */
    public static void deleteMessage(Message item){
        String messageId = item.getId();
        if (messageId != null && !messageId.isEmpty()) {
            String roomId = item.getFromUsername().equals(Core.getUserName()) ? item.getToUsername() : item.getFromUsername();
            ChatPanelContainer.get(roomId).getChatMessagePanel().deleteMessage(messageId);
        }
    }
}
