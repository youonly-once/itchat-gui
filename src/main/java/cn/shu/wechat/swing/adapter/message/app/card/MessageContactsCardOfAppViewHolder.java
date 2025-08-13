package cn.shu.wechat.swing.adapter.message.app.card;

import cn.shu.wechat.swing.adapter.message.app.MessageAppViewHolder;
import cn.shu.wechat.swing.components.message.RCAttachmentMessageBubble;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

/**
 * ContactsCard of app
 * @author 舒新胜
 * @date 2021-09-29
 */

public abstract class MessageContactsCardOfAppViewHolder extends MessageAppViewHolder {
    public static final int THUMB_WIDTH = 60;
    public static final int THUMB_HEIGHT = 60;
    /**
     * 链接预览图
     */
    public final JLabel icon = new JLabel();

    /**
     * 描述
     */
    public final JTextArea desc = new JTextArea();
    public MessageContactsCardOfAppViewHolder(RCAttachmentMessageBubble messageBubble) {
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

        icon.setPreferredSize(new Dimension(THUMB_WIDTH, THUMB_HEIGHT));
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
        title.addMouseListener(listener);
        desc.addMouseListener(listener);
        icon.addMouseListener(listener);
        contentPanel.addMouseListener(listener);
    }
    private void initView() {

    }
}
