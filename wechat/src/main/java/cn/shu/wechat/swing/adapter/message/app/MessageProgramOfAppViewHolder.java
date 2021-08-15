package cn.shu.wechat.swing.adapter.message.app;

import cn.shu.wechat.swing.components.message.RCAttachmentMessageBubble;

import javax.swing.*;

/**
 * App 消息
 * @author 舒新胜
 * @date 2021-8-2
 */

public abstract class MessageProgramOfAppViewHolder extends MessageAppViewHolder {

    /**
     * 小程序消息的图片
     */
    public final JLabel imageLabel = new JLabel();

    public MessageProgramOfAppViewHolder(RCAttachmentMessageBubble messageBubble) {
        super(messageBubble);
        initComponents();
        initView();
    }

    private void initComponents() {
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        contentPanel.add(imageLabel);

    }

    private void initView() {

    }
}
