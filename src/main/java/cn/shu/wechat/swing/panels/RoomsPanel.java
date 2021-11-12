package cn.shu.wechat.swing.panels;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.pojo.entity.Contacts;
import cn.shu.wechat.swing.adapter.RoomItemViewHolder;
import cn.shu.wechat.swing.adapter.RoomItemsAdapter;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCListView;
import cn.shu.wechat.swing.db.model.Room;
import cn.shu.wechat.swing.entity.RoomItem;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.utils.ExecutorServiceUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 左侧聊天列表
 * Created by 舒新胜 on 17-5-30.
 */
public class RoomsPanel extends ParentAvailablePanel {
    private static RoomsPanel context;


    /**
     * 未读消息总数
     */
    private static final AtomicInteger UNREAD_TOTAL_COUNT = new AtomicInteger(0);
    /**
     * 聊天列表视图数据
     */
    private RCListView roomItemsListView;

    /**
     * 当前聊天列表
     */
    private final List<RoomItem> roomItemList = new ArrayList<>();


    public RoomsPanel(JPanel parent) {
        super(parent);
        context = this;

        initComponents();
        initView();
        initData();
        roomItemsListView.setAdapter(new RoomItemsAdapter(roomItemList));
    }


    /**
     * 消息已读数量
     * @param count 本次已读
     */
    public static  void updateUnreadTotalCount(int count){
        if (count == 0){
            return;
        }
        synchronized (RoomsPanel.class) {
            int i = UNREAD_TOTAL_COUNT.addAndGet(count);
            if (i<0){
                UNREAD_TOTAL_COUNT.set(0);
            }
            if (i > 0) {
                TabOperationPanel.getContext().getChatLabel().setCornerText(String.valueOf(i));
                TabOperationPanel.getContext().repaint();
            } else if (UNREAD_TOTAL_COUNT.get() == 0) {
                MainFrame.getContext().setTrayFlashing(false);
                TabOperationPanel.getContext().getChatLabel().setCornerText("");
                TabOperationPanel.getContext().repaint();
                ;

            }
        }

    }

    private void initComponents() {
        roomItemsListView = new RCListView();
        roomItemsListView.getVerticalScrollBar().setUnitIncrement(RoomItemViewHolder.HEIGHT/3);
    }

