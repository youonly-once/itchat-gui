package cn.shu.wechat.swing.adapter.message.app;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.VerticalFlowLayout;
import cn.shu.wechat.swing.components.message.RCLeftImageMessageBubble;
import cn.shu.wechat.utils.FontUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created by 舒新胜 on 17-6-2.
 */
public class MessageLeftAttachmentViewHolder extends MessageAttachmentViewHolder {

    private boolean isGroup = true;

    public MessageLeftAttachmentViewHolder(boolean isGroup) {
        this.isGroup = isGroup;
        initComponents();
        initView();
    }

    private void initComponents() {
        messageBubble = new RCLeftImageMessageBubble();

        /*timePanel.setBackground(Colors.WINDOW_BACKGROUND);
        messageAvatarPanel.setBackground(Colors.WINDOW_BACKGROUND);

        size.setForeground(Colors.FONT_GRAY);
        size.setFont(FontUtil.getDefaultFont(12));*/


        sender.setFont(FontUtil.getDefaultFont(12));
        sender.setForeground(Colors.FONT_GRAY);
        //sender.setVisible(false);

        /*attachmentPanel.setOpaque(false);

        progressBar.setMaximum(100);
        progressBar.setMinimum(0);
        progressBar.setValue(100);
        progressBar.setUI(new GradientProgressBarUI());
        progressBar.setVisible(false);

        messageBubble.setCursor(new Cursor(Cursor.HAND_CURSOR));

        sizeLabel.setFont(FontUtil.getDefaultFont(12));
        sizeLabel.setForeground(Colors.FONT_GRAY);*/

        messageBubble.setCursor(new Cursor(Cursor.HAND_CURSOR));


    }

    private void initView() {
        setLayout(new BorderLayout());

        timePanel.add(time);

        attachmentPanel.setLayout(new GridBagLayout());
        attachmentPanel.add(attachmentIcon, new GBC(0, 0).setWeight(1, 1).setInsets(2, 5, 0, 0)
                .setGridHeight(2));
        attachmentPanel.add(attachmentTitle, new GBC(1, 0).setWeight(100, 1).setAnchor(GBC.NORTH)
                .setInsets(2, 5, 0, 5));

        attachmentPanel.add(sizeLabel, new GBC(1, 1).setWeight(1, 1)
                .setFill(GBC.HORIZONTAL).setAnchor(GBC.SOUTH)
                .setInsets(2, 8, 2, 5));

        messageBubble.add(attachmentPanel);


        JPanel senderMessagePanel = new JPanel();
        senderMessagePanel.setBackground(Colors.WINDOW_BACKGROUND);
        senderMessagePanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));

        if (isGroup) {
            senderMessagePanel.add(sender);
        }
        JPanel processBarPanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, true));
        processBarPanel.setOpaque(false);
        // processBarPanel.add(progressBar);
        progressBar.setBorder(new EmptyBorder(0, messageBubble.getSalientPointPixel(), 0, 0));
        processBarPanel.add(messageBubble);
        processBarPanel.add(progressBar);


        senderMessagePanel.add(processBarPanel);
        senderMessagePanel.add(revoke);
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
