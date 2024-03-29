package cn.shu.wechat.swing.panels;


import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.utils.IconUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Created by 舒新胜 on 2017/6/15.
 */
public class TipPanel extends ParentAvailablePanel {
    private JLabel imageLabel;

    public TipPanel(JPanel parent) {
        super(parent);
        initComponents();
        initView();
    }

    private void initComponents() {
        imageLabel = new JLabel();
        imageLabel.setForeground(Color.GRAY);
        imageLabel.setIcon(IconUtil.getIcon(this, "/image/bg.png", 140, 140));
    }

    private void initView() {
        setLayout(new GridBagLayout());
        add(imageLabel, new GBC(0, 0).setAnchor(GBC.CENTER).setInsets(0, 0, 50, 0));
    }
    public void setText(String s){
        imageLabel.setText(s);
    }

}
