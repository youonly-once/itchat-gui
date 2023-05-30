package cn.shu.wechat.swing.frames;

import cn.shu.WeChatStater;
import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.constant.WxConstant;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.dto.response.WxCreateRoomResp;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.service.LoginService;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCButton;
import cn.shu.wechat.swing.components.RCTextField;
import cn.shu.wechat.swing.db.model.ContactsUser;
import cn.shu.wechat.swing.entity.SelectUserData;
import cn.shu.wechat.swing.panels.SelectUserPanel;
import cn.shu.wechat.swing.utils.ChatUtil;
import cn.shu.wechat.swing.utils.FontUtil;
import cn.shu.wechat.utils.SpringContextHolder;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by 舒新胜 on 07/06/2017.
 */
public class CreateGroupDialog extends JDialog {
    private static CreateGroupDialog context;
    private JPanel editorPanel;
    private RCTextField groupNameTextField;

    private SelectUserPanel selectUserPanel;
    private JPanel buttonPanel;
    private JButton cancelButton;
    private JButton okButton;
    private final List<SelectUserData> userList = new ArrayList<>();



    public static final int DIALOG_WIDTH = 580;
    public static final int DIALOG_HEIGHT = 500;


    public CreateGroupDialog(Frame owner, boolean modal) {
        super(owner, modal);
        context = this;

        initComponents();
        initData();

        initView();
        setListeners();
    }

    private void initData() {
        for (Contacts con : Core.getContactMap().values()) {
            userList.add(new SelectUserData(con.getUsername(),
                    ContactsTools.getContactDisplayNameByUserName(con.getUsername()),
                    false));
        }
        selectUserPanel = new SelectUserPanel(DIALOG_WIDTH, DIALOG_HEIGHT - 100, userList);

    }

    private void initComponents() {
        int posX = MainFrame.getContext().getX();
        int posY = MainFrame.getContext().getY();

        posX = posX + (MainFrame.getContext().currentWindowWidth - DIALOG_WIDTH) / 2;
        posY = posY + (MainFrame.getContext().currentWindowHeight - DIALOG_HEIGHT) / 2;
        setBounds(posX, posY, DIALOG_WIDTH, DIALOG_HEIGHT);
        setUndecorated(true);
        getRootPane().setBorder(new LineBorder(Colors.DIALOG_BORDER));

        /*if (OSUtil.getOsType() != OSUtil.Mac_OS)
        {
            // 边框阴影，但是会导致字体失真
            AWTUtilities.setWindowOpaque(this, false);
            //getRootPane().setOpaque(false);
            getRootPane().setBorder(ShadowBorder.newInstance());
        }*/

        // 输入面板
        editorPanel = new JPanel();
        groupNameTextField = new RCTextField();
        groupNameTextField.setPlaceholder("群聊名称");
        groupNameTextField.setPreferredSize(new Dimension(DIALOG_WIDTH / 2, 35));
        groupNameTextField.setFont(FontUtil.getDefaultFont(14));
        groupNameTextField.setForeground(Colors.FONT_BLACK);
        groupNameTextField.setMargin(new Insets(0, 15, 0, 0));



        // 按钮组
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        cancelButton = new RCButton("取消");
        cancelButton.setForeground(Colors.FONT_BLACK);

        okButton = new RCButton("创建", Colors.MAIN_COLOR, Colors.MAIN_COLOR_DARKER, Colors.MAIN_COLOR_DARKER);
        okButton.setBackground(Colors.PROGRESS_BAR_START);
    }


    private void initView() {
        editorPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
      //  editorPanel.add(groupNameTextField);

        buttonPanel.add(cancelButton, new GBC(0, 0).setWeight(1, 1).setInsets(15, 0, 0, 0));
        buttonPanel.add(okButton, new GBC(1, 0).setWeight(1, 1));


        add(editorPanel, BorderLayout.NORTH);
        add(selectUserPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setListeners() {
        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setVisible(false);

                super.mouseClicked(e);
            }
        });

        okButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (okButton.isEnabled()) {
                    okButton.setEnabled(false);
                    okButton.setText("创建中...");
                    cancelButton.setEnabled(false);
                    createRoom();
                }

                super.mouseClicked(e);
            }
        });
    }

    private void createRoom(){
        try{
            new SwingWorker<Object,Object>() {
                private  WxCreateRoomResp wxCreateRoomResp;
                @Override
                protected Object doInBackground() throws Exception {
                   wxCreateRoomResp = SpringContextHolder.getBean(LoginService.class).webWxCreateRoom(
                            selectUserPanel.getSelectedUser().stream()
                                    .map(O -> Core.getContactMap().get(O.getUserName())
                                    ).collect(Collectors.toList()));
                    return null;
                }

                @Override
                protected void done() {
                    if (wxCreateRoomResp.getBaseResponse().getRet() ==0
                            && StringUtils.isNotEmpty(wxCreateRoomResp.getChatRoomName())){
                        Contacts group = Contacts.builder().username(wxCreateRoomResp.getChatRoomName())
                                .memberlist(wxCreateRoomResp.getMemberList()).build();
                        Core.getMemberMap().put(wxCreateRoomResp.getChatRoomName(),group);
                        Core.getGroupIdSet().add(wxCreateRoomResp.getChatRoomName());
                        Core.getGroupMap().put(wxCreateRoomResp.getChatRoomName(),group);

                        ChatUtil.openOrCreateDirectChat(wxCreateRoomResp.getChatRoomName());
                        CreateGroupDialog.context.dispose();
                    }else{
                        JOptionPane.showMessageDialog(MainFrame.getContext(), wxCreateRoomResp.getBaseResponse().getErrMsg(), "创建失败", JOptionPane.ERROR_MESSAGE);
                        okButton.setEnabled(true);
                        okButton.setText("创建");
                        cancelButton.setEnabled(true);
                    }
                    super.done();
                }
            }.execute();



        }
        catch (Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame.getContext(), e.getMessage(), "创建失败", JOptionPane.ERROR_MESSAGE);
            okButton.setEnabled(true);
            cancelButton.setEnabled(true);
            okButton.setText("创建");
        }
    }
    private void checkRoomExists(String name) {
        if (/*roomService.findByName(name)*/ ""!= null) {
            showRoomExistMessage(name);
            okButton.setEnabled(true);
        } else {
            List<SelectUserData> list = selectUserPanel.getSelectedUser();
            String[] usernames = new String[list.size()];

            for (int i = 0; i < list.size(); i++) {
                usernames[i] = list.get(i).getUserName();
            }

            //createChannelOrGroup(name, privateCheckBox.isSelected(), usernames);
        }
    }

    /**
     * 创建Channel或Group
     *
     * @param name
     * @param privateGroup
     * @param usernames
     */
    private void createChannelOrGroup(String name, boolean privateGroup, String[] usernames) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < usernames.length; i++) {
            sb.append("\"" + usernames[i] + "\"");
            if (i < usernames.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");

        JOptionPane.showMessageDialog(MainFrame.getContext(), "创建群聊", "创建群聊", JOptionPane.INFORMATION_MESSAGE);
    }

    public static CreateGroupDialog getContext() {
        return context;
    }

    public void showRoomExistMessage(String roomName) {
        JOptionPane.showMessageDialog(null, "群组\"" + roomName + "\"已存在", "群组已存在", JOptionPane.WARNING_MESSAGE);
        groupNameTextField.setText("");
        groupNameTextField.requestFocus();
    }

}