    private void initView() {
        setLayout(new GridBagLayout());
        roomItemsListView.setContentPanelBackground(Colors.DARK);
        add(roomItemsListView, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 1));
        //add(scrollPane, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 1));
    }

    private void initData() {
        roomItemList.clear();
        //从核心类加载房间列表
/*        Set<Contacts> recentContacts = Core.getRecentContacts();
        for (Contacts recentContact : recentContacts) {
            RoomItem item = new RoomItem();
            item.setRoomId(recentContact.getUsername());
            item.setTimestamp(System.currentTimeMillis());
            item.setName(ContactsTools.getContactDisplayNameByUserName(recentContact.getUsername()));
            item.setLastMessage("");
            item.setUnreadCount(0);
            item.setGroup(recentContact.getUsername().startsWith("@@"));
            item.setHeadImgPath(recentContact.getHeadimgurl());
            item.setRefreshHead(true);
            roomItemList.add(item);
        }*/
    }
    /**
     * 进入房间
     *
     * @param roomId 房间id
     */
    public void enterRoom(String roomId) {
        //切换显示层
        RoomChatPanelCard roomChatPanelCard = RoomChatContainer.getContext().createAndShow(roomId);
        RoomChatContainer.getContext().show(roomId);
        //更新聊天列表未读数量
        hasReadCount(roomId);
        //发送消息已读通知
        ExecutorServiceUtil.getGlobalExecutorService().execute(() -> MessageTools.sendStatusNotify(roomId));
    }
    /**
     * 添加房间
     * @param roomId 房间id
     * @param newReadCount 新消息数量
     * @param latestMsg 最近的一条消息
     * @param hasNewMsg 是否有未读消息 ，当房间为免打扰房间时newReadCount不计数，此时通过hasNewMsg判断
     */
    private void addRoom(String roomId, String latestMsg, int newReadCount,Boolean hasNewMsg) {
        Contacts contacts = Core.getMemberMap().get(roomId);
        addRoom(new RoomItem(contacts, latestMsg, newReadCount,hasNewMsg));
    }
    /**
     * 添加房间
     *
     * @param roomId 联系人ID
     */
    public void addRoom(String roomId) {
        addRoom(roomId, "", 0,false);
    }
    /**
     * 添加房间
     * @param item 房间Item
     */
    private void addRoom(RoomItem item) {
        roomItemList.add(0, item);
        roomItemsListView.notifyDataSetChanged(false);
        roomItemsListView.scrollToPosition(0);
    }

    /**
     * 添加房间
     * @param roomId 房间id
     * @param newReadCount 新消息数量
     * @param latestMsg 最近的一条消息
     * @param isMute 是否免打扰
     * @param hasNewMsg 是否有未读消息 ，当房间为免打扰房间时newReadCount不计数，此时通过hasNewMsg判断
     */
    public void addRoomOrOpenRoom(String roomId, String latestMsg, int newReadCount, Boolean isMute, boolean hasNewMsg) {

        //更新聊天列表
        Set<String> recentContacts = Core.getRecentContacts();
        if (!recentContacts.contains(roomId)) {
            //添加新房间并制定
            addRoom(roomId, latestMsg, newReadCount,hasNewMsg);
            recentContacts.add(roomId);
        } else {
            //更新消息 置顶
           updateRoomItem(roomId, newReadCount, latestMsg, System.currentTimeMillis(),isMute,hasNewMsg);
        }
    }


    /**
     * 批量添加房间
     * @param items 房间列表
     */
    public void addRoom(List<RoomItem> items) {
        roomItemList.addAll(items);
        roomItemsListView.notifyDataSetChanged(false);
    }


    /**
     * 重绘整个列表
     */
    public void notifyDataSetChanged(boolean keepSize) {
        initData();
        roomItemsListView.notifyDataSetChanged(keepSize);
    }


    /**
     * 更新房间未读消息数
     *
     * @param roomId      房间id
     * @param newReadCount 新消息数量
     */
    public void updateUnreadCount(String roomId, int newReadCount) {
        for (int i = 0; i < roomItemList.size(); i++) {
            RoomItem item = roomItemList.get(i);
            if (item.getRoomId().equals(roomId)) {
                //找到对应房间
                if (newReadCount>0) {
                    item.setUnreadCount(item.getUnreadCount() + newReadCount);
                }
                roomItemsListView.notifyItemChanged(i);
                break;
            }
        }
    }

    /**
     * 更新房间消息为已读
     *
     * @param roomId  房间id
     */
    public void hasReadCount(String roomId) {
        for (int i = 0; i < roomItemList.size(); i++) {
            RoomItem item = roomItemList.get(i);
            if (item.getRoomId().equals(roomId)) {
                updateUnreadTotalCount(-item.getUnreadCount());
                item.setUnreadCount(0);
                item.setHasNewMsg(false);
                roomItemsListView.notifyItemChanged(i);
                break;
            }
        }
    }

    /**
     * 更新指定房间信息
     * @param roomId 房间id
     * @param newReadCount 新消息数量
     * @param lastMsg 最近的一条消息
     * @param time 时间
     * @param isMute 是否免打扰
     * @param hasNewMsg 是否有未读消息 ，当房间为免打扰房间时newReadCount不计数，此时通过hasNewMsg判断
     */
    public void updateRoomItem(String roomId, int newReadCount, String lastMsg, Long time,Boolean isMute,Boolean hasNewMsg) {
        if (roomId == null || roomId.isEmpty()) {
            notifyDataSetChanged(true);
            return;
        }

        for (int i = 0; i < roomItemList.size(); i++) {
            RoomItem item = roomItemList.get(i);
            if (item.getRoomId().equals(roomId)) {
                //找到对应房间
                if (isMute != null) {
                    item.setMute(isMute);
                }
                if (lastMsg != null) {
                    item.setLastMessage(lastMsg);
                }
                if (time != null) {
                    item.setTimestamp(time);
                }
                 if (newReadCount>0) {
                    item.setUnreadCount(item.getUnreadCount() + newReadCount);
                }
                 if(hasNewMsg!=null){
                     item.setHasNewMsg(hasNewMsg);
                 }
                //最新消息移到首行
                if (i != 0) {
                    roomItemList.add(0, roomItemList.remove(i));
                    //重绘整个列表
                    roomItemsListView.notifyDataSetChanged(true);

                } else {
                    //当前消息位于首行，则无需重绘整个列表
                    roomItemsListView.notifyItemChanged(0);
                }
                return;
            }
        }

    }

    /**
     * 激活指定的房间项目
     *
     * @param position 房间位置
     */
    public void activeItem(int position) {
        RoomItemViewHolder holder = (RoomItemViewHolder) roomItemsListView.getItem(position);
        setItemBackground(holder, Colors.ITEM_SELECTED);
        RoomItemsAdapter adapter = (RoomItemsAdapter) (roomItemsListView.getAdapter());
        adapter.setSelectedViewHolder(holder);
        for (int i = 0; i < roomItemsListView.getItems().size(); i++) {
            if (i == position) {
                continue;
            }
            holder = (RoomItemViewHolder) roomItemsListView.getItem(i);
            setItemBackground(holder, Colors.DARK);
        }
        scrollToPosition(position*RoomItemViewHolder.HEIGHT);
    }

    /**
     * 激活指定的房间项目
     *
     * @param name 房间ID
     */
    public void activeItem(String name) {
        for (int i = 0; i < roomItemList.size(); i++) {
            RoomItem roomItem = roomItemList.get(i);
            if (roomItem.getRoomId().equals(name)) {
                activeItem(i);
                return;
            }
        }

    }


    /**
     * 设置房间的背影色
     *
     * @param holder 房间
     * @param color 背景色
     */
    private void setItemBackground(RoomItemViewHolder holder, Color color) {
        holder.setBackground(color);
        holder.nameBrief.setBackground(color);
        holder.timeUnread.setBackground(color);
    }

    public void scrollPoint(int point){
        roomItemsListView.getVerticalScrollBar().setValue(point);
    }

    public void scrollToPosition(int point){
        roomItemsListView.scrollToPosition(point);
    }

    public static RoomsPanel getContext() {
        return context;
    }

}
