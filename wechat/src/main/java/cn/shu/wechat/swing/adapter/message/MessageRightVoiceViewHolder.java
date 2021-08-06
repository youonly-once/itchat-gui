package cn.shu.wechat.swing.adapter.message;

import cn.shu.wechat.swing.components.*;
import cn.shu.wechat.swing.components.message.MessagePopupMenu;
import cn.shu.wechat.swing.components.message.RCLeftVoiceMessageBubble;
import cn.shu.wechat.swing.components.message.RCRightVoiceMessageBubble;
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
public class MessageRightVoiceViewHolder extends MessageVoiceViewHolder {

    protected final RCRightVoiceMessageBubble messageBubble = new RCRightVoiceMessageBubble();
    public MessageRightVoiceViewHolder(boolean isGroup) {
        super(isGroup);
        initComponent();
        initView();
    }
    private void initComponent(){

    }
    private void initView(){
        setLayout(new BorderLayout());
        timePanel.add(time);
        messageBubble.setCursor(new Cursor(Cursor.HAND_CURSOR));
        messageBubble.add(durationText);
        messageBubble.add(voiceImgLabel);

        JPanel senderMessagePanel = new JPanel();
        senderMessagePanel.setBackground(Colors.WINDOW_BACKGROUND);
        senderMessagePanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));

        JPanel voicePanel = new JPanel();
        voicePanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));
        voicePanel.add(messageBubble);
        voicePanel.add(progressBar);
        try {
            voiceImgLabel.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/image/right_voice.png"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        senderMessagePanel.add(voicePanel);

        messageAvatarPanel.setLayout(new GridBagLayout());
        messageAvatarPanel.add(senderMessagePanel, new GBC(1, 0).setWeight(1000, 1).setAnchor(GBC.EAST).setInsets(0, 5, 0, 0));
        messageAvatarPanel.add(avatar, new GBC(2, 0)
                .setWeight(1, 1)
                .setAnchor(GBC.NORTH)
                .setInsets(0, 5, 0, 0));

        add(timePanel, BorderLayout.NORTH);
        add(messageAvatarPanel, BorderLayout.CENTER);
    }
}
