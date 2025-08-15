package cn.shu.wechat.swing.panels.search;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.panels.ParentAvailablePanel;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 * Created by 舒新胜 on 17-5-30.
 */
public class SearchCardLayoutPanel extends ParentAvailablePanel {


    private final CardLayout cardLayout = new CardLayout();
    /**
     * -- GETTER --
     * 获取上一个tab，如果上一个tab是搜索tab，则返回搜索tab之前的tab
     *
     * @return
     */
    @Getter
    protected String previousTab;
    /**
     * -- GETTER --
     * 获取当前选中的tab, 如果当前的tab是搜索tab，则返回搜索tab之前的tab
     *
     * @return
     */
    protected String currentTab;


    public SearchCardLayoutPanel(JPanel parent, String previousTab, String currentTab) {
        super(parent);
        this.currentTab = currentTab;
        this.previousTab = previousTab;

        initComponents();
        initView();
    }


    private void initComponents() {


    }

    private void initView() {
        this.setLayout(cardLayout);
        this.setBackground(Colors.LEFT_WINDOW_BACKGROUND);

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

}
