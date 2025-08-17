package cn.shu.wechat.swing.adapter.room;

import cn.shu.wechat.swing.adapter.ViewHolder;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.utils.FontUtil;

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
        setBackground(Color.WHITE);
        // setBorder(new RCBorder(RCBorder.BOTTOM, new Color(235, 235, 235)));
        setOpaque(true);
        setLayout(new BorderLayout(0, 0));

        // 设置 roomName 样式
        roomName.setFont(FontUtil.getDefaultFont(12));
        roomName.setForeground(Colors.FONT_BLACK);
        roomName.setHorizontalAlignment(SwingConstants.CENTER);

        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setVerticalAlignment(SwingConstants.CENTER);
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        roomName.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(avatar, BorderLayout.CENTER);
        add(roomName, BorderLayout.SOUTH);

    }
}
