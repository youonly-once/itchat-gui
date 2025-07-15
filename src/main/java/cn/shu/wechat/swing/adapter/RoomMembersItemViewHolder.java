package cn.shu.wechat.swing.adapter;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.RCBorder;
import cn.shu.wechat.swing.utils.FontUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Created by 舒新胜 on 07/06/2017.
 */
public class RoomMembersItemViewHolder extends ViewHolder {
    public JLabel avatar = new JLabel();
    public JLabel roomName = new JLabel();

    public RoomMembersItemViewHolder() {
        initView();
    }

    private void initView() {
        setPreferredSize(new Dimension(40, 70));
        setBackground(Colors.WINDOW_BACKGROUND_LIGHT);
        setBorder(new RCBorder(RCBorder.BOTTOM, new Color(235, 235, 235)));
        setOpaque(true);

        // 设置 roomName 样式
        roomName.setFont(FontUtil.getDefaultFont(12));
        roomName.setForeground(Colors.FONT_BLACK);
        roomName.setHorizontalAlignment(SwingConstants.CENTER);

        // 设置 layout
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));

        // 创建垂直排列的头像+名称面板
        JPanel avatarPanel = new JPanel();
        avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.Y_AXIS));
        avatarPanel.setOpaque(false); // 保持透明
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        roomName.setAlignmentX(Component.CENTER_ALIGNMENT);

        avatarPanel.add(avatar);
        avatarPanel.add(Box.createVerticalStrut(3)); // 间距
        avatarPanel.add(roomName);

        add(avatarPanel);
    }
}
