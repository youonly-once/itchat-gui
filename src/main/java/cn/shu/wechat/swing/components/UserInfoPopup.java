package cn.shu.wechat.swing.components;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.dto.response.msg.send.WebWXSendMsgResponse;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.swing.frames.ImageViewerFrame;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.utils.AvatarUtil;
import cn.shu.wechat.swing.utils.ChatUtil;
import cn.shu.wechat.swing.utils.FontUtil;
import cn.shu.wechat.swing.utils.IconUtil;
import cn.shu.wechat.utils.ExecutorServiceUtil;

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

    private UserInfoPopup() {
        initComponents();
        initView();
        setListener();

    }

    private static final class ContextHolder {
        static final UserInfoPopup Context = new UserInfoPopup();
    }

    public static UserInfoPopup getInstance(){
        return ContextHolder.Context;
    }

    /**
     * 显示指定联系人信息
     * @param contacts
     */
    public void setContacts(Contacts contacts){
        this.contacts =contacts;
        areaLabel.setText("地区："+contacts.getProvince()+" "+contacts.getCity());
        if (contacts.getSex() !=null){
            if (contacts.getSex() == 1){
                genderLabel.setIcon( IconUtil.getIcon(this,"/image/man.png"));
            }else if (contacts.getSex() == 2){
                genderLabel.setIcon( IconUtil.getIcon(this,"/image/woman.png"));
            }else{
                genderLabel.setIcon(null);
            }

        }else{
            genderLabel.setIcon(null);
        }
        if (contacts.getGroupName()!=null&&contacts.getGroupName().startsWith("@@")){
            String memberNickNameOfGroup = ContactsTools.getMemberNickNameOfGroup(contacts.getGroupName(), contacts.getUsername());
            usernameLabel.setText(memberNickNameOfGroup == null ?contacts.getNickname():memberNickNameOfGroup);
        }else{
            String memberNickNameOfGroup = ContactsTools.getContactNickNameByUserName(contacts.getUsername());
            usernameLabel.setText(memberNickNameOfGroup == null ?contacts.getNickname():memberNickNameOfGroup);
        }
        if (Core.getMemberMap().containsKey(contacts.getUsername()) && contacts.getTicket()==null) {
            chat.setIcon(IconUtil.getIcon(this, "/image/chat.png", 20, 20));
            chat.setToolTipText("开始聊天");
            chat.setVisible(true);
        }else{
            chat.setIcon(IconUtil.getIcon(this,"/image/add.png"));
            chat.setToolTipText("添加好友");
            chat.setVisible(true);
        }


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
        chat.setVisible(true);
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



        nickNameArea.add(chat,BorderLayout.EAST);
        usernameLabel.setFont(FontUtil.getDefaultIconFont());
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
                if (Core.getMemberMap().containsKey(contacts.getUsername())
                        && contacts.getTicket()==null) {
                    ChatUtil.openOrCreateDirectChat(contacts.getUsername());
                    setVisible(false);
                }else{
                    setVisible(false);
                    ExecutorServiceUtil.getGlobalExecutorService().execute(new Runnable() {
                        @Override
                        public void run() {
                            WebWXSendMsgResponse webWXSendMsgResponse = MessageTools.addFriend(contacts.getUsername(), contacts.getTicket());

                            if (webWXSendMsgResponse.getBaseResponse().getRet() == 0) {
                                Core.getMemberMap().put(contacts.getUsername(),contacts);
                                ChatUtil.openOrCreateDirectChat(contacts.getUsername());
                                contacts.setTicket(null);
                            }
                        }
                    });

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
                ImageViewerFrame instance = ImageViewerFrame.getInstance();
                instance.setImage(bufferedImage);
                instance.toFront();
                instance.setVisible(true);
                super.mouseClicked(e);
            }
        });
    }


}
