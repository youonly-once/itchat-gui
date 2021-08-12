package cn.shu.wechat.swing.components;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.beans.pojo.Contacts;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.swing.ImageViewer.ImageViewerFrame;
import cn.shu.wechat.swing.panels.*;
import cn.shu.wechat.swing.utils.AvatarUtil;
import cn.shu.wechat.swing.utils.ChatUtil;
import cn.shu.wechat.swing.utils.FontUtil;
import cn.shu.wechat.swing.utils.IconUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;
import java.util.Objects;

/**
 * Created by 舒新胜 on 07/06/2017.
 */
public class UserInfoPopup extends JPopupMenu {
    private JPanel contentPanel;
    private JLabel avatarLabel;
    private JLabel usernameLabel;
    private Contacts contacts;
    private JLabel areaLabel;
    private JLabel genderLabel;
    private JLabel remarkNameLabel;
    private JLabel signatureLabel;
    private JLabel chat;
    private static UserInfoPopup Context;

    private UserInfoPopup() {
        initComponents();
        initView();
        setListener();

    }
    public static UserInfoPopup getInstance(){
        if (Context == null){
            synchronized (UserInfoPopup.class){
                if (Context == null){
                    Context = new UserInfoPopup();
                }
            }
        }
        return Context;
    }

    /**
     * 显示指定联系人信息
     * @param contacts
     */
    public void setContacts(Contacts contacts){
        this.contacts =contacts;
        areaLabel.setText("地区："+contacts.getProvince()+" "+contacts.getCity()+" "+contacts.getAlias());
        if (contacts.getSex() !=null){
            genderLabel.setIcon(IconUtil.getIcon(this,contacts.getSex() == 1?"/image/man.png":"/image/woman.png"));
        }else{
            genderLabel.setIcon(null);
        }
        if (contacts.getGroupName()!=null&&contacts.getGroupName().startsWith("@@")){
            usernameLabel.setText(ContactsTools.getMemberNickNameOfGroup(contacts.getGroupName(),contacts.getUsername()));
        }else{
            usernameLabel.setText(ContactsTools.getContactNickNameByUserName(contacts.getUsername()));
        }
        chat.setVisible(Core.getMemberMap().containsKey(contacts.getUsername()));
        ImageIcon imageIcon = new ImageIcon();

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
                }
                avatarLabel.setIcon(imageIcon);
            }
        }.execute();
        remarkNameLabel.setText("备注："+contacts.getRemarkname());
        signatureLabel.setText("签名："+contacts.getSignature());
    }
    private void initComponents() {
        setBackground(Colors.WINDOW_BACKGROUND_LIGHT);
        contentPanel = new JPanel();
        contentPanel.setPreferredSize(new Dimension(220, 350));
        contentPanel.setBackground(Colors.WINDOW_BACKGROUND_LIGHT);

        avatarLabel = new JLabel();
        usernameLabel = new JLabel();

        areaLabel =new JLabel();
        areaLabel.setForeground(Color.GRAY);
        areaLabel.setFont(FontUtil.getDefaultFont(12));

        genderLabel =new JLabel();

        remarkNameLabel =new JLabel();
        remarkNameLabel.setForeground(Color.GRAY);
        remarkNameLabel.setFont(FontUtil.getDefaultFont(12));

        signatureLabel = new JLabel();
        signatureLabel.setForeground(Color.GRAY);
        signatureLabel.setFont(FontUtil.getDefaultFont(12));

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
        nickNameArea.setBackground(Color.white);
        nickNameArea.add(usernameLabel,BorderLayout.WEST);
        nickNameArea.add(genderLabel,BorderLayout.CENTER);


        ImageIcon icon = IconUtil.getIcon(this, "/image/chat.png", 20, 20);
        chat.setIcon(icon);
        nickNameArea.add(chat,BorderLayout.EAST);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
        usernameLabel.setFont(FontUtil.getDefaultFont(16));
        infoPanel.add(nickNameArea);

        infoPanel.add(signatureLabel);
        infoPanel.add(remarkNameLabel);
        infoPanel.add(areaLabel);

        contentPanel.add(avatarPanel, new GBC(0, 0).setWeight(1, 1));
        contentPanel.add(infoPanel, new GBC(0, 1).setWeight(1, 1));

    }

    private void setListener() {
        chat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (Core.getMemberMap().containsKey(contacts.getUsername())) {
                    ChatUtil.openOrCreateDirectChat(contacts.getUsername());
                    setVisible(false);;
                }

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


}
