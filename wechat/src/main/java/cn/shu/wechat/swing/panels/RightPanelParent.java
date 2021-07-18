package cn.shu.wechat.swing.panels;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 7/18/2021 11:40
 */
public class RightPanelParent extends JPanel {
    private String roomId;
    private CardLayout cardLayout;
    private Map<String,RightPanel> cards = new HashMap<>();

    public RightPanelParent(String roomId) {
        super();
        this.roomId = roomId;
        init();
    }
    private void init(){
        cardLayout = new CardLayout();
        this.setLayout(cardLayout);

    }

    /**
     * 添加层
     * @param roomId 房间id
     */
    public void addPanel(String roomId){
        RightPanel rightPanel = new RightPanel();
        add(rightPanel,roomId);
        cards.put(roomId,rightPanel);
    }


    /**
     * 显示对应层
     * @param roomId 房间id
     */
    public void show(String roomId){
        cardLayout.show(this,roomId);
    }

    /**
     * 获取对应层
     * @param roomId roomId
     */
    public RightPanel get(String roomId){
        return cards.get(roomId);
    }

}
