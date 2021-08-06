package cn.shu.wechat.swing.adapter.message;

import cn.shu.wechat.swing.components.*;
import cn.shu.wechat.swing.components.message.MessagePopupMenu;
import cn.shu.wechat.swing.components.message.RCLeftImageMessageBubble;
import cn.shu.wechat.swing.components.message.RCLeftTextMessageBubble;
import cn.shu.wechat.swing.components.message.RCLeftVoiceMessageBubble;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.utils.FontUtil;
import lombok.Getter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Created by 舒新胜 on 17-6-2.
 */
@Getter
public class MessageLeftVoiceViewHolder extends MessageVoiceViewHolder {
    private final RCLeftVoiceMessageBubble messageBubble = new RCLeftVoiceMessageBubble();
    private final JLabel sender = new JLabel();
    private JLabel unreadPoint;
    protected final JPanel contentPanel = new JPanel();
    public MessageLeftVoiceViewHolder(boolean isGroup) {
       super(isGroup);
        initComponent();
        initView();
    }
    private void initComponent(){

    }
    private void initView(){
        setLayout(new BorderLayout());
        timePanel.add(time);
        sender.setFont(FontUtil.getDefaultFont(12));
        sender.setForeground(Colors.FONT_GRAY);
        messageBubble.setCursor(new Cursor(Cursor.HAND_CURSOR));
        messageBubble.add(voiceImgLabel);
        messageBubble.add(durationText);

        JPanel senderMessagePanel = new JPanel();
        senderMessagePanel.setBackground(Colors.WINDOW_BACKGROUND);
        senderMessagePanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));
        if (isGroup) {
            senderMessagePanel.add(sender);
        }
        try {
            voiceImgLabel.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/image/left_voice.png"))));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JPanel voicePanel = new JPanel();
        voicePanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));
        voicePanel.add(messageBubble);
        voicePanel.add(progressBar);

        contentPanel.add(voicePanel);
        try {
            unreadPoint = new JLabel(new ImageIcon(ImageIO.read(getClass().getResource("/image/voice_redpoint.png"))));
            contentPanel.add(unreadPoint);
        } catch (IOException e) {
            e.printStackTrace();
        }
        senderMessagePanel.add(contentPanel);

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
            contentPanel.remove(unreadPoint);
        }
    }
}
