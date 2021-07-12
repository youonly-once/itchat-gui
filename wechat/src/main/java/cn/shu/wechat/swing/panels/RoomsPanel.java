package cn.shu.wechat.swing.panels;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.beans.pojo.Contacts;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.swing.adapter.RoomItemViewHolder;
import cn.shu.wechat.swing.adapter.RoomItemsAdapter;
import cn.shu.wechat.swing.app.Launcher;
import cn.shu.wechat.swing.components.*;
import cn.shu.wechat.swing.db.model.Room;
import cn.shu.wechat.swing.db.service.RoomService;
import cn.shu.wechat.swing.entity.RoomItem;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 左侧聊天列表
 * Created by song on 17-5-30.
 */
public class RoomsPanel extends ParentAvailablePanel
{
    private static RoomsPanel context;
    /**
     * 聊天列表视图数据
     */
    private RCListView roomItemsListView;

    /**
     * 当前聊天列表
     */
    private List<RoomItem> roomItemList = new ArrayList<>();



    public RoomsPanel(JPanel parent)
    {
        super(parent);
        context = this;

        initComponents();
        initView();
        initData();
        roomItemsListView.setAdapter(new RoomItemsAdapter(roomItemList));
    }

    private void initComponents()
    {
        roomItemsListView = new RCListView();
    }

    private void initView()
    {
        setLayout(new GridBagLayout());
        roomItemsListView.setContentPanelBackground(Colors.DARK);
        add(roomItemsListView, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 1));
        //add(scrollPane, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 1));
    }

    private void initData()
    {
        roomItemList.clear();

        // TODO: 从数据库中加载房间列表
        //从核心类加载房间列表
        List<Contacts> recentContacts = Core.getRecentContacts();
        for (Contacts recentContact : recentContacts) {
            RoomItem item = new RoomItem();
            item.setRoomId(recentContact.getUsername());
            item.setTimestamp(System.currentTimeMillis());
            item.setTitle(ContactsTools.getContactDisplayNameByUserName(recentContact.getUsername()));
            item.setType(recentContact.getUsername().startsWith("@@")?"c":"d");
            item.setLastMessage("");
            item.setUnreadCount(0);
            roomItemList.add(item);
        }
    }

    /**
     * 添加房间
     * @param recentContact 联系人
     * @param latestMsg 最新消息
     */
    public void addRoom(Contacts recentContact,String latestMsg){
        RoomItem item = new RoomItem();
        item.setRoomId(recentContact.getUsername());
        item.setTimestamp(System.currentTimeMillis());
        item.setTitle(StringUtils.isEmpty(recentContact.getRemarkname())?recentContact.getNickname():recentContact.getRemarkname());
        item.setType(recentContact.getUsername().startsWith("@@")?"c":"d");
        item.setLastMessage(latestMsg);
        item.setUnreadCount(1);
        roomItemList.add(0,item);
        roomItemsListView.notifyDataSetChanged(true);
    }
    /**
     * 重绘整个列表
     */
    public void notifyDataSetChanged(boolean keepSize)
    {
        initData();
        roomItemsListView.notifyDataSetChanged(keepSize);
    }

    /**
     * 更新房间列表
     * 当这条消息所在的房间在当前房间列表中排在第一位时，此时房间列表项目顺序不变，无需重新排列
     * 因此无需更新整个房间列表，只需更新第一个项目即可
     *
     * @param msgRoomId
     */
    public void updateRoomsList(String msgRoomId)
    {
        String roomId = (String) ((RoomItemViewHolder) (roomItemsListView.getItem(0))).getTag();
        if (roomId.equals(msgRoomId))
        {
            Room room = null;//roomService.findById(roomId);
            for (RoomItem roomItem : roomItemList)
            {
                if (roomItem.getRoomId().equals(roomId))
                {
                    roomItem.setUnreadCount(room.getUnreadCount());
                    roomItem.setTimestamp(room.getLastChatAt());
                    roomItem.setLastMessage(room.getLastMessage());
                    break;
                }
            }

            roomItemsListView.notifyItemChanged(0);
        }
        else
        {
            notifyDataSetChanged(false);
        }
    }

    /**
     * 更新指定位置的房间项目
     * @param roomId
     */
    public void updateRoomItem(String roomId,int unReadCount,String lastMsg,Long time)
    {
        if (roomId == null || roomId.isEmpty())
        {
            notifyDataSetChanged(true);
            return;
        }

        for (int i = 0; i < roomItemList.size(); i++)
        {
            RoomItem item = roomItemList.get(i);
            if (item.getRoomId().equals(roomId))
            {
                //Room room = roomService.findById(item.getRoomId());
               // if (room != null)
               // {
                    if (lastMsg!=null){
                        item.setLastMessage(lastMsg);
                    }
                    if (time!=null){
                        item.setTimestamp(time);
                    }


                    if (unReadCount == 0){
                        item.setUnreadCount(unReadCount);
                    }else if (unReadCount!=-1){
                        item.setUnreadCount(item.getUnreadCount()+unReadCount);
                    }

                    roomItemsListView.notifyItemChanged(i);
              //  }
                break;
            }
        }
    }

    /**
     * 激活指定的房间项目
     * @param position
     */
    public void activeItem(int position)
    {
        RoomItemViewHolder holder = (RoomItemViewHolder) roomItemsListView.getItem(position);
        setItemBackground(holder, Colors.ITEM_SELECTED);
    }

    /**
     * 设置每个房间项目的背影色
     * @param holder
     * @param color
     */
    private void setItemBackground(RoomItemViewHolder holder, Color color)
    {
        holder.setBackground(color);
        holder.nameBrief.setBackground(color);
        holder.timeUnread.setBackground(color);
    }



    public static RoomsPanel getContext()
    {
        return context;
    }
}
