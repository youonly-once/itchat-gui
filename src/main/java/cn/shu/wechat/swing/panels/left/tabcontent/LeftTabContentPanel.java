package cn.shu.wechat.swing.panels.left.tabcontent;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.panels.search.SearchCardLayoutPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Created by 舒新胜 on 17-5-30.
 */
public class LeftTabContentPanel extends SearchCardLayoutPanel {
    private static LeftTabContentPanel context;

    private final CardLayout cardLayout = new CardLayout();


    public LeftTabContentPanel(JPanel parent, String previousTab, String currentTab) {
        super(parent, previousTab, currentTab);
        context = this;
        initComponents();
        initView();
    }


    private void initComponents() {


    }

    private void initView() {
        this.setLayout(cardLayout);
        setBackground(Colors.WINDOW_BACKGROUND);
    }

    /**
     * 显示指定的card
     *
     * @param who
     */
    public void showPanel(String who) {
        previousTab = currentTab;
        if (!who.equals("SEARCH")) {
            currentTab = who;
        }
        cardLayout.show(this, who);
    }

    /**
     * 获取上一个tab，如果上一个tab是搜索tab，则返回搜索tab之前的tab
     *
     * @return
     */
    public String getPreviousTab() {
        return previousTab;
    }

    /**
     * 获取当前选中的tab, 如果当前的tab是搜索tab，则返回搜索tab之前的tab
     *
     * @return
     */
    public String getCurrentTab() {
        return currentTab;
    }

    public static LeftTabContentPanel getContext() {
        return context;
    }

}
