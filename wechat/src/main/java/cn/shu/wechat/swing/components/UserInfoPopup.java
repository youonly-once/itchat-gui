package cn.shu.wechat.swing.components;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.beans.pojo.Contacts;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.swing.panels.ChatPanel;
import cn.shu.wechat.swing.panels.ContactsPanel;
import cn.shu.wechat.swing.panels.RoomsPanel;
import cn.shu.wechat.swing.panels.TabOperationPanel;
import cn.shu.wechat.swing.utils.AvatarUtil;
import cn.shu.wechat.swing.utils.FontUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by song on 07/06/2017.
 */
public class UserInfoPopup extends JPopupMenu {
    private JPanel contentPanel;
    private JLabel avatarLabel;
    private JLabel usernameLabel;
    private JButton sendButton;
    private Contacts contacts;

    public UserInfoPopup(Contacts contacts) {
        this.contacts = contacts;
        initComponents();
        initView();
        setListener();

        // 更新对方头像
        updateAvatar();
    }

    private void updateAvatar() {
        ContactsPanel.getContext().getUserAvatar(contacts.getUsername(), true);
    }

    private void initComponents() {
        setBackground(Colors.WINDOW_BACKGROUND_LIGHT);


        contentPanel = new JPanel();
        contentPanel.setPreferredSize(new Dimension(200, 200));
        contentPanel.setBackground(Colors.WINDOW_BACKGROUND_LIGHT);

        avatarLabel = new JLabel();
        ImageIcon imageIcon = new ImageIcon();
        imageIcon.setImage(AvatarUtil.createOrLoadUserAvatar(contacts.getUsername()).getScaledInstance(60, 60, Image.SCALE_SMOOTH));
        avatarLabel.setIcon(imageIcon);

        usernameLabel = new JLabel();
        usernameLabel.setText(ContactsTools.getContactDisplayNameByUserName(contacts.getUsername()));

        /*sendButton = new RCButton("发消息");
        sendButton.setPreferredSize(new Dimension(180, 40));
        sendButton.setForeground(Colors.FONT_BLACK);*/
        sendButton = new RCButton("发消息", Colors.MAIN_COLOR, Colors.MAIN_COLOR_DARKER, Colors.MAIN_COLOR_DARKER);
        sendButton.setBackground(Colors.PROGRESS_BAR_START);
        sendButton.setPreferredSize(new Dimension(180, 35));
        sendButton.setFont(FontUtil.getDefaultFont(15));
    }

    private void initView() {
        add(contentPanel);

        contentPanel.setLayout(new GridBagLayout());

        JPanel avatarUsernamePanel = new JPanel();
        avatarUsernamePanel.setBackground(Colors.WINDOW_BACKGROUND_LIGHT);

        avatarUsernamePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        avatarUsernamePanel.add(avatarLabel);
        avatarUsernamePanel.add(usernameLabel);
        avatarUsernamePanel.setPreferredSize(new Dimension(180, 80));


        JPanel sendButtonPanel = new JPanel();
        sendButtonPanel.setBackground(Colors.WINDOW_BACKGROUND_LIGHT);
        sendButtonPanel.add(sendButton);

        contentPanel.add(avatarUsernamePanel, new GBC(0, 0).setWeight(1, 1));
        contentPanel.add(sendButtonPanel, new GBC(0, 1).setWeight(1, 1));

    }

    private void setListener() {
        sendButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openOrCreateDirectChat();
                super.mouseClicked(e);
            }
        });

        avatarLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setVisible(false);

                ImageIcon icon = new ImageIcon(AvatarUtil.createOrLoadUserAvatar(contacts.getUsername()));
                Image image = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                // ImageViewerFrame imageViewerFrame = new ImageViewerFrame(image);
                // imageViewerFrame.setVisible(true);
                // imageViewerFrame.toFront();
                super.mouseClicked(e);
            }
        });
    }

    private void openOrCreateDirectChat() {
        /*ContactsUser user  = contactsUserService.find("username", username).get(0);*/
        if (!Core.getRecentContacts().contains(contacts)) {
            // 房间bu存在，直接打开，否则发送请求创建房间
            createDirectChat(contacts);
        }
        ChatPanel.getContext().enterRoom(contacts.getUsername());
    }

    /**
     * 创建直接聊天
     *
     * @param contacts
     */
    private void createDirectChat(Contacts contacts) {
        // JOptionPane.showMessageDialog(MainFrame.getContext(), "发起聊天", "发起聊天", JOptionPane.INFORMATION_MESSAGE);
        RoomsPanel.getContext().addRoom(contacts, "", 0);
        Core.getRecentContacts().add(contacts);
        TabOperationPanel.getContext().switchToChatLabel();
    }

}
