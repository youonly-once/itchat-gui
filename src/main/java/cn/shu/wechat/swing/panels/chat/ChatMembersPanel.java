package cn.shu.wechat.swing.panels.chat;

import cn.shu.wechat.core.Core;
import cn.shu.wechat.pojo.entity.Contacts;
import cn.shu.wechat.service.LoginService;
import cn.shu.wechat.swing.adapter.RoomMembersAdapter;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCButton;
import cn.shu.wechat.swing.components.RCListView;
import cn.shu.wechat.swing.db.model.ContactsUser;
import cn.shu.wechat.swing.entity.SelectUserData;
import cn.shu.wechat.swing.frames.AddOrRemoveMemberDialog;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.panels.ParentAvailablePanel;
import cn.shu.wechat.swing.utils.AvatarUtil;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import cn.shu.wechat.utils.SpringContextHolder;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 舒新胜 on 07/06/2017.
 */
@Log4j2
public class ChatMembersPanel extends ParentAvailablePanel {
    public final int ROOM_MEMBER_PANEL_WIDTH = 200;

    /**
     * list
     */
    private final RCListView listView = new RCListView();
    /**
     * 操作面板
     */
    private final JPanel operationPanel = new JPanel();
    /**
     * 退群按钮
     */
    private JButton leaveButton;
    /**
     * 群成员
     */
    private final List<Contacts> members = new ArrayList<>();

    /**
     * 房间ID
     */
    private final String roomId;
    /**
     *
     */
    private RoomMembersAdapter adapter;
    /**
     * 添加或移除群聊面板
     */
    private AddOrRemoveMemberDialog addOrRemoveMemberDialog;

    /**
     * 同一时间只能一个线程更新成员信息
     */
    private volatile boolean isUpdatingUI = false;

    public ChatMembersPanel(JPanel parent, String roomId) {
        super(parent);
        this.roomId = roomId;
        initComponents();
        initView();
        setListeners();
    }

    private void initComponents() {
        setBorder(new LineBorder(Colors.LIGHT_GRAY));
        setBackground(Colors.FONT_WHITE);

        setPreferredSize(new Dimension(ROOM_MEMBER_PANEL_WIDTH, MainFrame.getContext().currentWindowHeight));
        setVisible(false);
        listView.setScrollBarColor(Colors.SCROLL_BAR_THUMB, Colors.WINDOW_BACKGROUND);
        listView.setContentPanelBackground(Colors.FONT_WHITE);
        listView.getContentPanel().setBackground(Colors.FONT_WHITE);

        operationPanel.setPreferredSize(new Dimension(60, 80));
        operationPanel.setBackground(Colors.FONT_WHITE);


        leaveButton = new RCButton("退出群聊", Colors.WINDOW_BACKGROUND_LIGHT, Colors.WINDOW_BACKGROUND, Colors.SCROLL_BAR_TRACK_LIGHT);
        leaveButton.setForeground(Colors.RED);
        leaveButton.setPreferredSize(new Dimension(180, 30));

    }

    private void initView() {
        operationPanel.add(leaveButton);

        setLayout(new GridBagLayout());
        add(listView, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 1000));
        add(operationPanel, new GBC(0, 1).setFill(GBC.BOTH).setWeight(1, 1).setInsets(10, 0, 5, 0));

