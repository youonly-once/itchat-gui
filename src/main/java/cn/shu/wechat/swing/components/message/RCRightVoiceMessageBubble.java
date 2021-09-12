package cn.shu.wechat.swing.components.message;

import cn.shu.wechat.swing.utils.IconUtil;

import java.awt.*;

/**
 * 左侧文本聊天气泡
 */
public class RCRightVoiceMessageBubble extends RCAttachmentMessageBubble {
    public RCRightVoiceMessageBubble() {
        NinePatchImageIcon backgroundNormal = new NinePatchImageIcon(IconUtil.getBufferedImage(this,"/image/right.9.png"));
        NinePatchImageIcon backgroundActive = new NinePatchImageIcon(IconUtil.getBufferedImage(this,"/image/right_active.9.png"));
        setBackgroundNormalIcon(backgroundNormal);
        setBackgroundActiveIcon(backgroundActive);
        setBackgroundIcon(backgroundNormal);
    }

    @Override
    public Insets getInsets() {
        return new Insets(5, 10, 5, 10);
    }
}
