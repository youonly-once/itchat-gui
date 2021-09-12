package cn.shu.wechat.swing.components.message;

import cn.shu.wechat.swing.utils.IconUtil;

import java.awt.*;

/**
 * 右侧图片聊天气泡
 */
public class RCRightImageMessageBubble extends RCAttachmentMessageBubble {
    public RCRightImageMessageBubble() {
        NinePatchImageIcon backgroundNormal = new NinePatchImageIcon(IconUtil.getBufferedImage(this,"/image/right.9.png"));
        NinePatchImageIcon backgroundActive = new NinePatchImageIcon(IconUtil.getBufferedImage(this,"/image/right_active.9.png"));
        setBackgroundNormalIcon(backgroundNormal);
        setBackgroundActiveIcon(backgroundActive);
        setBackgroundIcon(backgroundNormal);
    }

    @Override
    public Insets getInsets() {
        return new Insets(2, 2, 5, 8);
    }
}
