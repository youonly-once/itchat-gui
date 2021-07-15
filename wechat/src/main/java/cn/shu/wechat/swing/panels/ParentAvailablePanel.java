package cn.shu.wechat.swing.panels;

import lombok.NoArgsConstructor;

import javax.swing.*;

/**
 * Created by song on 17-5-30.
 */
@NoArgsConstructor
public class ParentAvailablePanel extends JPanel {
    private JPanel parent;

    public ParentAvailablePanel(JPanel parent) {
        this.parent = parent;
    }

    public JPanel getParentPanel() {
        return parent;
    }
}
