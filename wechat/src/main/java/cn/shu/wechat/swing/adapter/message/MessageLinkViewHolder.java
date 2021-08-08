package cn.shu.wechat.swing.adapter.message;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.SizeAutoAdjustTextArea;
import cn.shu.wechat.swing.components.VerticalFlowLayout;
import cn.shu.wechat.swing.components.message.RCAttachmentMessageBubble;
import cn.shu.wechat.swing.components.message.RCLeftImageMessageBubble;
import cn.shu.wechat.swing.components.message.TagPanel;
import cn.shu.wechat.swing.utils.FontUtil;
import lombok.Getter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * @author 舒新胜
 * @date 17-6-2
 */

public class MessageLinkViewHolder extends BaseMessageViewHolder {

    public static final int thumbWidth = 48;
    protected final JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    protected final JPanel messageAvatarPanel = new JPanel();
    protected final JTextArea title = new JTextArea();
    protected final JTextArea desc = new JTextArea();
    protected final JLabel icon = new JLabel();
    protected final JPanel senderMessagePanel = new JPanel();
    protected final TagPanel contentTagPanel = new TagPanel();
    protected final RCAttachmentMessageBubble messageBubble;
    protected JLabel sourceName = new JLabel();
    protected JLabel sourceIcon = new JLabel();
    protected JPanel sourcePanel =  new JPanel((new FlowLayout(FlowLayout.LEFT,5,0)));
    public MessageLinkViewHolder(RCAttachmentMessageBubble messageBubble) {
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

        senderMessagePanel.setBackground(Colors.WINDOW_BACKGROUND);
        senderMessagePanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));

    }
    private void setListeners() {
        MouseAdapter listener = messageBubble.getMouseListener();
        contentTagPanel.addMouseListener(listener);
        title.addMouseListener(listener);
        desc.addMouseListener(listener);
        icon.addMouseListener(listener);

    }
    private void initView() {
        setLayout(new BorderLayout());
        timePanel.add(time);

        //消息内容

        contentTagPanel.setLayout(new GridBagLayout());

        JPanel descIconPanel = new JPanel(new BorderLayout());

        sourcePanel.add(sourceIcon);
        sourcePanel.add(sourceName);
        sourcePanel.setOpaque(false);
        Color decode = Color.decode("#f2f2f2");
        sourcePanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, decode));
        sourceName.setFont(new Font("楷体",Font.PLAIN,12));
        sourceName.setOpaque(false);
        sourceName.setForeground(Color.GRAY);
        int width = 250;
        int height = 120;
        contentTagPanel.setPreferredSize(new Dimension(width, height));
        contentTagPanel.setOpaque(false);
        descIconPanel.setOpaque(false);
        descIconPanel.add(desc,BorderLayout.CENTER);
        descIconPanel.add(icon,BorderLayout.EAST);
        title.setFont(new Font("楷体",Font.BOLD,18));
        title.setEditable(false);
        title.setOpaque(false);
        title.setLineWrap(true);
        title.setWrapStyleWord(true);
        desc.setLineWrap(true);
        desc.setOpaque(false);
        desc.setEditable(false);
        desc.setWrapStyleWord(true);
        desc.setForeground(Color.GRAY);
        title.setCursor(new Cursor(Cursor.HAND_CURSOR));
        desc.setCursor(new Cursor(Cursor.HAND_CURSOR));
        contentTagPanel.add(title, new GBC(0, 0)
                .setWeight(1, 30)
                .setFill(GridBagConstraints.BOTH)
                .setAnchor(GBC.NORTH)
                .setInsets(5, 0, 0, 0));
        contentTagPanel.add(descIconPanel, new GBC(0, 1)
                .setWeight(1, 70)
                .setAnchor(GBC.CENTER)
                .setFill(GridBagConstraints.BOTH)
                .setInsets(0, 0, 0, 0));
        messageBubble.setCursor(new Cursor(Cursor.HAND_CURSOR));
        messageBubble.setLayout(new VerticalFlowLayout(VerticalFlowLayout.BOTTOM,5,0,true,false));
        messageBubble.add(contentTagPanel);
        messageBubble.add(sourcePanel);
        add(timePanel, BorderLayout.NORTH);
        add(messageAvatarPanel, BorderLayout.CENTER);
    }
}
