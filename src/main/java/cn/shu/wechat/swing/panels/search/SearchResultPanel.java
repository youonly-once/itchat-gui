package cn.shu.wechat.swing.panels.search;

import cn.shu.wechat.entity.SearchResultItem;
import cn.shu.wechat.swing.adapter.search.SearchResultItemsAdapter;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.panels.ParentAvailablePanel;
import lombok.Getter;

import javax.swing.*;
import java.util.List;

/**
 * 搜索结果列表
 * Created by 舒新胜 on 17-6-21.
 */
public abstract class SearchResultPanel extends ParentAvailablePanel {

    /**
     * 自己所在布局管理器的名称
     */
    @Getter
    private final String selfName;

    @Getter
    private JLabel tipLabel;

    public SearchResultPanel(JPanel parent, String selfName) {
        super(parent);
        this.selfName = selfName;
        initComponents();
    }

    private void initComponents() {
        this.setBackground(Colors.LEFT_WINDOW_BACKGROUND);

        tipLabel = new JLabel("无搜索结果");
        tipLabel.setHorizontalAlignment(SwingConstants.CENTER);
        tipLabel.setForeground(Colors.DARK);
        tipLabel.setVisible(false);
    }

    public abstract void setData(List<SearchResultItem> data);

    /**
     * 重绘整个列表
     */
    public abstract void notifyDataSetChanged(boolean keepSize);


    /**
     * 在布局管理器中展示自己
     */
    public void showSelf() {
        ((SearchCardLayoutPanel) getParentPanel()).showPanel(selfName);
    }

    public void showPreviousTab() {
        SearchCardLayoutPanel parentPanel = (SearchCardLayoutPanel) getParentPanel();
        parentPanel.showPanel(parentPanel.getPreviousTab());
    }

    public abstract void setKeyWord(String keyWord);

    public abstract void setSearchMessageOrFileListener(SearchResultItemsAdapter.SearchMessageOrFileListener searchMessageOrFileListener);

}
