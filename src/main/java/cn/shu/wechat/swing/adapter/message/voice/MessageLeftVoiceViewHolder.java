package cn.shu.wechat.swing.adapter.message.voice;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.SizeAutoAdjustTextArea;
import cn.shu.wechat.swing.components.VerticalFlowLayout;
import cn.shu.wechat.swing.components.message.RCLeftVoiceMessageBubble;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.utils.FontUtil;
import cn.shu.wechat.swing.utils.IconUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 *
 * @author 舒新胜
 * @date 17-6-2
 */

public class MessageLeftVoiceViewHolder extends MessageVoiceViewHolder {
    public final SizeAutoAdjustTextArea sender = new SizeAutoAdjustTextArea((int)(MainFrame.getContext().currentWindowWidth * 0.5));
    private JLabel unreadPoint;
    public MessageLeftVoiceViewHolder(boolean isGroup) {
       super(isGroup,new RCLeftVoiceMessageBubble());
        initComponent();
        initView();
    }
    private void initComponent(){

    }



    private void initView(){
        setLayout(new BorderLayout());
        timePanel.add(time);

        messageBubble.setCursor(new Cursor(Cursor.HAND_CURSOR));
        messageBubble.add(voiceImgLabel);
        messageBubble.add(durationText);
        messageBubble.add(unitLabel);
        messageBubble.add(gapText);
        JPanel senderMessagePanel = new JPanel();
        senderMessagePanel.setBackground(Colors.WINDOW_BACKGROUND);
        senderMessagePanel.setLayout(new BorderLayout());
        if (isGroup) {
            sender.setFont(FontUtil.getDefaultFont(12));
            sender.setForeground(Colors.FONT_GRAY);
            sender.setBorder(new EmptyBorder(0,messageBubble.getSalientPointPixel(),100,0));
            senderMessagePanel.add(sender,BorderLayout.NORTH);
        }
        voiceImgLabel.setIcon(IconUtil.getIcon(this, "/image/left_voice.png"));
        JPanel processBarPanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, true));
        processBarPanel.setOpaque(false);
        processBarPanel.add(progressBar);
        processBarPanel.setBorder(new EmptyBorder(0, messageBubble.getSalientPointPixel(), 0, 0));
        voicePanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, true));
        voicePanel.add(messageBubble);
        voicePanel.add(processBarPanel);
        contentTagPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        contentTagPanel.add(voicePanel);

        unreadPoint = new JLabel(IconUtil.getIcon(this, "/image/voice_redpoint.png"));
        unreadPoint.setBorder(new EmptyBorder(0, 5, 0, 0));
        contentTagPanel.add(unreadPoint);

        senderMessagePanel.add(contentTagPanel, BorderLayout.CENTER);
        senderMessagePanel.add(revoke,BorderLayout.EAST);
        messageAvatarPanel.setLayout(new GridBagLayout());
        messageAvatarPanel.add(avatar, new GBC(1, 0).setWeight(1, 1).setAnchor(GBC.NORTH).setInsets(0, 5, 0, 0));
        messageAvatarPanel.add(senderMessagePanel, new GBC(2, 0)
                .setWeight(1000, 1)
                .setAnchor(GBC.WEST)
                .setInsets(0, 5, 0, 0));

        add(timePanel, BorderLayout.NORTH);
        add(messageAvatarPanel, BorderLayout.CENTER);
    }
    @Override
    public void removeUnreadPoint(){
        if (unreadPoint!=null){
            contentTagPanel.remove(unreadPoint);
        }
    }
}
