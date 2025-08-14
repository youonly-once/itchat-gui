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
    public JLabel imageLabel;
    private final Dimension imgSize;

    public MessageProgramOfAppViewHolder(Dimension imgSize,String title) {
        super();
        this.imgSize = imgSize;
        //让图片宽度和标题一样
       // this.imgSize.setSize(this.title.getFontMetrics(this.getFont()).stringWidth(title), this.imgSize.height);

    }

    protected void initComponents() {
        super.initComponents();
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        contentPanel.setLayout(new BorderLayout());
        imageLabel.setPreferredSize(imgSize);
        contentPanel.add(imageLabel,BorderLayout.CENTER);

    }

    protected void setListeners() {
        super.setListeners();
        MouseAdapter listener = messageBubble.getMouseListener();
        imageLabel.addMouseListener(listener);
    }

    protected void initView() {
        super.initView();
    }
}
