package cn.shu.wechat.swing.components;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.beans.pojo.Contacts;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.swing.frames.ImageViewerFrame;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.utils.AvatarUtil;
import cn.shu.wechat.swing.utils.ChatUtil;
import cn.shu.wechat.swing.utils.FontUtil;
import cn.shu.wechat.swing.utils.IconUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

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
            if (contacts.getSex() == 1){
                IconUtil.getIcon(this,"/image/man.png");
            }else if (contacts.getSex() == 2){
                IconUtil.getIcon(this,"/image/woman.png");
            }else{
                genderLabel.setIcon(null);
            }

        }else{
            genderLabel.setIcon(null);
        }
        if (contacts.getGroupName()!=null&&contacts.getGroupName().startsWith("@@")){
            usernameLabel.setText(ContactsTools.getMemberNickNameOfGroup(contacts.getGroupName(),contacts.getUsername()));
        }else{
            usernameLabel.setText(ContactsTools.getContactNickNameByUserName(contacts.getUsername()));
        }
        chat.setVisible(Core.getMemberMap().containsKey(contacts.getUsername()));

        avatarLabel.setIcon(IconUtil.getIcon(this,"/image/image_loading.gif"));
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
                    orLoadBigAvatar = orLoadBigAvatar.getScaledInstance(220,220,Image.SCALE_SMOOTH);
                    avatarLabel.setIcon(new ImageIcon(orLoadBigAvatar));
                }

            }
        }.execute();
        remarkNameLabel.setText("备注："+contacts.getRemarkname());
        signatureLabel.setText("签名："+contacts.getSignature());
        signatureLabel.setToolTipText(contacts.getSignature());
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
        setBorder(new EmptyBorder(0,0,0,0));
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(0,0,0,0));
        contentPanel.setBackground(Color.WHITE);
        //头像
        avatarLabel.setPreferredSize(new Dimension(220, 200));
        avatarLabel.setHorizontalAlignment(JLabel.CENTER);
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
        usernameLabel.setFont(FontUtil.getDefaultFont(16));
        infoPanel.add(nickNameArea);

        infoPanel.add(signatureLabel);
        infoPanel.add(remarkNameLabel);
        infoPanel.add(areaLabel);
        infoPanel.setBorder(new EmptyBorder(0,10,0,10));
        contentPanel.add(avatarLabel, new GBC(0, 0)
                .setFill(GridBagConstraints.BOTH)
                .setWeight(1, 1));
        contentPanel.add(infoPanel, new GBC(0, 1)
                //.setInsets(10,10,10,10)
                .setFill(GridBagConstraints.HORIZONTAL)
                .setWeight(1, 1));

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
                BufferedImage bufferedImage = AvatarUtil.createOrLoadBigAvatar(contacts.getUsername(), contacts.getHeadimgurl());
                if (bufferedImage == null) {
                    JOptionPane.showMessageDialog(MainFrame.getContext(), "图片下载中...", "文件不存在", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                ImageViewerFrame instance = new ImageViewerFrame(bufferedImage);
            
                instance.toFront();
                instance.setVisible(true);
                super.mouseClicked(e);
            }
        });
    }


}
