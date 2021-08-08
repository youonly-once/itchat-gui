package cn.shu.wechat.swing.components.message;

import java.awt.*;

/**
 * 右侧图片聊天气泡
 */
public class RCRightLinkMessageBubble extends RCAttachmentMessageBubble {
    public RCRightLinkMessageBubble() {
        NinePatchImageIcon backgroundNormal = new NinePatchImageIcon(this.getClass().getResource("/image/right_white.9.png"));
        NinePatchImageIcon backgroundActive = new NinePatchImageIcon(this.getClass().getResource("/image/right_white_active.9.png"));
        setBackgroundNormalIcon(backgroundNormal);
        setBackgroundActiveIcon(backgroundActive);
        setBackgroundIcon(backgroundNormal);
    }

    @Override
    public Insets getInsets() {
        return new Insets(2, 2, 5, 8);
    }
}
