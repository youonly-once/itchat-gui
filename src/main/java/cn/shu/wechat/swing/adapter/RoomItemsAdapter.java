package cn.shu.wechat.swing.adapter;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.entity.RoomItem;
import cn.shu.wechat.swing.listener.AbstractMouseListener;
import cn.shu.wechat.swing.panels.chat.ChatPanelContainer;
import cn.shu.wechat.swing.panels.left.tabcontent.RoomsPanel;
import cn.shu.wechat.swing.utils.FontUtil;
import cn.shu.wechat.swing.utils.IconUtil;
import cn.shu.wechat.swing.utils.TimeUtil;
import cn.shu.wechat.swing.worker.HeadLoadingSwingWorker;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 舒新胜
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
    @Setter
    private RoomItemViewHolder selectedViewHolder;

    private final List<RoomItemViewHolder> viewHolders =new ArrayList<>();

    public RoomItemsAdapter(List<RoomItem> roomItems) {
        this.roomItems = roomItems;
    }

    @Override
    public int getCount() {
        return roomItems.size();
    }

    @Override
    public RoomItemViewHolder onCreateViewHolder(int viewType,int subViewType,  int position) {
        //避免重复创建
        RoomItemViewHolder roomItemViewHolder;
        if (viewHolders.size() > position){
            //存在
            roomItemViewHolder = viewHolders.get(position);
            if (roomItemViewHolder == null){
                roomItemViewHolder = new RoomItemViewHolder();
                viewHolders.set(position,roomItemViewHolder);
            }
        }else{
            roomItemViewHolder = new RoomItemViewHolder();
            viewHolders.add(position,roomItemViewHolder);
        }
        return roomItemViewHolder;
    }

    @Override
    public void onBindViewHolder(RoomItemViewHolder viewHolder, int position) {
        RoomItem roomItem = roomItems.get(position);
        viewHolder.setTag(roomItem.getRoomId());
        viewHolder.roomName.setText(roomItem.getName());
        new HeadLoadingSwingWorker(viewHolder.avatar,roomItem.getRoomId()).loadAvatar();

        //如果是 群 判断是否有人@我
        Contacts contacts = roomItem.getContacts();
        if (contacts!=null && ContactsTools.isRoomContact(contacts)){
            if ((StringUtils.isNotEmpty(Core.getUserSelf().getRemarkname()) && roomItem.getLastMessage().contains("@"+Core.getUserSelf().getRemarkname()))
                    || roomItem.getLastMessage().contains("@所有人")
            ||(StringUtils.isNotEmpty(Core.getNickName()) &&  roomItem.getLastMessage().contains("@"+Core.getNickName()))){
                viewHolder.atMe.setVisible(true);
                roomItem.setAtMe(true);
            }else {
                contacts.getMemberlist().stream().filter(e -> e.getUsername().equals(Core.getUserName())).findAny().ifPresent(e -> {
                    if (StringUtils.isNotEmpty(e.getDisplayname()) && roomItem.getLastMessage().contains( "@" + e.getDisplayname())) {
                        viewHolder.atMe.setVisible(true);
                        roomItem.setAtMe(true);
                    }
                });
            }
        }
        //如果没有点开房间查看消息 即使新消息来了 也要展示有人@我
        viewHolder.atMe.setVisible(roomItem.isAtMe());


        // 消息
        viewHolder.brief.setText(roomItem.getLastMessage());
        if (roomItem.getLastMessage() != null && roomItem.getLastMessage().length() > 15) {
            viewHolder.brief.setText(roomItem.getLastMessage().substring(0, 15) + "...");
        } else {
            viewHolder.brief.setText(roomItem.getLastMessage());
        }

        // 时间
        if (roomItem.getLocalDateTime() != null) {
            viewHolder.time.setText(TimeUtil.diff(roomItem.getLocalDateTime()));
        }
            // 未读消息数
            if (roomItem.getUnreadCount() > 0) {
                //消息免打扰
                if(roomItem.isMute()){
                    viewHolder. timeUnread.add(viewHolder.mutePoint,BorderLayout.CENTER);
                    viewHolder.mutePoint.setVisible(true);
                    viewHolder.unreadCount.setVisible(false);
                    viewHolder.unreadCount.setText( "");
                }else{
                    viewHolder. timeUnread.add(viewHolder.unreadCount,BorderLayout.CENTER);
                    viewHolder.mutePoint.setVisible(false);
                    viewHolder.unreadCount.setVisible(true);
                    viewHolder.unreadCount.setText(roomItem.getUnreadCount() + "");
                }

            } else if (roomItem.isHasNewMsg() && roomItem.isMute()){
                viewHolder. timeUnread.add(viewHolder.mutePoint,BorderLayout.CENTER);
                viewHolder.mutePoint.setVisible(true);
                viewHolder.unreadCount.setVisible(false);
                viewHolder.unreadCount.setText( "");
            }else{
                viewHolder.unreadCount.setVisible(false);
                viewHolder.mutePoint.setVisible(false);
                viewHolder.unreadCount.setText("");
            }

        // 设置是否激活
        if (roomItem.getRoomId().equals(ChatPanelContainer.getCurrRoomId())) {
            setBackground(viewHolder, Colors.SCROLL_BAR_TRACK_LIGHT);
            selectedViewHolder = viewHolder;
        }else{
            setBackground(viewHolder, Colors.WINDOW_BACKGROUND);
        }

        //更新鼠标监听器
        if (viewHolder.mouseListener != null){
            viewHolder.mouseListener.setMyHolder(viewHolder);
            viewHolder.mouseListener.setMyRoomId(roomItem.getRoomId());
            viewHolder.mouseListener.setPosition(position);
        }else{
            viewHolder.mouseListener = new RoomItemAbstractMouseListener(viewHolder,roomItem.getRoomId(),position);;
            viewHolder.addMouseListener(viewHolder.mouseListener);
        }

    }


    class RoomItemAbstractMouseListener extends AbstractMouseListener{
        private final JPopupMenu jPopupMenu = new JPopupMenu();

        public void setPosition(int position) {
            this.position = position;
        }

        private  int position;
        private RoomItemViewHolder myHolder ;
        private String myRoomId ;
        public RoomItemAbstractMouseListener(RoomItemViewHolder myHolder, String myRoomId,int pos) {
            this.myHolder = myHolder;
            this.myRoomId = myRoomId;
            this.position = pos;
            JMenuItem delItem = new JMenuItem("删除");
            delItem.setFont(FontUtil.getDefaultFont(14));
            delItem.setIcon(IconUtil.getIcon(this, "/image/delete.png"));
            delItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    RoomsPanel.getContext().removeItem(position,RoomItemAbstractMouseListener.this.myRoomId);
                }
            });
            jPopupMenu.add(delItem);
        }

        public void setMyHolder(RoomItemViewHolder myHolder) {
            this.myHolder = myHolder;
        }

        public void setMyRoomId(String myRoomId) {
            this.myRoomId = myRoomId;
        }



        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (selectedViewHolder != myHolder) {
                    //之前选择的房间背景色去掉
                    setBackground(selectedViewHolder, Colors.WINDOW_BACKGROUND);
                    // 进入房间
                    RoomsPanel.getContext().enterRoom(myRoomId);

                    selectedViewHolder = myHolder;
                }
            }else if(e.getButton() == MouseEvent.BUTTON3){
                jPopupMenu.show(e.getComponent(),e.getX(),e.getY());
            }
        }


        @Override
        public void mouseEntered(MouseEvent e) {
            if (selectedViewHolder != myHolder) {
                setBackground(myHolder, Colors.ITEM_SELECTED_LIGHT);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (selectedViewHolder != myHolder) {
                setBackground(myHolder, Colors.WINDOW_BACKGROUND);
            }
        }
    };
    private void setBackground(RoomItemViewHolder holder, Color color) {
        if (holder == null){
            //首次启动时 holder为null
            return;
        }
        holder.setBackground(color);
        holder.nameBrief.setBackground(color);
        holder.timeUnread.setBackground(color);
        holder.atAndBrief.setBackground(color);
    }



}
