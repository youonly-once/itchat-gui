package cn.shu.wechat.swing.components.message;

import cn.shu.wechat.utils.IconUtil;

import java.awt.*;

/**
 * 右侧附件聊天气泡
 */
public class RCRightAttachmentMessageBubble extends RCAttachmentMessageBubble {
    public RCRightAttachmentMessageBubble(Insets insets) {
        super(insets);
        NinePatchImageIcon backgroundNormal = new NinePatchImageIcon(IconUtil.getBufferedImage(this,"/image/right_white.9.png"));
        NinePatchImageIcon backgroundActive = new NinePatchImageIcon(IconUtil.getBufferedImage(this,"/image/right_white_active.9.png"));
        setBackgroundNormalIcon(backgroundNormal);
        setBackgroundActiveIcon(backgroundActive);
        setBackgroundIcon(backgroundNormal);
    }

    public RCRightAttachmentMessageBubble() {
        this(new Insets(5,5,5,5));
    }
}
