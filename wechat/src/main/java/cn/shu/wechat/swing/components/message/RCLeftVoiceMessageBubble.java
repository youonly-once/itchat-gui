package cn.shu.wechat.swing.components.message;

import java.awt.*;

/**
 * 左侧文本聊天气泡
 */
public class RCLeftVoiceMessageBubble extends RCAttachmentMessageBubble {
    public RCLeftVoiceMessageBubble() {
        NinePatchImageIcon backgroundNormal = new NinePatchImageIcon(this.getClass().getResource("/image/left.9.png"));
        NinePatchImageIcon backgroundActive = new NinePatchImageIcon(this.getClass().getResource("/image/left_active.9.png"));
        setBackgroundNormalIcon(backgroundNormal);
        setBackgroundActiveIcon(backgroundActive);
        setBackgroundIcon(backgroundNormal);
    }

    @Override
    public Insets getInsets() {
        return new Insets(5, 10, 5, 10);
    }
}
