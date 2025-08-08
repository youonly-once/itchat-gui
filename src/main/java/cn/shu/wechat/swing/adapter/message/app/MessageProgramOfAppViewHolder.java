package cn.shu.wechat.swing.adapter.message.app;

import cn.shu.wechat.swing.components.message.RCAttachmentMessageBubble;

import javax.swing.*;
import java.awt.*;

/**
 * App 消息
 * @author 舒新胜
 * @date 2021-8-2
 */

public abstract class MessageProgramOfAppViewHolder extends MessageAppViewHolder {
    public final static int maxWidth = 200;
    public final static int maxHeight = 200;
    /**
     * 小程序消息的图片
     */
    public final JLabel imageLabel = new JLabel();
    private final Dimension imgSize;

    public MessageProgramOfAppViewHolder(RCAttachmentMessageBubble messageBubble, Dimension imgSize) {
        super(messageBubble);
        this.imgSize = imgSize;
        initComponents();
        initView();
    }

    private void initComponents() {
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setPreferredSize(imgSize);
        contentPanel.setPreferredSize(imgSize);
        contentPanel.add(imageLabel);

    }

    private void initView() {

    }
}
