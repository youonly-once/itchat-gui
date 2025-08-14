package cn.shu.wechat.swing.adapter.message.app;

import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.message.RCAttachmentMessageBubble;
import cn.shu.wechat.swing.components.message.TagPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;

/**
 * App 消息
 * @author 舒新胜
 * @date 2021-8-2
 */

public abstract class MessageAppViewHolder extends BaseMessageViewHolder {


    /**
     * APP消息标题
     */
    public final JTextArea title = new JTextArea();

    /**
     * 消息内容面板 由APP消息各类子类型实现
     */
    public final TagPanel contentPanel = new TagPanel(new BorderLayout());

    public RCAttachmentMessageBubble messageBubble;
    /**
     * APP名称
     */
    public final JLabel sourceName = new JLabel();
    /**
     * APP图标
     */
    public final JLabel sourceIcon = new JLabel();


    public MessageAppViewHolder() {

    }

    protected void initComponents() {
        contentPanel.setBorder(new EmptyBorder(5,5,5,5));
        contentPanel.setOpaque(false);

    }
    protected void setListeners() {
        MouseAdapter listener = messageBubble.getMouseListener();
        title.addMouseListener(listener);
        sourceName.addMouseListener(listener);
        contentPanel.addMouseListener(listener);
        sourceIcon.addMouseListener(listener);

    }
    protected void initView() {

        sourceName.setFont(new Font("楷体",Font.PLAIN,12));
        sourceName.setOpaque(false);
        sourceName.setForeground(Color.GRAY);


        title.setFont(new Font("楷体",Font.BOLD,18));
        title.setEditable(false);
        title.setOpaque(false);
        title.setLineWrap(true);
        title.setWrapStyleWord(true);
        title.setCursor(new Cursor(Cursor.HAND_CURSOR));

        messageBubble.setCursor(new Cursor(Cursor.HAND_CURSOR));
        messageBubble.setLayout(new GridBagLayout());
        messageBubble.add(title,        new GBC(0, 0).setGridWidth(2).setAnchor(GBC.WEST).setFill(GBC.BOTH).setFill(GBC.HORIZONTAL).setInsets(5, 5, 0, 5));
        messageBubble.add(contentPanel,new GBC(0, 1).setGridWidth(2).setAnchor(GBC.CENTER).setFill(GBC.BOTH).setInsets(0, 0, 0, 0));
        messageBubble.add(sourceIcon,new GBC(0, 2).setAnchor(GBC.WEST).setFill(GBC.BOTH).setInsets(5, 5, 0, 0));
        messageBubble.add(sourceName,new GBC(1, 2).setAnchor(GBC.WEST).setFill(GBC.BOTH).setInsets(5, 0, 0, 5));


    }
}
