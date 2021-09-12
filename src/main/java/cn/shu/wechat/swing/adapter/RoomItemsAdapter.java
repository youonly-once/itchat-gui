package cn.shu.wechat.swing.adapter;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.entity.RoomItem;
import cn.shu.wechat.swing.listener.AbstractMouseListener;
import cn.shu.wechat.swing.panels.RoomChatContainer;
import cn.shu.wechat.swing.panels.RoomsPanel;
import cn.shu.wechat.swing.utils.AvatarUtil;
import cn.shu.wechat.swing.utils.TimeUtil;

import javax.swing.*;
import java.awt.*;
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

    public void setSelectedViewHolder(RoomItemViewHolder selectedViewHolder) {
        this.selectedViewHolder = selectedViewHolder;
    }

    /**
     * 当前选中的viewHolder
     */
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
        new SwingWorker<Object,Object>(){
            ImageIcon orLoadAvatar = null;
            @Override
            protected Object doInBackground() throws Exception {
                orLoadAvatar = AvatarUtil.createOrLoadUserAvatar(roomItem.getRoomId());
                return null;
            }

            @Override
            protected void done() {
                viewHolder.avatar.setIcon(orLoadAvatar);
            }
        }.execute();
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
        if (roomItem.getRoomId().equals(RoomChatContainer.getCurrRoomId())) {
            setBackground(viewHolder, Colors.ITEM_SELECTED);
            selectedViewHolder = viewHolder;
        }else{
            setBackground(viewHolder, Colors.DARK);
        }

        //更新鼠标监听器
        if (viewHolder.mouseListener != null){
            viewHolder.mouseListener.setMyHolder(viewHolder);
            viewHolder.mouseListener.setMyRoomId(roomItem.getRoomId());
        }else{
            viewHolder.mouseListener = new RoomItemAbstractMouseListener(viewHolder,roomItem.getRoomId());;
            viewHolder.addMouseListener(viewHolder.mouseListener);
        }

    }


    class RoomItemAbstractMouseListener extends AbstractMouseListener{

        public RoomItemAbstractMouseListener(RoomItemViewHolder myHolder, String myRoomId) {
            this.myHolder = myHolder;
            this.myRoomId = myRoomId;
        }

        public void setMyHolder(RoomItemViewHolder myHolder) {
            this.myHolder = myHolder;
        }

        public void setMyRoomId(String myRoomId) {
            this.myRoomId = myRoomId;
        }

        private RoomItemViewHolder myHolder ;
        private String myRoomId ;

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (selectedViewHolder != myHolder) {
                    //之前选择的房间背景色去掉
                    setBackground(selectedViewHolder, Colors.DARK);
                    // 进入房间
                    RoomsPanel.getContext().enterRoom(myRoomId);
                    selectedViewHolder = myHolder;
                }
            }
        }


        @Override
        public void mouseEntered(MouseEvent e) {
            if (selectedViewHolder != myHolder) {
                setBackground(myHolder, Colors.ITEM_SELECTED_DARK);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (selectedViewHolder != myHolder) {
                setBackground(myHolder, Colors.DARK);
            }
        }
    };

    private void setBackground(RoomItemViewHolder holder, Color color) {
        holder.setBackground(color);
        holder.nameBrief.setBackground(color);
        holder.timeUnread.setBackground(color);
    }



}
