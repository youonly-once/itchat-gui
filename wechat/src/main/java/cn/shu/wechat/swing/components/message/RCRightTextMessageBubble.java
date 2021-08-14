package cn.shu.wechat.swing.components.message;

import cn.shu.wechat.swing.utils.IconUtil;

/**
 * 右侧文本聊天气泡
 */
public class RCRightTextMessageBubble extends RCTextMessageBubble {
    public RCRightTextMessageBubble() {
        NinePatchImageIcon backgroundNormal = new NinePatchImageIcon(IconUtil.getBufferedImage(this,"/image/right.9.png"));
        NinePatchImageIcon backgroundActive = new NinePatchImageIcon(IconUtil.getBufferedImage(this,"/image/right_active.9.png"));
        setBackgroundNormalIcon(backgroundNormal);
        setBackgroundActiveIcon(backgroundActive);
        setBackgroundIcon(backgroundNormal);
    }
}
