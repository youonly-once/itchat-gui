package cn.shu.wechat.swing.panels.chat;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.constant.DownloadType;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.entity.Message;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.panels.TitlePanel;
import cn.shu.wechat.swing.panels.left.tabcontent.RoomsPanel;
import cn.shu.wechat.task.DownloadManager;
import cn.shu.wechat.task.DownloadTask;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Created by 舒新胜 on 17-5-29.
 */
@Getter
@Log4j2
public class ChatPanel extends JPanel {

    /**
     * 聊天房标题
     */
    private TitlePanel titlePanel;
    /**
     * 聊天房群成员
     */
    private ChatMembersPanel chatMembersPanel;

    /**
     * 聊天面板
     */
    private ChatMessagePanel chatMessagePanel;

    /**
     * 房间成员
     */
    private final String roomId;


    public ChatPanel(String roomId) {
        this.roomId = roomId;
        initComponents();
        initView();
        initData();
    }

    private void initData() {
        ChatPanel.this.getTitlePanel().showStatusLabel("加载中...");
        //消息发送者信息
        Contacts contacts = ContactsTools.getContactByUserName(roomId);
        if (contacts == null) {
            log.error("未知联系人：{}", roomId);
            return;
        }
        if (ContactsTools.isRoomContact(roomId)) {
            if (contacts.getMemberlist() == null || contacts.getMemberlist().isEmpty()) {
                ExecutorServiceUtil.getGlobalExecutorService().submit(() -> {
                        DownloadTask<Void> objectDownloadTask = new DownloadTask<>();
                        objectDownloadTask.setTaskId("WebWxBatchGetContact:" + roomId);
                        objectDownloadTask.setType(DownloadType.GetBatchContacts);
                        objectDownloadTask.setGroupName(roomId);
                        DownloadManager.submitAwait(objectDownloadTask);
                        SwingUtilities.invokeLater(() -> {
                            updateRoomTitle();
                            ChatPanel.this.getTitlePanel().hideStatusLabel();
                        });
                });
            }else{
                contacts = Core.getMemberMap().get(roomId);
                updateRoomTitle();
                ChatPanel.this.getTitlePanel().hideStatusLabel();
            }
        }else{
            contacts = Core.getMemberMap().get(roomId);
            updateRoomTitle();
            ChatPanel.this.getTitlePanel().hideStatusLabel();
        }

    }


    /**
     * 更新房间标题
     */
    private void updateRoomTitle() {
        Contacts contacts = Core.getMemberMap().get(roomId);
        String title = ContactsTools.getContactDisplayNameByUserName(contacts.getUsername());
        if (ContactsTools.isRoomContact(roomId)) {
            if (contacts.getMemberlist() == null) {
                title += " (loading)";
            } else {
                title += " (" + (contacts.getMemberlist().size()) + ")";
            }
        }
        // 更新房间标/题
        titlePanel.updateRoomTitle(title);
    }

    private void initComponents() {

        titlePanel = new TitlePanel(this);
        chatMessagePanel = new ChatMessagePanel(this, roomId);
        chatMembersPanel = new ChatMembersPanel(this, roomId);

        //setBorder(new LineBorder(Colors.SCROLL_BAR_TRACK_LIGHT));
    }

    private void initView() {

        this.setBackground(Colors.FONT_WHITE);
        this.setLayout(new BorderLayout());

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.add(titlePanel);
        northPanel.add(chatMembersPanel);

        add(northPanel, BorderLayout.NORTH);
        add(chatMessagePanel, BorderLayout.CENTER);

        // 默认隐藏成员面板
        chatMembersPanel.setVisible(false);
    }


    /**
     * 添加一条消息到最后
     *
     * @param message 新消息
     */
    public void addMessageToEnd(Message message) {
        if (!roomId.equals(ChatPanelContainer.getCurrRoomId())) {
            return;
        }
        chatMessagePanel.addMessageToEnd(message);
    }

    public void clearMsgItem() {
        chatMessagePanel.clearMsgItem();
    }
}
