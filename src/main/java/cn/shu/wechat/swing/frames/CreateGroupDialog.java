package cn.shu.wechat.swing.frames;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.dto.response.WxCreateRoomResp;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.entity.RoomItem;
import cn.shu.wechat.entity.SelectUserData;
import cn.shu.wechat.service.LoginService;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCButton;
import cn.shu.wechat.swing.components.RCTextField;
import cn.shu.wechat.swing.panels.SelectUserPanel;
import cn.shu.wechat.swing.panels.left.tabcontent.RoomsPanel;
import cn.shu.wechat.utils.ChatUtil;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import cn.shu.wechat.utils.FontUtil;
import cn.shu.wechat.utils.SpringContextHolder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by 舒新胜 on 07/06/2017.
 */
public class CreateGroupDialog extends JDialog {
    @Getter
    private static CreateGroupDialog context;
    private JPanel editorPanel;
    private RCTextField groupNameTextField;

    private SelectUserPanel selectUserPanel;
    private JPanel buttonPanel;
    private JButton cancelButton;
    private JButton okButton;
    private final List<SelectUserData> userList = new ArrayList<>();
    private final Collection<Contacts> searchList = new ArrayList<>();



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
        userList.clear();
        searchList.clear();
        for (RoomItem con : RoomsPanel.getContext().getRoomItemList()) {
            userList.add(new SelectUserData(con.getRoomId(),
                    ContactsTools.getContactDisplayNameByUserName(con.getRoomId()),
                    false));
        }
        searchList.addAll(Core.getMemberMap().values());
        selectUserPanel = new SelectUserPanel(DIALOG_WIDTH, DIALOG_HEIGHT - 100, userList, searchList);

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
                dispose();

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


        ExecutorServiceUtil.getGlobalExecutorService().submit(() -> {
            try {
                WxCreateRoomResp wxCreateRoomResp = SpringContextHolder.getBean(LoginService.class).webWxCreateRoom(
                        selectUserPanel.getSelectedUser().stream()
                                .map(O -> Core.getMemberMap().get(O.getUserName())
                                ).collect(Collectors.toList()));
                if (wxCreateRoomResp.getBaseResponse().getRet() == 0
                        && StringUtils.isNotEmpty(wxCreateRoomResp.getChatRoomName())) {
                    Contacts group = Contacts.builder().username(wxCreateRoomResp.getChatRoomName())
                            .memberlist(wxCreateRoomResp.getMemberList()).build();

                    ContactsTools.addContacts(group);

                    SwingUtilities.invokeLater(() -> {
                        ChatUtil.openOrCreateDirectChat(wxCreateRoomResp.getChatRoomName());
                        CreateGroupDialog.context.dispose();
                    });

                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(MainFrame.getContext(), wxCreateRoomResp.getBaseResponse().getErrMsg(), "创建失败", JOptionPane.ERROR_MESSAGE);
                        okButton.setEnabled(true);
                        okButton.setText("创建");
                        cancelButton.setEnabled(true);
                    });
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(MainFrame.getContext(), e.getMessage(), "创建失败", JOptionPane.ERROR_MESSAGE);
                okButton.setEnabled(true);
                cancelButton.setEnabled(true);
                okButton.setText("创建");
            }


        });
    }

    public void setVisible(boolean aF) {
        if (!aF) {
            userList.clear();
            searchList.clear();
        }
        super.setVisible(aF);
    }

    public void dispose() {
        userList.clear();
        searchList.clear();
        super.dispose();
    }
}
