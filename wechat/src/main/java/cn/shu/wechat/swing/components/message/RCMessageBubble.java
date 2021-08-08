package cn.shu.wechat.swing.components.message;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by 舒新胜 on 27/06/2017.
 */
public interface RCMessageBubble {
    void addMouseListener(MouseListener l);

    void setBackgroundIcon(Icon icon);

    NinePatchImageIcon getBackgroundNormalIcon();

    NinePatchImageIcon getBackgroundActiveIcon();

    void setActiveStatus(boolean status);

    default MouseAdapter getMouseListener(){
        MouseAdapter listener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setActiveStatus(true);
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setActiveStatus(false);
                super.mouseExited(e);
            }
        };
        return listener;
    }
}
