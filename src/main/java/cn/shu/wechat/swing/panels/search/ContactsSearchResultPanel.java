package cn.shu.wechat.swing.panels.search;

import cn.shu.wechat.entity.SearchResultItem;
import cn.shu.wechat.swing.adapter.search.SearchResultItemsAdapter;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCListView;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索结果列表
 * Created by 舒新胜 on 17-6-21.
 */
public class ContactsSearchResultPanel extends SearchResultPanel {

    private final SearchResultItemsAdapter searchResultItemsAdapter;

    private RCListView resultItemsListView;
    private List<SearchResultItem> searchResultItems = new ArrayList<>();
    @Getter
    private JLabel tipLabel;

    public ContactsSearchResultPanel(JPanel parent, String selfName) {
        super(parent, selfName);

        initComponents();
        initView();
        searchResultItemsAdapter = new SearchResultItemsAdapter(searchResultItems);
        resultItemsListView.setAdapter(searchResultItemsAdapter);
    }

    private void initComponents() {
        resultItemsListView = new RCListView();
        this.setBackground(Colors.WINDOW_BACKGROUND);

        tipLabel = new JLabel("无搜索结果");
        tipLabel.setHorizontalAlignment(SwingConstants.CENTER);
        tipLabel.setForeground(Colors.DARK);
        tipLabel.setVisible(false);
    }

    private void initView() {
        setLayout(new GridBagLayout());
        resultItemsListView.setContentPanelBackground(Colors.WINDOW_BACKGROUND);
        add(tipLabel, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 1).setInsets(10, 0, 0, 0));
        add(resultItemsListView, new GBC(0, 1).setFill(GBC.BOTH).setWeight(1, 1000));
    }


    public void setData(List<SearchResultItem> data) {
        searchResultItems.clear();
        searchResultItems.addAll(data);
    }

    /**
     * 重绘整个列表
     */
    public void notifyDataSetChanged(boolean keepSize) {

        resultItemsListView.notifyDataSetChanged(keepSize);
    }

    public void setKeyWord(String keyWord) {
        this.searchResultItemsAdapter.setKeyWord(keyWord);
    }

    public void setSearchMessageOrFileListener(SearchResultItemsAdapter.SearchMessageOrFileListener searchMessageOrFileListener) {
        if (this.searchResultItemsAdapter == null) {
            throw new RuntimeException("请先设置adapter!");
        }

        this.searchResultItemsAdapter.setSearchMessageOrFileListener(searchMessageOrFileListener);
    }


}
