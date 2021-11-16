package cn.shu.wechat.swing.panels.chat;


import javax.swing.*;
import java.awt.*;

/**
 * Created by 舒新胜 on 04/07/2017.
 */
public class MessageEditorExpressionItemPanel extends JPanel {
    protected String code;
    protected ImageIcon icon;
    protected String displayName;

    protected Dimension size;

    private JLabel iconLabel;

    public MessageEditorExpressionItemPanel(String code, ImageIcon icon, String displayName) {
        this.code = code;
        this.icon = icon;
        this.displayName = displayName;

        setPreferredSize(new Dimension(30, 30));
        iconLabel = new JLabel();


        setIconPreferredSize(new Dimension(20, 20));
        iconLabel.setIcon(icon);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);
        add(iconLabel);

        this.setToolTipText(displayName);
    }

    public void setIconPreferredSize(Dimension size) {
        iconLabel.setPreferredSize(size);
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
