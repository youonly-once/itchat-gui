package cn.shu.wechat.swing.adapter.message.app.program;

import cn.shu.wechat.swing.adapter.message.app.MessageAppViewHolder;
import cn.shu.wechat.swing.components.message.RCAttachmentMessageBubble;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

/**
 * App 消息
 * @author 舒新胜
 * @date 2021-8-2
 */

public abstract class MessageProgramOfAppViewHolder extends MessageAppViewHolder {
    public final static int maxWidth = 250;
    public final static int maxHeight = 200;
    /**
     * 小程序消息的图片
     */
    public final JLabel imageLabel = new JLabel();
    private final Dimension imgSize;

    public MessageProgramOfAppViewHolder(RCAttachmentMessageBubble messageBubble, Dimension imgSize,String title) {
        super(messageBubble);
        this.imgSize = imgSize;
        //让图片宽度和标题一样
       // this.imgSize.setSize(this.title.getFontMetrics(this.getFont()).stringWidth(title), this.imgSize.height);
        initComponents();
        initView();
        setListeners();
    }

    private void initComponents() {
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        contentPanel.setLayout(new BorderLayout());
        imageLabel.setPreferredSize(imgSize);
        contentPanel.add(imageLabel,BorderLayout.CENTER);

    }

    private void setListeners() {
        MouseAdapter listener = messageBubble.getMouseListener();
        title.addMouseListener(listener);
        imageLabel.addMouseListener(listener);
        sourceIcon.addMouseListener(listener);
        sourceName.addMouseListener(listener);
        contentPanel.addMouseListener(listener);
    }

    private void initView() {

    }
}
