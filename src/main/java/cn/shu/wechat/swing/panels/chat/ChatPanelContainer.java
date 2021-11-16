package cn.shu.wechat.swing.panels.chat;

import cn.shu.wechat.swing.panels.ParentAvailablePanel;
import cn.shu.wechat.swing.panels.TipPanel;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;

/**
 * 聊天房容器
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 7/18/2021 11:40
 */
public class ChatPanelContainer extends ParentAvailablePanel {
    private CardLayout cardLayout;

    private final static LinkedHashMap<String, ChatPanel> cards = new LinkedHashMap<>(5);
    public static String  getCurrRoomId() {
        return currRoomId;
    }

    private static String currRoomId;
    public static ChatPanelContainer getContext() {
        return context;
    }

    private static ChatPanelContainer context;
    public ChatPanelContainer(JPanel parent) {
        super(parent);
        context = this;
        init();
        TipPanel tipPanel = new TipPanel(this);
        tipPanel.setText("未选择聊天");
        this.add(tipPanel,"TIP");
        cardLayout.show(this,"TIP");

    }
    private void init(){
        cardLayout = new CardLayout();
        this.setLayout(cardLayout);
    }

    /**
     * 添加层
     * @param roomId 房间id
     */
    public ChatPanel addPanel(String roomId){
        if (cards.containsKey(roomId)){
            return cards.get(roomId);
        }
        ChatPanel rightPanel = new ChatPanel(roomId);
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
    public ChatPanel createAndShow(String roomId){
        if (!cards.containsKey(roomId)){
            addPanel(roomId);
        }
        show(roomId);
        return get(roomId);
    }

    /**
     * 删除聊天框
     * @param roomId 房间ID
     */
    public void removeCard(String roomId){
        if (!cards.containsKey(roomId)){
            return;
        }
        ChatPanel remove = cards.remove(roomId);
        remove(remove);
    }

    /**
     * 获取对应层
     * @param roomId roomId
     */
    public static ChatPanel get(String roomId){
        return cards.get(roomId);
    }

    /**
     * 获取对应层
     */
    public static ChatPanel getCurrRoom() {
        return cards.get(currRoomId);
    }


    /**
     * 房间是否存在
     */
    public boolean exists(String roomId){
        return cards.containsKey(roomId);
    }
}
