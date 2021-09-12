package cn.shu.wechat.swing.panels;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.pojo.entity.Contacts;
import cn.shu.wechat.pojo.entity.Message;
import cn.shu.wechat.service.LoginService;
import cn.shu.wechat.swing.components.Colors;
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
public class RoomChatPanelCard extends JPanel {

    /**
     * 聊天房标题
     */
    private TitlePanel titlePanel;
    /**
     * 聊天房群成员
     */
    private RoomMembersPanel roomMembersPanel;

    /**
     * 聊天面板
     */
    private ChatPanel chatPanel;

    /**
     * 提示面板
     */
    private TipPanel tipPanel;


    /**
     *
     */
    private JPanel contentPanel;

    private CardLayout cardLayout;

    public static final String MESSAGE = "MESSAGE";
    public static final String TIP = "TIP";
    public static final String USER_INFO = "USER_INFO";
    /**
     * 房间成员
     */
    private final String roomId;

    /**
     * 用户信息
     */
    private Contacts contacts;

    public RoomChatPanelCard(String roomId) {
        this.roomId = roomId;
        initComponents();
        initView();
        initData();
    }

    private void initData() {
        //消息发送者信息
        contacts = Core.getMemberMap().get(roomId);
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
            RoomChatPanelCard.this.getTitlePanel().showStatusLabel("加载中...");
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
                    chatPanel.setRoomMembers(list);

                    updateRoomTitle();
                    RoomChatPanelCard.this.getTitlePanel().hideStatusLabel();
                }
            }.execute();
        } else {
            ArrayList<String> list = new ArrayList<>();
            for (Contacts contacts1 : contacts.getMemberlist()) {
                list.add(ContactsTools.getMemberDisplayNameOfGroup(roomId, contacts1.getUsername()));
            }
            chatPanel.setRoomMembers(list);
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
        cardLayout = new CardLayout();
        contentPanel = new JPanel();
        contentPanel.setLayout(cardLayout);

        titlePanel = new TitlePanel(this);
        chatPanel = new ChatPanel(this, roomId);
        roomMembersPanel = new RoomMembersPanel(this, roomId);
        tipPanel = new TipPanel(this);


        setBorder(new LineBorder(Colors.SCROLL_BAR_TRACK_LIGHT));
    }

    private void initView() {
        contentPanel.add(tipPanel, TIP);
        contentPanel.add(chatPanel, MESSAGE);

        this.setBackground(Colors.FONT_WHITE);
        this.setLayout(new BorderLayout());
        add(titlePanel, BorderLayout.NORTH);
        add(roomMembersPanel, BorderLayout.EAST);
        add(contentPanel, BorderLayout.CENTER);
        showPanel(MESSAGE);
    }

    /**
     * 显示提示或者是消息
     * @param who
     */
    public void showPanel(String who) {
        cardLayout.show(contentPanel, who);
    }


    /**
     * 添加一条消息到最后
     *
     * @param message 新消息
     */
    public void addMessageToEnd(Message message) {
        chatPanel.addMessageToEnd(message);
    }

}
