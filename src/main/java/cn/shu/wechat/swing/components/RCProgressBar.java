package cn.shu.wechat.swing.components;

import lombok.Getter;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Created by 舒新胜 on 17-6-4.
 */
public class RCProgressBar extends JProgressBar {
    private int height = 1;
    public RCProgressBar() {
        setForeground(Colors.PROGRESS_BAR_START);

        setBorder(new LineBorder(Colors.PROGRESS_BAR_END));
    }
    public RCProgressBar(int height) {
        this();
        this.height = height;
    }

    @Override
    protected void paintBorder(Graphics g) {
    }

    public int getHeight() {
        return height;
    }


    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), height);
    }
}
