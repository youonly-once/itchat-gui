package cn.shu.wechat.swing.adapter;

import cn.shu.wechat.beans.pojo.Contacts;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.entity.RoomItem;
import cn.shu.wechat.swing.listener.AbstractMouseListener;
import cn.shu.wechat.swing.panels.*;
import cn.shu.wechat.swing.utils.AvatarUtil;
import cn.shu.wechat.swing.utils.TimeUtil;
import com.alibaba.fastjson.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author song
 * @date 17-5-30
 */
public class RoomItemsAdapter extends BaseAdapter<RoomItemViewHolder> {
    /**
     * 房间条目
     */
    private final List<RoomItem> roomItems;
    /**
     * 当前选中的viewHolder
     */
    private RoomItemViewHolder selectedViewHolder;

    public RoomItemsAdapter(List<RoomItem> roomItems) {
        this.roomItems = roomItems;
    }

    @Override
    public int getCount() {
        return roomItems.size();
    }

    @Override
    public RoomItemViewHolder onCreateViewHolder(int viewType, int position) {
        return new RoomItemViewHolder();
    }

    @Override
    public void onBindViewHolder(RoomItemViewHolder viewHolder, int position) {
        //viewHolder.setCursor(new Cursor(Cursor.HAND_CURSOR));
        //System.out.println(viewHolders.size());
        RoomItem roomItem = roomItems.get(position);

        viewHolder.setTag(roomItem.getRoomId());

        viewHolder.roomName.setText(roomItem.getName());

        //异步加载头像会闪烁
  /*      new SwingWorker<Object, Object>() {
            Image orLoadAvatar ;
            @Override
            protected Object doInBackground() throws Exception {
                orLoadAvatar = AvatarUtil.createOrLoadAvatar(roomItem.getRoomId());
                return null;
            }

            @Override
            protected void done() {
                if (orLoadAvatar != null) {
                    ImageIcon icon = new ImageIcon();
                    icon.setImage(orLoadAvatar);
                    viewHolder.avatar.setIcon(icon);
                    RoomsPanel.getContext().getRoomItemsListView().getContentPanel().revalidate();

                }
            }
        }.execute();*/
        //if (!roomItem.getRoomId().equals("filehelper")){
            ImageIcon icon = icon = new ImageIcon();
            icon.setImage(AvatarUtil.createOrLoadAvatar(roomItem.getRoomId()));
            viewHolder.avatar.setIcon(icon);
      //  }




        // 消息
        viewHolder.brief.setText(roomItem.getLastMessage());
        if (roomItem.getLastMessage() != null && roomItem.getLastMessage().length() > 15) {
            viewHolder.brief.setText(roomItem.getLastMessage().substring(0, 15) + "...");
        } else {
            viewHolder.brief.setText(roomItem.getLastMessage());
        }

        // 时间
        if (roomItem.getTimestamp() > 0) {
            viewHolder.time.setText(TimeUtil.diff(roomItem.getTimestamp()));
        }

        // 未读消息数
        if (roomItem.getUnreadCount() > 0) {
            viewHolder.unreadCount.setVisible(true);
            viewHolder.unreadCount.setText(roomItem.getUnreadCount() + "");
        } else {
            viewHolder.unreadCount.setVisible(false);
        }

        // 设置是否激活
        if (roomItem.getRoomId().equals(RoomChatPanel.getContext().getCurrRoomId())) {
            setBackground(viewHolder, Colors.ITEM_SELECTED);
            selectedViewHolder = viewHolder;
        }
        //viewHolder.unreadCount.setVisible(true);
        //viewHolder.unreadCount.setText(roomItem.getUnreadCount() + "1");

        //鼠标点击事件 点击变色并进入房间
        viewHolder.addMouseListener(new AbstractMouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {

                    if (selectedViewHolder != viewHolder) {
                        //之前选择的房间背景色去掉
                        setBackground(selectedViewHolder,Colors.DARK);
                        // 进入房间
                        enterRoom(roomItem.getRoomId());

                  /*      for (RoomItemViewHolder holder : viewHolders) {
                            if (holder != viewHolder) {
                                setBackground(holder, Colors.DARK);
                            }
                        }*/

                        //setBackground(viewHolder, Colors.ITEM_SELECTED);
                        selectedViewHolder = viewHolder;
                    }
                }
            }


            @Override
            public void mouseEntered(MouseEvent e) {
                if (selectedViewHolder != viewHolder) {
                    setBackground(viewHolder, Colors.ITEM_SELECTED_DARK);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (selectedViewHolder != viewHolder) {
                    setBackground(viewHolder, Colors.DARK);
                }
            }
        });
    }

    /**
     * 根据房间id获取群成员
     *
     * @param roomId 房间id 目前以用户的UserName做的id
     * @return 群成员
     */
    private String[] getRoomMembers(String roomId) {
        Contacts contacts = Core.getMemberMap().get(roomId);
        List<Contacts> memberList = contacts.getMemberlist();
        //String members = room.getMember();
        List<String> roomMembers = new ArrayList<>();
        String[] memberArr = null;
        for (Object o1 : memberList) {
            JSONObject meber = (JSONObject) o1;
            roomMembers.add(meber.getString("NickName"));
        }

      /*  if (members != null)
        {
            String[] userArr = members.split(",");
            for (int i = 0; i < userArr.length; i++)
            {
                if (!roomMembers.contains(userArr[i]))
                {
                    roomMembers.add(userArr[i]);
                }
            }
        }
        String creator = room.getCreatorName();
        if (creator != null)
        {
            if (!roomMembers.equals(creator))
            {
                roomMembers.add(creator);
            }
        }*/

        memberArr = roomMembers.toArray(new String[]{});
        return memberArr;
    }

    private void setBackground(RoomItemViewHolder holder, Color color) {
        holder.setBackground(color);
        holder.nameBrief.setBackground(color);
        holder.timeUnread.setBackground(color);
    }

    /**
     * 进入房间
     *
     * @param roomId 房间id
     */
    private void enterRoom(String roomId) {
        // 加载房间消息
         //ChatPanel.getContext().enterRoom(roomId);

        //切换显示层
        RoomChatPanelCard roomChatPanelCard = RoomChatPanel.getContext().createAndShow(roomId);
        RoomChatPanel.getContext().show(roomId);
        //显示聊天界面
        roomChatPanelCard.showPanel(RoomChatPanelCard.MESSAGE);
        //更新聊天列表未读数量
        RoomsPanel.getContext().updateUnreadCount(roomId,0);

        //RightPanelParent.getContext().show(roomId);
        //  rightPanel.getChatPanel().enterRoom(roomId);
        //TitlePanel.getContext().hideRoomMembersPanel();
        /*RoomMembersPanel.getContext().setRoomId(roomId);
        if (RoomMembersPanel.getContext().isVisible())
        {
            RoomMembersPanel.getContext().updateUI();
        }*/
    }

}
