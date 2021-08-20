package cn.shu.wechat.swing.panels;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;

/**
 * 聊天房容器
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 7/18/2021 11:40
 */
public class RoomChatContainer extends ParentAvailablePanel {
    private CardLayout cardLayout;

    private final static LinkedHashMap<String, RoomChatPanelCard> cards = new LinkedHashMap<>(5);
    public String  getCurrRoomId() {
        return currRoomId;
    }

    private String currRoomId;
    public static RoomChatContainer getContext() {
        return context;
    }

    private static RoomChatContainer context;
    public RoomChatContainer(JPanel parent) {
        super(parent);
        context = this;
        init();
        createAndShow("filehelper");

    }
    private void init(){
        cardLayout = new CardLayout();
        this.setLayout(cardLayout);
    }

    /**
     * 添加层
     * @param roomId 房间id
     */
    public RoomChatPanelCard addPanel(String roomId){
        if (cards.containsKey(roomId)){
            return cards.get(roomId);
        }
        RoomChatPanelCard rightPanel = new RoomChatPanelCard(roomId);
        add(rightPanel,roomId);
        cards.put(roomId,rightPanel);
        return rightPanel;
    }


    /**
     * 显示对应层
     * @param roomId 房间id
     */
    public void show(String roomId){
        currRoomId = roomId;
        cardLayout.show(this,roomId);
    }

    /**
     * 创建显示
     * @param roomId 房间id
     */
    public RoomChatPanelCard createAndShow(String roomId){
        if (!cards.containsKey(roomId)){
            addPanel(roomId);
        }
        show(roomId);
        return get(roomId);
    }

    /**
     * 获取对应层
     * @param roomId roomId
     */
    public static RoomChatPanelCard get(String roomId){
        return context.cards.get(roomId);
    }

    /**
     * 获取对应层
     */
    public RoomChatPanelCard getCurr() {
        return cards.get(currRoomId);
    }


    /**
     * 房间是否存在
     */
    public boolean exists(String roomId){
        return cards.containsKey(roomId);
    }
}