        adapter = new RoomMembersAdapter(members);
        listView.setAdapter(adapter);
    }


    /**
     * 设置成员面板显示状态
     *
     * @param aFlag {TRUE}显示
     */
    public void setVisibleAndUpdateUI(boolean aFlag) {
        if (aFlag) {
            updateUI();
        }

        setVisible(aFlag);
    }

    @Override
    public void updateUI() {
        if (isUpdatingUI) {
            return;
        }
        if (roomId != null) {
            isUpdatingUI = true;
            new SwingWorker<Object, Object>() {

                @Override
                protected Object doInBackground() throws Exception {
                    getRoomMembers();
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        //TODO 这一步比较耗时
                        listView.notifyDataSetChanged(false);
                        // 单独聊天，不显示退出按钮
                        leaveButton.setVisible(roomId.startsWith("@@"));
                        setLeaveButtonVisibility(true);

                        if (isRoomCreator()) {
                            leaveButton.setText("解散群聊");
                        } else {
                            leaveButton.setText("退出群聊");
                        }
                        updateAvatar();
                    } finally {
                        isUpdatingUI = false;
                    }

                }
            }.execute();

        }
    }

    /**
     * 获取群成员
     */
    private void getRoomMembers() {

        // 单独聊天，成员两人
        if (!roomId.startsWith("@@")) {
            members.clear();
            //显示另外个人
            members.add(Core.getMemberMap().get(roomId));
        } else {
            //群聊
            //获取成员
            Contacts group = Core.getMemberMap().get(roomId);
            List<Contacts> memberlist = group.getMemberlist();
            if (CollectionUtils.isEmpty(memberlist)
                    || StringUtils.isEmpty(memberlist.get(0).getHeadimgurl())) {
                LoginService bean = SpringContextHolder.getBean(LoginService.class);
                memberlist = bean.WebWxBatchGetContact(roomId);
                members.clear();
                Contacts contacts1 = Core.getMemberMap().get(Core.getUserName());
                boolean remove = memberlist.remove(contacts1);
                members.addAll(memberlist);
            } else {
                members.clear();
                Contacts contacts1 = Core.getMemberMap().get(Core.getUserName());
                boolean remove = memberlist.remove(contacts1);
                members.addAll(memberlist);
            }

            for (Contacts contacts : memberlist) {
                contacts.setGroupName(roomId);
            }


            if (isRoomCreator()) {
                members.add(Contacts.builder().displayname("添加成员").build());
                if (members.size() > 1) {
                    members.add(Contacts.builder().displayname("删除成员").build());
                }
            }


        }


    }

    /**
     * 更新头像
     */
    private void updateAvatar() {
        ExecutorServiceUtil.getGlobalExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                //下载头像
                for (int i = 0; i < members.size(); i++) {
                    Contacts contacts = members.get(i);
                    int finali = i;
                    new SwingWorker<Object, Object>() {

                        private ImageIcon memberAvatar;

                        @Override
                        protected Object doInBackground() throws Exception {
                            //下载头像
                            if (contacts.getUsername() == null) {
                                return null;
                            }
                            memberAvatar = AvatarUtil.createOrLoadMemberAvatar(roomId, contacts.getUsername());
                            return null;
                        }

                        @Override
                        protected void done() {
                            //更新头像
                            if (memberAvatar != null) {
                                updateAvatar(finali, memberAvatar);
                            }
                        }
                    }.execute();
                }
            }
        });

    }

    /**
     * 判断当前用户是否是房间创建者
     *
     * @return 是否为群主
     */
    private boolean isRoomCreator() {
        Contacts contacts = Core.getMemberMap().get(roomId);
        if (contacts == null || contacts.getIsowner() == null) {
            return false;
        }
        //1群主 0成员
        return contacts.getIsowner() == 1;
    }


    public void setLeaveButtonVisibility(boolean visible) {
        operationPanel.setVisible(visible);
    }

    private void setListeners() {
        adapter.setAddMemberButtonMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectAndAddRoomMember();
                super.mouseClicked(e);
            }
        });

        adapter.setRemoveMemberButtonMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectAndRemoveRoomMember();
                super.mouseClicked(e);
            }
        });

        leaveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isRoomCreator()) {
                    int ret = JOptionPane.showConfirmDialog(MainFrame.getContext(), "确认解散群聊？", "确认解散群聊", JOptionPane.YES_NO_OPTION);
                    if (ret == JOptionPane.YES_OPTION) {
                        deleteChannelOrGroup(roomId);
                    }
                } else {
                    int ret = JOptionPane.showConfirmDialog(MainFrame.getContext(), "退出群聊，并从聊天列表中删除该群聊", "确认退出群聊", JOptionPane.YES_NO_OPTION);
                    if (ret == JOptionPane.YES_OPTION) {
                        leaveChannelOrGroup(roomId);
                    }
                }
                super.mouseClicked(e);
            }
        });
    }


    /**
     * 选择并添加群成员
     */
    private void selectAndAddRoomMember() {
        List<ContactsUser> contactsUsers = null;// contactsUserService.findAll();
        List<SelectUserData> selectUsers = new ArrayList<>();

        for (ContactsUser contactsUser : contactsUsers) {
            if (!members.contains(contactsUser.getUsername())) {
                selectUsers.add(new SelectUserData(contactsUser.getUsername(), false));
            }
        }
        addOrRemoveMemberDialog = new AddOrRemoveMemberDialog(MainFrame.getContext(), true, selectUsers);
        addOrRemoveMemberDialog.getOkButton().setText("添加");
        addOrRemoveMemberDialog.getOkButton().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (((JButton) e.getSource()).isEnabled()) {
                    ((JButton) e.getSource()).setEnabled(false);
                    List<SelectUserData> selectedUsers = addOrRemoveMemberDialog.getSelectedUser();
                    String[] userArr = new String[selectedUsers.size()];
                    for (int i = 0; i < selectedUsers.size(); i++) {
                        userArr[i] = selectedUsers.get(i).getName();
                    }

                    inviteOrKick(userArr, "invite");
                }
                super.mouseClicked(e);
            }
        });
        addOrRemoveMemberDialog.setVisible(true);
    }

    /**
     * 选择并移除群成员
     */
    private void selectAndRemoveRoomMember() {
        List<SelectUserData> userDataList = new ArrayList<>();
        //TODO
   /*     for (String member : members)
        {
            if (member.equals(room.getCreatorName()) || member.equals("添加成员") || member.equals("删除成员"))
            {
                continue;
            }
            userDataList.add(new SelectUserData(member, false));
        }*/

        addOrRemoveMemberDialog = new AddOrRemoveMemberDialog(MainFrame.getContext(), true, userDataList);
        addOrRemoveMemberDialog.getOkButton().setText("移除");
        addOrRemoveMemberDialog.getOkButton().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (((JButton) e.getSource()).isEnabled()) {
                    ((JButton) e.getSource()).setEnabled(false);
                    List<SelectUserData> selectedUsers = addOrRemoveMemberDialog.getSelectedUser();
                    String[] userArr = new String[selectedUsers.size()];
                    for (int i = 0; i < selectedUsers.size(); i++) {
                        userArr[i] = selectedUsers.get(i).getName();
                    }

                    inviteOrKick(userArr, "kick");
                }

                super.mouseClicked(e);
            }
        });
        addOrRemoveMemberDialog.setVisible(true);
    }


    private void inviteOrKick(final String[] usernames, String type) {
        // TODO: 添加或删除成员
        JOptionPane.showMessageDialog(null, usernames, type, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 删除Channel或Group
     *
     * @param roomId
     */
    private void deleteChannelOrGroup(String roomId) {
        JOptionPane.showMessageDialog(null, "删除群聊：" + roomId, "删除群聊", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 退出Channel或Group
     *
     * @param roomId
     */
    private void leaveChannelOrGroup(final String roomId) {
        JOptionPane.showMessageDialog(null, "退出群聊：" + roomId, "退出群聊", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 更新群成员头像
     *
     * @param pos   联系人位置
     * @param image 联系人头像
     */
    public void updateAvatar(int pos, ImageIcon image) {
        Contacts contacts = members.get(pos);
        contacts.setAvatarIcon(image);
        listView.notifyItemChanged(pos);
    }
}
