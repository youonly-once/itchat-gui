package cn.shu.wechat.swing.panels;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;

import javax.swing.*;
import java.awt.*;

/**
 * Created by 舒新胜 on 17-5-29.
 */
public class LeftPanel extends JPanel {
    /**
     * 个人信息面板
     */
    private MyInfoPanel myInfoPanel;

    /**
     * 搜索面板
     */
    private SearchPanel searchPanel;

    private TabOperationPanel mainOperationPanel;

    /**
     * list panel
     */
    private ListPanel listPanel;

    public LeftPanel() {

        initComponents();
        initView();
    }

    private void initComponents() {
        myInfoPanel = new MyInfoPanel(this);

        searchPanel = new SearchPanel(this);

        mainOperationPanel = new TabOperationPanel(this);
        //mainOperationPanel.setBackground(Color.blue);

        listPanel = new ListPanel(this);
        listPanel.setBackground(Colors.DARK);
    }

    private void initView() {
        this.setBackground(Colors.DARK);
        this.setLayout(new GridBagLayout());

        add(myInfoPanel, new GBC(0, 0).setAnchor(GBC.CENTER).setFill(GBC.BOTH).setWeight(1, 7));
        add(searchPanel, new GBC(0, 1).setAnchor(GBC.CENTER).setFill(GBC.HORIZONTAL).setWeight(1, 1));
        add(mainOperationPanel, new GBC(0, 2).setAnchor(GBC.CENTER).setFill(GBC.BOTH).setWeight(1, 1));
        add(listPanel, new GBC(0, 3).setAnchor(GBC.CENTER).setFill(GBC.BOTH).setWeight(1, 60));

    }

    public ListPanel getListPanel() {
        return this.listPanel;
    }


}
