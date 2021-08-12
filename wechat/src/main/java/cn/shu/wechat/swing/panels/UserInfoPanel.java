package cn.shu.wechat.swing.panels;


import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.beans.pojo.Contacts;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.swing.components.*;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.utils.AvatarUtil;
import cn.shu.wechat.swing.utils.ChatUtil;
import cn.shu.wechat.swing.utils.FontUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * Created by 舒新胜 on 2017/6/15.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class UserInfoPanel extends ParentAvailablePanel {
    private JPanel contentPanel = new JPanel();
    private RCButton button= new RCButton();
    private JLabel avatarLabel = new JLabel();
    private JLabel sexLabel = new JLabel();
    private UserInfoDetailItemLabel username = new UserInfoDetailItemLabel("ID");
    private UserInfoDetailItemLabel nickname= new UserInfoDetailItemLabel("昵称");
    private UserInfoDetailItemLabel remarkName= new UserInfoDetailItemLabel("备注");

    private UserInfoDetailItemLabel signature = new UserInfoDetailItemLabel("签名");
    private UserInfoDetailItemLabel region= new UserInfoDetailItemLabel("地区");
    private JPanel titlePanel = new TitlePanel(this);

    private volatile String currUserId;

    private static UserInfoPanel context;
    public UserInfoPanel(JPanel parent) {
        super(parent);
        UserInfoPanel.context = this;
        initComponents();
        initView();
        setListeners();
        setContacts(Core.getUserSelf());
    }
    public static UserInfoPanel getContext() {
        return context;
    }
    public void setContacts(Contacts contacts){
        currUserId = contacts.getUsername();
        new SwingWorker<Object,Object>(){
            Image orLoadBigAvatar = null;
            private final String userId = contacts.getUsername();
            @Override
            protected Object doInBackground() throws Exception {
                orLoadBigAvatar = AvatarUtil.createOrLoadBigAvatar(contacts.getUsername(), contacts.getHeadimgurl());
                if (orLoadBigAvatar != null){
                    orLoadBigAvatar = orLoadBigAvatar.getScaledInstance(200,200,Image.SCALE_SMOOTH);
                }

                return null;
            }

            @Override
            protected void done() {
                if (orLoadBigAvatar!=null && userId.equals(currUserId)){
                    avatarLabel.setIcon(new ImageIcon(orLoadBigAvatar));
                }
                super.done();
            }
        }.execute();
        if (contacts.getSex() == 1){
            sexLabel.setIcon(new ImageIcon(getClass().getResource("/image/woman.png")));
        }else if (contacts.getSex() == 2){
            sexLabel.setIcon(new ImageIcon(getClass().getResource("/image/man.png")));
        }else {
            sexLabel.setIcon(null);
        }

        username.setValue(contacts.getUsername());
        nickname.setValue(ContactsTools.getContactNickNameByUserName(contacts.getUsername()));
        remarkName.setValue(ContactsTools.getContactRemarkNameByUserName(contacts.getUsername()));
        signature.setValue(ContactsTools.getSignatureNameOfGroup(contacts.getUsername()));
        region.setValue(contacts.getProvince()+" "+contacts.getCity());
    }
    private void initComponents() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.CENTER, 10, 20, true, false));


        username.setFont(FontUtil.getDefaultFont(20));

        button = new RCButton("发消息", Colors.MAIN_COLOR, Colors.MAIN_COLOR_DARKER, Colors.MAIN_COLOR_DARKER);
        button.setBackground(Colors.PROGRESS_BAR_START);
        button.setPreferredSize(new Dimension(200, 40));
        button.setFont(FontUtil.getDefaultFont(16));
    }

    private void initView() {
        this.setLayout(new GridBagLayout());


        JPanel infoPanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.CENTER, 0, 10, true, false));
        //infoPanel.add(username);
        nickname.add(sexLabel);
        infoPanel.add(nickname);
        infoPanel.add(remarkName);
        infoPanel.add(signature);
        infoPanel.add(region);

        JPanel avatarInfoPanel = new JPanel();
        avatarInfoPanel.setLayout(new BorderLayout( 15, 0));
        avatarInfoPanel.add(avatarLabel,BorderLayout.WEST);
        avatarInfoPanel.add(infoPanel,BorderLayout.CENTER);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(avatarInfoPanel);
        contentPanel.add(button);
        add(titlePanel, new GBC(0, 0).setWeight(1, 1).setFill(GBC.BOTH).setAnchor(GBC.CENTER).setInsets(0, 0, 0, 0));
        add(contentPanel, new GBC(0, 1).setWeight(1, 1000).setAnchor(GBC.CENTER)
                .setInsets(0, 0, 250, 0));
    }


    private void setListeners() {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ChatUtil.openOrCreateDirectChat(currUserId);
                super.mouseClicked(e);
            }
        });
    }

    static class UserInfoDetailItemLabel extends JPanel{
        private final JLabel  nameLabel = new JLabel ();
        private final SizeAutoAdjustTextArea  valueLabel = new SizeAutoAdjustTextArea (200);
        public UserInfoDetailItemLabel(String name,String value){
            setNameAndValue(name,value);
        }
        public UserInfoDetailItemLabel(String name){
            this(name,"");
            initView();
        }
        public void setNameAndValue(String name,String value){
            this.nameLabel.setText(name);
            setValue(value);
        }
        private void initView(){
            setLayout(new FlowLayout(FlowLayout.LEFT,20,0));
            nameLabel.setForeground(Color.GRAY);
            valueLabel.setForeground(Color.BLACK);
            add(nameLabel);
            add(valueLabel);
        }
        public void setValue(String value){
            this.valueLabel.setText(value);
        }
    }


}
