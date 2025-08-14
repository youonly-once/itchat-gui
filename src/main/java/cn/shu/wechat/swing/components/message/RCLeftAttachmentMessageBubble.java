package cn.shu.wechat.swing.components.message;

import cn.shu.wechat.utils.IconUtil;

import java.awt.*;

/**
 * 右侧文本聊天气泡
 */
public class RCLeftAttachmentMessageBubble extends RCAttachmentMessageBubble {
    public RCLeftAttachmentMessageBubble(Insets insets) {
        super(insets);
        NinePatchImageIcon backgroundNormal = new NinePatchImageIcon(IconUtil.getBufferedImage(this,"/image/left.9.png"));
        NinePatchImageIcon backgroundActive = new NinePatchImageIcon(IconUtil.getBufferedImage(this,"/image/left_active.9.png"));
        setBackgroundNormalIcon(backgroundNormal);
        setBackgroundActiveIcon(backgroundActive);
        setBackgroundIcon(backgroundNormal);
    }

    public RCLeftAttachmentMessageBubble() {
        this(new Insets(5,5,5,5));
    }

}
