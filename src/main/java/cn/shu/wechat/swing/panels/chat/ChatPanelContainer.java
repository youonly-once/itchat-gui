package cn.shu.wechat.swing.panels.chat;

import cn.shu.wechat.swing.panels.ParentAvailablePanel;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.*;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 聊天房容器
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 7/18/2021 11:40
 */
public class ChatPanelContainer extends ParentAvailablePanel {
    private static final Logger log = LogManager.getLogger(ChatPanelContainer.class);
    private CardLayout cardLayout;

    private final static LinkedHashMap<String, ChatPanel> cards = new LinkedHashMap<>(5);

    @Getter
    private static String currRoomId;

    @Getter
    private static ChatPanelContainer context;
    public ChatPanelContainer(JPanel parent) {
        super(parent);
        context = this;
        init();
        createAndShow("filehelper");
    }
    private void init(){
        cardLayout = new CardLayout();
        this.setLayout(cardLayout);

        java.util.Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (ChatPanel value : cards.values()) {
                    value.clearMsgItem();
                }

            }
        }, 1000 * 60 * 10, 1000 * 60 * 20);

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
        if (roomId.equals(currRoomId)) return;
        //移出之前的Panel
        if (currRoomId != null) {
            removeCard(currRoomId);
        }


        cards.get(roomId).getChatMessagePanel().getChatMessageEditorPanel().addShareComponent();
        currRoomId = roomId;

        cardLayout.show(this,roomId);
        this.revalidate();
        this.repaint();
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
        if (isCurrentRoom(roomId)) {
            currRoomId = null;
        }
        if (!cards.containsKey(roomId)){

            ChatMessageEditorPanel.removeShareComponent();
            return;
        }
        ChatPanel remove = cards.remove(roomId);
        //先循环移除监听器 否则removeShareComponent后 shareTextEditor为null 他的监听器无法移除
        removeAllListenersRecursively(remove);
        ChatMessageEditorPanel.removeShareComponent();


        this.remove(remove);
        this.revalidate();
        this.repaint();
    }

    public void removeAllListenersRecursively(Component comp) {
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                removeAllListenersRecursively(child);
            }
        }

        // 示例：移除常见的几种监听器（可扩展）
        if (comp instanceof JComponent jc  ) {
            for (MouseListener ml : jc.getMouseListeners()) {
                jc.removeMouseListener(ml);
            }
            for (KeyListener kl : jc.getKeyListeners()) {
                jc.removeKeyListener(kl);
            }
            for (FocusListener fl : jc.getFocusListeners()) {
                jc.removeFocusListener(fl);
            }

            for (MouseMotionListener fl : jc.getMouseMotionListeners()) {
                jc.removeMouseMotionListener(fl);
            }

            for (AncestorListener fl : jc.getAncestorListeners()) {
                jc.removeAncestorListener(fl);
            }

            for (ComponentListener fl : jc.getComponentListeners()) {
                jc.removeComponentListener(fl);
            }

            // ... 其他类型监听器根据需要添加
        }
        if (comp instanceof JTextPane jc) {

            for (CaretListener fl : jc.getCaretListeners()) {
                jc.removeCaretListener(fl);
            }

        }
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

    public boolean isCurrentRoom(String roomId) {
        return roomId.equals(getCurrRoomId());
    }
}
