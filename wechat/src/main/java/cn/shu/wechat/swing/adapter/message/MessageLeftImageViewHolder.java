package cn.shu.wechat.swing.adapter.message;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.VerticalFlowLayout;
import cn.shu.wechat.swing.components.message.MessageImageLabel;
import cn.shu.wechat.swing.components.message.RCLeftImageMessageBubble;
import cn.shu.wechat.swing.utils.FontUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Created by song on 17-6-2.
 */
public class MessageLeftImageViewHolder extends BaseMessageViewHolder {
    public JLabel sender = new JLabel();
    //public JLabel avatar = new JLabel();
    //public JLabel size = new JLabel();
    public MessageImageLabel image = new MessageImageLabel();
    public RCLeftImageMessageBubble imageBubble = new RCLeftImageMessageBubble();
    private JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    private JPanel messageAvatarPanel = new JPanel();
    private boolean isGroup = true;

    public MessageLeftImageViewHolder(boolean isGroup) {
        this.isGroup = isGroup;
        initComponents();
        initView();
    }

    private void initComponents() {
        timePanel.setBackground(Colors.WINDOW_BACKGROUND);
        messageAvatarPanel.setBackground(Colors.WINDOW_BACKGROUND);


       // imageBubble.add(image);

        time.setForeground(Colors.FONT_GRAY);
        time.setFont(FontUtil.getDefaultFont(12));

        sender.setFont(FontUtil.getDefaultFont(12));
        sender.setForeground(Colors.FONT_GRAY);
        //sender.setVisible(false);
    }

    private void initView() {
        setLayout(new BorderLayout());
        timePanel.add(time);

        JPanel senderMessagePanel = new JPanel();
        senderMessagePanel.setBackground(Colors.WINDOW_BACKGROUND);
        senderMessagePanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));
        if (isGroup) {
            senderMessagePanel.add(sender);
        }

        senderMessagePanel.add(image);

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
