package cn.shu.wechat.swing.adapter.message.voice;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.VerticalFlowLayout;
import cn.shu.wechat.swing.components.message.RCRightVoiceMessageBubble;
import cn.shu.wechat.swing.utils.IconUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 *
 * @author 舒新胜
 * @date 17-6-2
 */

public class MessageRightVoiceViewHolder extends MessageVoiceViewHolder {

    public MessageRightVoiceViewHolder() {
        super(new RCRightVoiceMessageBubble());
        initComponent();
        initView();
    }
    private void initComponent(){

    }
    private void initView(){
        setLayout(new BorderLayout());
        timePanel.add(time);
        messageBubble.setCursor(new Cursor(Cursor.HAND_CURSOR));
        messageBubble.add(gapText);
        messageBubble.add(durationText);
        messageBubble.add(unitLabel);
        messageBubble.add(voiceImgLabel);

        contentTagPanel.setBackground(Colors.WINDOW_BACKGROUND);
        contentTagPanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));
        JPanel processBarPanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, true));
        processBarPanel.setOpaque(false);
        processBarPanel.add(progressBar);
        processBarPanel.setBorder(new EmptyBorder(0, 0, 0, messageBubble.getSalientPointPixel()));
        voicePanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));
        voicePanel.add(messageBubble);
        voicePanel.add(processBarPanel);
        JPanel controlPanel = new JPanel(new BorderLayout(0, 0));
        controlPanel.add(voicePanel, BorderLayout.CENTER);
        controlPanel.add(revoke, BorderLayout.EAST);

        voiceImgLabel.setIcon(IconUtil.getIcon(this, "/image/right_voice.png"));
        contentTagPanel.add(controlPanel);

        messageAvatarPanel.setLayout(new GridBagLayout());
        messageAvatarPanel.add(contentTagPanel, new GBC(1, 0).setWeight(1000, 1).setAnchor(GBC.EAST).setInsets(0, 5, 0, 0));
        messageAvatarPanel.add(avatar, new GBC(2, 0)
                .setWeight(1, 1)
                .setAnchor(GBC.NORTH)
                .setInsets(0, 5, 0, 5));

        add(timePanel, BorderLayout.NORTH);
        add(messageAvatarPanel, BorderLayout.CENTER);
    }
}
