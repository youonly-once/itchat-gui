package cn.shu.wechat.swing.panels.left.tabcontent;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.panels.ParentAvailablePanel;

import javax.swing.*;
import java.awt.*;

/**
 * Created by 舒新胜 on 17-5-30.
 */
public class CollectionsPanel extends ParentAvailablePanel {
    private JLabel tipLabel;

    public CollectionsPanel(JPanel parent) {
        super(parent);

        initComponents();
        initView();
    }

    private void initComponents() {
        tipLabel = new JLabel("暂无收藏");
        tipLabel.setForeground(Colors.FONT_GRAY);
    }

    private void initView() {
        this.setBackground(Colors.DARK);
        setLayout(new FlowLayout());
        add(tipLabel);
    }
}
