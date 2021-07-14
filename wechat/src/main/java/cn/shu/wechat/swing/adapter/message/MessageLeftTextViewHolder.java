package cn.shu.wechat.swing.adapter.message;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.SizeAutoAdjustTextArea;
import cn.shu.wechat.swing.components.VerticalFlowLayout;
import cn.shu.wechat.swing.components.message.MessagePopupMenu;
import cn.shu.wechat.swing.components.message.RCLeftImageMessageBubble;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.utils.FontUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Created by song on 17-6-2.
 */
public class MessageLeftTextViewHolder extends BaseMessageViewHolder
{
    public JLabel sender = new JLabel();
    //public JLabel avatar = new JLabel();
    //public JLabel size = new JLabel();
    //public RCLeftTextMessageBubble text = new RCLeftTextMessageBubble();

    public SizeAutoAdjustTextArea text;
    public RCLeftImageMessageBubble messageBubble = new RCLeftImageMessageBubble();

    private JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER,5,0));
    private JPanel messageAvatarPanel = new JPanel();
    private MessagePopupMenu popupMenu = new MessagePopupMenu();
    private boolean isGroup = true;
    public MessageLeftTextViewHolder(boolean isGroup)
    {
        this.isGroup = isGroup;
        initComponents();
        initView();
    }

    private void initComponents()
    {
        int maxWidth = (int) (MainFrame.getContext().currentWindowWidth * 0.5);
        text = new SizeAutoAdjustTextArea(maxWidth);
        text.setParseUrl(true);

        time.setForeground(Colors.FONT_GRAY);
        time.setFont(FontUtil.getDefaultFont(12));

        sender.setFont(FontUtil.getDefaultFont(12));
        sender.setForeground(Colors.FONT_GRAY);

        messageAvatarPanel.setBackground(Colors.WINDOW_BACKGROUND);
        timePanel.setBackground(Colors.WINDOW_BACKGROUND);
    }

    private void initView()
    {
        setLayout(new BorderLayout());
        timePanel.add(time);
       // text.setBorder(new LineBorder(Color.YELLOW));
        messageBubble.add(text);
        messageBubble.setBackground(Colors.LIGHT_GRAY);
        JPanel senderMessagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
        senderMessagePanel.setBackground(Colors.WINDOW_BACKGROUND);
        senderMessagePanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0,0,true, false));
        if (isGroup){
           senderMessagePanel.add(sender);
        }
        senderMessagePanel.add(messageBubble);
        senderMessagePanel.setBackground(Colors.LIGHT_GRAY);
        //senderMessagePanel.setBorder(new LineBorder(Color.blue));
        messageAvatarPanel.setLayout(new GridBagLayout());
        messageAvatarPanel.add(avatar, new GBC(1, 0).setWeight(1, 1).setAnchor(GBC.NORTH).setInsets(0,5,0,0));
        messageAvatarPanel.add(senderMessagePanel, new GBC(2, 0)
                .setWeight(1000, 1)
                .setAnchor(GBC.WEST)
                .setInsets(0,5,0,0));
        //messageAvatarPanel.setBorder(new LineBorder(Color.black));
        add(timePanel, BorderLayout.NORTH);
        add(messageAvatarPanel, BorderLayout.CENTER);
    }
}
