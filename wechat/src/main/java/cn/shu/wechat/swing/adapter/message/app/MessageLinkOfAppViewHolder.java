package cn.shu.wechat.swing.adapter.message.app;

import cn.shu.wechat.swing.components.message.RCAttachmentMessageBubble;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

/**
 * link of app
 * @author 舒新胜
 * @date 17-6-2
 */

public abstract class MessageLinkOfAppViewHolder extends MessageAppViewHolder {

    public static final int THUMB_WIDTH = 48;
    /**
     * 链接预览图
     */
    public final JLabel icon = new JLabel();

    /**
     * 描述
     */
    public final JTextArea desc = new JTextArea();
    public MessageLinkOfAppViewHolder(RCAttachmentMessageBubble messageBubble) {
        super(messageBubble);
        initComponents();
        initView();
        setListeners();
    }

    private void initComponents() {
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(desc,BorderLayout.CENTER);
        contentPanel.add(icon,BorderLayout.EAST);

        desc.setLineWrap(true);
        desc.setOpaque(false);
        desc.setEditable(false);
        desc.setWrapStyleWord(true);
        desc.setForeground(Color.GRAY);
        desc.setColumns(20);
        desc.setCursor(new Cursor(Cursor.HAND_CURSOR));

    }
    private void setListeners() {
        MouseAdapter listener = messageBubble.getMouseListener();
        contentTitlePanel.addMouseListener(listener);
        title.addMouseListener(listener);
        desc.addMouseListener(listener);
    }
    private void initView() {

    }
}
