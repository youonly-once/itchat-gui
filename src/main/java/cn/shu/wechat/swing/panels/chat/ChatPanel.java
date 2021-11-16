package cn.shu.wechat.swing.panels.chat;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.pojo.entity.Contacts;
import cn.shu.wechat.pojo.entity.Message;
import cn.shu.wechat.service.LoginService;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.panels.TitlePanel;
import cn.shu.wechat.utils.SpringContextHolder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;

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

    /**
     * 用户信息
     */
    private Contacts contacts;

    public ChatPanel(String roomId) {
        this.roomId = roomId;
        initComponents();
        initView();
        initData();
    }

    private void initData() {
        //消息发送者信息
        contacts = ContactsTools.getContactByUserName(roomId);
        if (contacts == null) {
            log.error("未知联系人：{}", roomId);
        }
        //加载群成员
        if (roomId.startsWith("@@")) {
            loadMemberList();
        } else {
            // 更新房间标题
            updateRoomTitle();
        }
    }

    /**
     * 加载群成员
     */
    private void loadMemberList() {

        //加载群成员
        if (contacts.getMemberlist() == null || contacts.getMemberlist().isEmpty()) {
            ChatPanel.this.getTitlePanel().showStatusLabel("加载中...");
            new SwingWorker<Object, Object>() {

                @Override
                protected Object doInBackground() throws Exception {

                    LoginService bean = SpringContextHolder.getBean(LoginService.class);
                    bean.WebWxBatchGetContact(roomId);
                    return null;
                }

                @Override
                protected void done() {
                    super.done();
                    contacts = Core.getMemberMap().get(roomId);
                    ArrayList<String> list = new ArrayList<>();
                    for (Contacts contacts1 : contacts.getMemberlist()) {
                        list.add(ContactsTools.getMemberDisplayNameOfGroup(roomId, contacts1.getUsername()));
                    }
                    chatMessagePanel.setRoomMembers(list);

                    updateRoomTitle();
                    ChatPanel.this.getTitlePanel().hideStatusLabel();
                }
            }.execute();
        } else {
            ArrayList<String> list = new ArrayList<>();
            for (Contacts contacts1 : contacts.getMemberlist()) {
                list.add(ContactsTools.getMemberDisplayNameOfGroup(roomId, contacts1.getUsername()));
            }
            chatMessagePanel.setRoomMembers(list);
            updateRoomTitle();
        }
    }

    /**
     * 更新房间标题
     */
    public void updateRoomTitle() {
        String title = ContactsTools.getContactDisplayNameByUserName(contacts.getUsername());
        if (roomId.startsWith("@@")) {
            if (contacts.getMemberlist() == null) {
                title += " (0)";
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

        setBorder(new LineBorder(Colors.SCROLL_BAR_TRACK_LIGHT));
    }

    private void initView() {

        this.setBackground(Colors.FONT_WHITE);
        this.setLayout(new BorderLayout());
        add(titlePanel, BorderLayout.NORTH);
        add(chatMembersPanel, BorderLayout.EAST);
        add(chatMessagePanel, BorderLayout.CENTER);
    }


    /**
     * 添加一条消息到最后
     *
     * @param message 新消息
     */
    public void addMessageToEnd(Message message) {
        chatMessagePanel.addMessageToEnd(message);
    }

}
