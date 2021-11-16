package cn.shu.wechat.swing.panels;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.panels.chat.ChatPanelContainer;
import lombok.Getter;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * 右半部分
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 7/18/2021 15:39
 */
@Getter
public class RightPanel extends JPanel {
    /**
     * 显示聊天房的panel
     */
    private final ChatPanelContainer chatPanelContainer;
    /**
     * 显示用户信息的panel
     */
    private final UserInfoPanel userInfoPanel;

    public static final String CHAT_ROOM = "CHAT_ROOM";
    public static final String USER_INFO = "USER_INFO";

    public static RightPanel getContext() {
        return context;
    }

    private static RightPanel context;
    /**
     * 布局
     */
    private final CardLayout cardLayout;
    public RightPanel() {
        context =this;
        userInfoPanel = new UserInfoPanel(this);
        chatPanelContainer = new ChatPanelContainer(this);
        cardLayout = new CardLayout();
        setBorder(new LineBorder(Colors.SCROLL_BAR_TRACK_LIGHT));
        setLayout(cardLayout);
        add(userInfoPanel, USER_INFO);
        add(chatPanelContainer, CHAT_ROOM);
        show(CHAT_ROOM);
    }

    /**
     * 显示对应层
     * @param name
     */
    public void show(String name){
        cardLayout.show(this,name);
    }
}
