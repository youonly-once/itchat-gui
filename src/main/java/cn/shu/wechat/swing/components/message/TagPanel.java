package cn.shu.wechat.swing.components.message;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author 舒新胜
 * @date 27/06/2017
 */
public class TagPanel extends JPanel {
    private Object tag;

    public TagPanel(BorderLayout borderLayout) {
        super(borderLayout);
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }
}
