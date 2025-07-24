package cn.shu.wechat.swing.panels.chat;


import javax.swing.*;
import java.awt.*;

/**
 * Created by 舒新胜 on 04/07/2017.
 */
public class MessageEditorExpressionItemLabel extends JLabel {
    private String code;
    private ImageIcon icon;
    private String displayName;

    private Dimension size;


    public MessageEditorExpressionItemLabel(String code, ImageIcon icon, String displayName) {
        this.code = code;
        this.icon = icon;
        this.displayName = displayName;
        size = new Dimension(20, 20);
        this.setPreferredSize(size);
        this.setIcon(icon);
        this.setOpaque(true);
        this.setHorizontalAlignment(SwingConstants.CENTER);
        this.setVerticalAlignment(SwingConstants.CENTER);
        icon.setDescription(code+"&"+icon.getDescription());

        this.setToolTipText(displayName);
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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
