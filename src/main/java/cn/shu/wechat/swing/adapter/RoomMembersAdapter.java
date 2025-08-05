package cn.shu.wechat.swing.adapter;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.UserInfoPopup;
import cn.shu.wechat.swing.listener.AbstractMouseListener;
import cn.shu.wechat.swing.media.HeadLoadingSwingWorker;
import cn.shu.wechat.utils.IconUtil;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 舒新胜 on 07/06/2017.
 */
public class RoomMembersAdapter extends BaseAdapter<RoomMembersItemViewHolder> {
    private final List<Contacts> members;
    private final List<RoomMembersItemViewHolder> viewHolders = new ArrayList<>();

    @Setter
    private MouseAdapter addMemberButtonMouseListener;
    @Setter
    private MouseAdapter removeMemberButtonMouseListener;
    /**
     * 当前选中的viewHolder
     */
    private RoomMembersItemViewHolder selectedViewHolder;
    public RoomMembersAdapter(List<Contacts> members) {
        this.members = members;
    }

    @Override
    public RoomMembersItemViewHolder onCreateViewHolder(int viewType,int subViewType, int position) {
        return new RoomMembersItemViewHolder();
    }

    public void setRoomName(JLabel roomName, String name) {
        String displayName = name;
        if (name != null && name.length() > 3) {
            displayName = name.substring(0, 3) + "…";
        }
        roomName.setText("<html><div style='text-align:center;'>" + displayName + "</div></html>");
    }
    @Override
    public void onBindViewHolder(RoomMembersItemViewHolder viewHolder, int position) {
        Contacts contacts = members.get(position);

        if ("添加成员".equals(contacts.getDisplayname())) {
            viewHolder.setCursor(new Cursor(Cursor.HAND_CURSOR));
            String name = contacts.getDisplayname();
            //setRoomName(viewHolder.roomName, name);
            viewHolder.remove(viewHolder.roomName);
            ImageIcon imageIcon = IconUtil.getIcon(this, "/image/add_member.png", 40, 40);
            viewHolder.avatar.setIcon(imageIcon);

            viewHolder.addMouseListener(new AbstractMouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    //System.out.println("添加/刪除用戶");
                    //selectAndAddRoomMember();
                    if (addMemberButtonMouseListener != null) {
                        addMemberButtonMouseListener.mouseClicked(e);
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    viewHolder.setBackground(Colors.ITEM_SELECTED_LIGHT);
                    super.mouseEntered(e);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    viewHolder.setBackground(Colors.WINDOW_BACKGROUND_LIGHT);

                }
            });
        } else if ("删除成员".equals(contacts.getDisplayname())) {
            viewHolder.setCursor(new Cursor(Cursor.HAND_CURSOR));
            String name = contacts.getDisplayname();
            viewHolder.remove(viewHolder.roomName);
            //setRoomName(viewHolder.roomName, name);
            ImageIcon imageIcon = IconUtil.getIcon(this, "/image/delete_member.png", 40, 40);
            viewHolder.avatar.setIcon(imageIcon);

            viewHolder.addMouseListener(new AbstractMouseListener() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    viewHolder.setBackground(Colors.ITEM_SELECTED_LIGHT);
                    super.mouseEntered(e);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    viewHolder.setBackground(Colors.WINDOW_BACKGROUND_LIGHT);

                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (removeMemberButtonMouseListener != null) {
                        removeMemberButtonMouseListener.mouseClicked(e);
                    }
                }
            });
        } else {
            String userName = contacts.getUsername();
            String name = ContactsTools.getMemberDisplayNameOfGroup(contacts,userName);
            if (StringUtils.isEmpty(contacts.getGroupName())) {
                //非群成员头像
                new HeadLoadingSwingWorker(viewHolder.avatar, userName).loadAvatar();
            } else {
                new HeadLoadingSwingWorker(viewHolder.avatar, contacts.getGroupName(), userName).loadAvatar();
            }
            setRoomName(viewHolder.roomName, name);
            if (!name.equals(Core.getNickName())) {
                //TODO 重复添加事件了
                viewHolder.addMouseListener(new AbstractMouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (selectedViewHolder != null){
                            selectedViewHolder.setBackground(Colors.WINDOW_BACKGROUND_LIGHT);
                        }
                        selectedViewHolder = viewHolder;
                        viewHolder.setBackground(Colors.ITEM_SELECTED_LIGHT);

                        // 弹出用户信息面板
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            UserInfoPopup userInfoPopup = UserInfoPopup.getInstance();
                            userInfoPopup.setContacts(contacts);
                            userInfoPopup.show(e.getComponent(), e.getX(), e.getY());
                        }


                        for (RoomMembersItemViewHolder holder : viewHolders) {
                            if (holder != viewHolder) {
                                holder.setBackground(Colors.WINDOW_BACKGROUND_LIGHT);
                            }
                        }

                    }
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        viewHolder.setBackground(Colors.ITEM_SELECTED_LIGHT);
                        super.mouseEntered(e);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        viewHolder.setBackground(Colors.WINDOW_BACKGROUND_LIGHT);

                    }
                });
            }
        }

    }


    @Override
    public int getCount() {
        return members.size();
    }

}
