package cn.shu.wechat.swing.adapter.message.image;

import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.VerticalFlowLayout;
import cn.shu.wechat.swing.components.message.MessageImageLabel;
import cn.shu.wechat.swing.components.message.RCLeftImageMessageBubble;
import cn.shu.wechat.utils.FontUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created by 舒新胜 on 17-6-2.
 */
public class MessageLeftImageViewHolder extends BaseMessageViewHolder {
    //public JLabel avatar = new JLabel();
    //public JLabel size = new JLabel();
    public MessageImageLabel image = new MessageImageLabel();
    public RCLeftImageMessageBubble imageBubble = new RCLeftImageMessageBubble();
    private JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    private JPanel messageAvatarPanel = new JPanel();
    private boolean isGroup = true;
    private final Dimension imgSize;

    public MessageLeftImageViewHolder(boolean isGroup,Dimension imgSize) {
        this.isGroup = isGroup;
        this.imgSize = imgSize;
        initComponents();
        initView();
    }

    private void initComponents() {
        timePanel.setBackground(Colors.WINDOW_BACKGROUND);
        messageAvatarPanel.setBackground(Colors.WINDOW_BACKGROUND);

        time.setForeground(Colors.FONT_GRAY);
        time.setFont(FontUtil.getDefaultFont(12));

    }

    private void initView() {
        setLayout(new BorderLayout());
        timePanel.add(time);
        // 设置图标水平居中
        image.setHorizontalAlignment(SwingConstants.CENTER);

        // 设置图标垂直居中
        image.setVerticalAlignment(SwingConstants.CENTER);
        if (imgSize != null) {
            image.setPreferredSize(imgSize);
        }
        JPanel senderMessagePanel = new JPanel();
        senderMessagePanel.setBackground(Colors.WINDOW_BACKGROUND);
        senderMessagePanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));
        if (isGroup) {
            sender.setFont(FontUtil.getDefaultFont(12));
            sender.setForeground(Colors.FONT_GRAY);
            sender.setBorder(new EmptyBorder(0,0,5,0));
            senderMessagePanel.add(sender);
        }
        JPanel controlPanel = new JPanel(new BorderLayout(0, 0));
        controlPanel.add(image,BorderLayout.CENTER);
        controlPanel.add(revoke,BorderLayout.EAST);
        senderMessagePanel.add(controlPanel);
        messageAvatarPanel.setLayout(new GridBagLayout());
        messageAvatarPanel.add(avatar, new GBC(1, 0).setWeight(1, 1).setAnchor(GBC.NORTH).setInsets(0, 5, 0, 0));
        messageAvatarPanel.add(senderMessagePanel, new GBC(2, 0)
                .setWeight(1000, 1)
                .setAnchor(GBC.WEST)
                .setInsets(0, 5, 0, 0));
        add(timePanel, BorderLayout.NORTH);
        add(messageAvatarPanel, BorderLayout.CENTER);
    }
}
