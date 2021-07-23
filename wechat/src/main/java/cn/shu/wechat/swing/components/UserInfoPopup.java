package cn.shu.wechat.swing.components;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.beans.pojo.Contacts;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.swing.ImageViewer.ImageViewerFrame;
import cn.shu.wechat.swing.panels.ChatPanel;
import cn.shu.wechat.swing.panels.ContactsPanel;
import cn.shu.wechat.swing.panels.RoomsPanel;
import cn.shu.wechat.swing.panels.TabOperationPanel;
import cn.shu.wechat.swing.utils.AvatarUtil;
import cn.shu.wechat.swing.utils.FontUtil;
import cn.shu.wechat.swing.utils.IconUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

/**
 * Created by 舒新胜 on 07/06/2017.
 */
public class UserInfoPopup extends JPopupMenu {
    private JPanel contentPanel;
    private JLabel avatarLabel;
    private JLabel usernameLabel;
    private JButton sendButton;
    private Contacts contacts;
    private JLabel areaLabel;
    private JLabel genderLabel;
    private JLabel remarkNameLabel;
    private JLabel signatureLabel;
    private JLabel chat;
    public UserInfoPopup(Contacts contacts) {
        this.contacts = contacts;
        initComponents();
        initView();
        setListener();

        // 更新对方头像
        //updateAvatar();
    }

    private void updateAvatar() {
        ContactsPanel.getContext().getUserAvatar(contacts.getUsername(), true);
    }

    private void initComponents() {
        setBackground(Colors.WINDOW_BACKGROUND_LIGHT);


        contentPanel = new JPanel();
        contentPanel.setPreferredSize(new Dimension(220, 350));
        contentPanel.setBackground(Colors.WINDOW_BACKGROUND_LIGHT);

        avatarLabel = new JLabel();

        usernameLabel = new JLabel();
        areaLabel =new JLabel("地区："+contacts.getProvince()+" "+contacts.getCity()+" "+contacts.getAlias());
        genderLabel =new JLabel();
        if (contacts.getSex() !=null){
            genderLabel.setIcon(IconUtil.getIcon(this,contacts.getSex() == 1?"/image/man.png":"/image/woman.png"));
        }
        remarkNameLabel =new JLabel("备注："+contacts.getRemarkname());
        signatureLabel = new JLabel("签名："+contacts.getSignature());
        ImageIcon imageIcon = new ImageIcon();
        if (contacts.getGroupName()!=null&&contacts.getGroupName().startsWith("@@")){
            usernameLabel.setText(ContactsTools.getMemberNickNameOfGroup(contacts.getGroupName(),contacts.getUsername()));
        }else{
            usernameLabel.setText(ContactsTools.getContactNickNameByUserName(contacts.getUsername()));
        }
        //异步加载头像
        new SwingWorker<Object,Object>() {
            Image orLoadBigAvatar ;
            @Override
            protected Object doInBackground() throws Exception {
                orLoadBigAvatar = AvatarUtil.createOrLoadBigAvatar(contacts.getUsername(), contacts.getHeadimgurl());
                return null;
            }
            @Override
            protected void done() {
                if (orLoadBigAvatar!=null){
                    imageIcon.setImage(orLoadBigAvatar.getScaledInstance(220,220,Image.SCALE_SMOOTH));
                    avatarLabel.setIcon(imageIcon);
                }

            }
        }.execute();


        /*sendButton = new RCButton("发消息");
        sendButton.setPreferredSize(new Dimension(180, 40));
        sendButton.setForeground(Colors.FONT_BLACK);*/
        sendButton = new RCButton("发消息", Colors.MAIN_COLOR, Colors.MAIN_COLOR_DARKER, Colors.MAIN_COLOR_DARKER);
        sendButton.setBackground(Colors.PROGRESS_BAR_START);
        sendButton.setPreferredSize(new Dimension(180, 35));
        sendButton.setFont(FontUtil.getDefaultFont(15));
        chat = new JLabel();
    }

    private void initView() {
        add(contentPanel);

        contentPanel.setLayout(new GridBagLayout());

        //头像
        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        avatarPanel.add(avatarLabel);
        avatarPanel.setPreferredSize(new Dimension(220, 200));
        //个人信息
        JPanel infoPanel = new JPanel(new GridLayout(4,1,0,0));
        infoPanel.setPreferredSize(new Dimension(220, 114));
        infoPanel.setBackground(Color.white);
        JPanel nickNameArea = new JPanel(new BorderLayout(0,0));
      //  Border emptyBorder = BorderFactory.createEmptyBorder(20,20,10,20);
        //nickNameArea.setBorder(emptyBorder);
        nickNameArea.setBackground(Color.white);
        nickNameArea.add(usernameLabel,BorderLayout.WEST);
        nickNameArea.add(genderLabel,BorderLayout.CENTER);

        ImageIcon icon = IconUtil.getIcon(this, "/image/chat.png", 20, 20);
        chat.setIcon(icon);
        nickNameArea.add(chat,BorderLayout.EAST);
        // signatureLabel.setBorder(emptyBorder);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
        usernameLabel.setFont(FontUtil.getDefaultFont(16));
        infoPanel.add(nickNameArea);
        infoPanel.add(signatureLabel);
        signatureLabel.setForeground(Color.GRAY);
        signatureLabel.setFont(FontUtil.getDefaultFont(12));
        infoPanel.add(remarkNameLabel);
        remarkNameLabel.setForeground(Color.GRAY);
        remarkNameLabel.setFont(FontUtil.getDefaultFont(12));
        infoPanel.add(areaLabel);
        areaLabel.setForeground(Color.GRAY);
        areaLabel.setFont(FontUtil.getDefaultFont(12));


        contentPanel.add(avatarPanel, new GBC(0, 0).setWeight(1, 1));
        contentPanel.add(infoPanel, new GBC(0, 1).setWeight(1, 1));

    }

    private void setListener() {
        chat.addMouseListener(new MouseAdapter() {
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

                ImageIcon icon = new ImageIcon(Objects.requireNonNull(AvatarUtil.createOrLoadBigAvatar(contacts.getUsername(), contacts.getHeadimgurl())));
                Image image = icon.getImage();
                 ImageViewerFrame imageViewerFrame = new ImageViewerFrame(image);
                 imageViewerFrame.setVisible(true);
                 imageViewerFrame.toFront();
                super.mouseClicked(e);
            }
        });
    }

    private void openOrCreateDirectChat() {
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
        RoomsPanel.getContext().addRoom(contacts, "", 0);
        Core.getRecentContacts().add(contacts);
        TabOperationPanel.getContext().switchToChatLabel();
    }

}
