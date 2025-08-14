package cn.shu.wechat.swing.adapter.message.app.link;

import cn.shu.wechat.swing.adapter.message.app.MessageAppViewHolder;
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

    public static final int THUMB_WIDTH = 100;
    public static final int THUMB_HEIGHT = 32;
    /**
     * 链接预览图
     */
    public JLabel icon;

    /**
     * 描述
     */
    public JTextArea desc ;

    public MessageLinkOfAppViewHolder() {
        super();

    }

    protected void initComponents() {
        super.initComponents();
        desc = new JTextArea();
        icon = new JLabel();
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
    protected void setListeners() {
        super.setListeners();
        MouseAdapter listener = messageBubble.getMouseListener();
        desc.addMouseListener(listener);
        icon.addMouseListener(listener);
    }
    protected void initView() {
        super.initView();
        setLayout(new GridBagLayout());
    }
}
