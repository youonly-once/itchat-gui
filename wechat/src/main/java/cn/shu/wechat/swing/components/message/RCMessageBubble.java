package cn.shu.wechat.swing.components.message;

import javax.swing.*;
import java.awt.event.MouseListener;

/**
 * Created by 舒新胜 on 27/06/2017.
 */
public interface RCMessageBubble {
    void addMouseListener(MouseListener l);

    void setBackgroundIcon(Icon icon);

    NinePatchImageIcon getBackgroundNormalIcon();

    NinePatchImageIcon getBackgroundActiveIcon();
}
