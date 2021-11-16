package cn.shu.wechat.swing.panels.left;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.panels.left.tabcontent.LeftTabContentPanel;

import javax.swing.*;
import javax.swing.border.LineBorder;
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
    private LeftTabContentPanel leftTabContentPanel;

    public LeftPanel() {

        initComponents();
        initView();
    }

    private void initComponents() {
        myInfoPanel = new MyInfoPanel(this);

        searchPanel = new SearchPanel(this);

        mainOperationPanel = new TabOperationPanel(this);

        leftTabContentPanel = new LeftTabContentPanel(this);
    }

    private void initView() {
        this.setBackground(Colors.WINDOW_BACKGROUND);
        this.setLayout(new GridBagLayout());

        add(myInfoPanel, new GBC(0, 0).setAnchor(GBC.CENTER).setFill(GBC.BOTH).setWeight(1, 10).setInsets(0,0,0,0));
        add(searchPanel, new GBC(0, 1).setAnchor(GBC.CENTER).setFill(GBC.BOTH).setWeight(1, 2).setInsets(0,0,0,0));
        add(mainOperationPanel, new GBC(0, 2).setAnchor(GBC.CENTER).setFill(GBC.BOTH).setWeight(1, 2).setInsets(0,0,0,0));
        add(leftTabContentPanel, new GBC(0, 3).setAnchor(GBC.CENTER).setFill(GBC.BOTH).setWeight(1, 86).setInsets(0,0,0,0));
    }

    public LeftTabContentPanel getListPanel() {
        return this.leftTabContentPanel;
    }


}
