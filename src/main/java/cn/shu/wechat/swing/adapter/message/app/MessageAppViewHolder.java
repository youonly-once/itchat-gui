package cn.shu.wechat.swing.adapter.message.app;

import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.VerticalFlowLayout;
import cn.shu.wechat.swing.components.message.RCAttachmentMessageBubble;
import cn.shu.wechat.swing.components.message.TagPanel;
import cn.shu.wechat.swing.utils.FontUtil;

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

    protected final JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    protected final JPanel messageAvatarPanel = new JPanel();
    /**
     * APP消息标题
     */
    public final JTextArea title = new JTextArea();

    protected final JPanel senderMessagePanel = new JPanel();
    /**
     * 消息内容面板 由APP消息各类子类型实现
     */
    protected final JPanel contentPanel = new JPanel(new BorderLayout());
    public final TagPanel contentTitlePanel = new TagPanel();
    public final RCAttachmentMessageBubble messageBubble;
    /**
     * APP名称
     */
    public final JLabel sourceName = new JLabel();
    /**
     * APP图标
     */
    public final JLabel sourceIcon = new JLabel();
    /**
     * APP信息面板
     */
    public final JPanel sourcePanel =  new JPanel((new FlowLayout(FlowLayout.LEFT,5,0)));

    public MessageAppViewHolder(RCAttachmentMessageBubble messageBubble) {
        this.messageBubble = messageBubble;
        initComponents();
        initView();
        setListeners();
    }

    private void initComponents() {
        timePanel.setBackground(Colors.WINDOW_BACKGROUND);
        messageAvatarPanel.setBackground(Colors.WINDOW_BACKGROUND);
        messageAvatarPanel.setLayout(new GridBagLayout());
        time.setForeground(Colors.FONT_GRAY);
        time.setFont(FontUtil.getDefaultFont(12));
        contentPanel.setBorder(new EmptyBorder(5,5,5,5));
        contentPanel.setOpaque(false);
        senderMessagePanel.setBackground(Colors.WINDOW_BACKGROUND);
        senderMessagePanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));

    }
    private void setListeners() {
        MouseAdapter listener = messageBubble.getMouseListener();
        contentTitlePanel.addMouseListener(listener);
        title.addMouseListener(listener);

    }
    private void initView() {
        setLayout(new BorderLayout());
        timePanel.add(time);

        sourcePanel.add(sourceIcon);
        sourcePanel.add(sourceName);
        sourcePanel.setOpaque(false);
        sourcePanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#f2f2f2")));
        sourceName.setFont(new Font("楷体",Font.PLAIN,12));
        sourceName.setOpaque(false);
        sourceName.setForeground(Color.GRAY);


        title.setFont(new Font("楷体",Font.BOLD,18));
        title.setEditable(false);
        title.setOpaque(false);
        title.setLineWrap(true);
        title.setWrapStyleWord(true);
        title.setCursor(new Cursor(Cursor.HAND_CURSOR));

        contentTitlePanel.setLayout(new GridBagLayout());
        contentTitlePanel.setOpaque(false);
        contentTitlePanel.add(title, new GBC(0, 0)
                .setWeight(1, 30)
                .setFill(GridBagConstraints.BOTH)
                .setAnchor(GBC.NORTH)
                .setInsets(5, 0, 0, 0));
        contentTitlePanel.add(contentPanel, new GBC(0, 1)
                .setWeight(1, 70)
                .setAnchor(GBC.CENTER)
                .setFill(GridBagConstraints.BOTH)
                .setInsets(0, 0, 0, 0));
        messageBubble.setCursor(new Cursor(Cursor.HAND_CURSOR));
        messageBubble.setLayout(new VerticalFlowLayout(VerticalFlowLayout.BOTTOM,5,0,true,false));
        messageBubble.add(contentTitlePanel);
        messageBubble.add(sourcePanel);
        add(timePanel, BorderLayout.NORTH);
        add(messageAvatarPanel, BorderLayout.CENTER);
    }
}
