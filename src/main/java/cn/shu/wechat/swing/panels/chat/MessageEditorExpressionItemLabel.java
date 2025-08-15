package cn.shu.wechat.swing.panels.chat;


import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;

/**
 * Created by 舒新胜 on 04/07/2017.
 */
public class MessageEditorExpressionItemLabel extends JLabel {
    @Getter
    @Setter
    private String code;

    private Dimension size;


    public MessageEditorExpressionItemLabel(String code, ImageIcon icon, String displayName) {
        this.code = code;
        size = new Dimension(40, 40);
        this.setPreferredSize(size);
        this.setIcon(icon);
        this.setOpaque(true);
        this.setHorizontalAlignment(SwingConstants.CENTER);
        this.setVerticalAlignment(SwingConstants.CENTER);
        icon.setDescription(code+"&"+icon.getDescription());

        this.setToolTipText(displayName);
    }


    @Override
    public Dimension getSize() {
        return size;
    }

    @Override
    public void setSize(Dimension size) {
        this.size = size;
    }
}
