package cn.shu.wechat.swing.panels.search;

import cn.shu.wechat.constant.SearchResultType;
import cn.shu.wechat.entity.SearchResultItem;
import cn.shu.wechat.entity.SelectUserData;
import cn.shu.wechat.swing.adapter.search.SearchResultItemsAdapter;
import cn.shu.wechat.swing.adapter.selected.SelectUserItemViewHolder;
import cn.shu.wechat.swing.adapter.selected.SelectUserItemsAdapter;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCListView;
import cn.shu.wechat.swing.listener.AbstractMouseListener;
import cn.shu.wechat.swing.panels.SelectUserPanel;
import cn.shu.wechat.utils.IconUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class ForwardSearchResultPanel extends SearchResultPanel {
    private RCListView<SelectUserItemViewHolder, SelectUserItemsAdapter> selectUserListView;

    private List<SelectUserData> leftUserList;

    private SelectUserItemsAdapter selectUserItemsAdapter;

    private ImageIcon checkIcon;
    private ImageIcon uncheckIcon;


    public ForwardSearchResultPanel(JPanel parent, String selfName, List<SelectUserData> leftUserList) {
        super(parent, selfName);
        this.leftUserList = leftUserList;
        initComponents();
        initView();
    }


    private void initComponents() {
        checkIcon = IconUtil.getIcon(this, "/image/check.png");
        uncheckIcon = IconUtil.getIcon(this, "/image/uncheck.png");

        selectUserListView = new RCListView();

        selectUserItemsAdapter = new SelectUserItemsAdapter(leftUserList);
        selectUserItemsAdapter.setMouseListener(new AbstractMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SelectUserItemViewHolder holder = (SelectUserItemViewHolder) e.getSource();
                SelectUserPanel selectUserPanel = (SelectUserPanel) (((SearchCardLayoutPanel) getParentPanel()).getParentPanel());
                String username = holder.username;
                if (selectUserPanel.unSelectUser(username)) {
                    holder.icon.setIcon(uncheckIcon);
                } else {
                    selectUserPanel.selectUser(leftUserList.stream().filter(data -> data.getUserName().equals(username))
                            .findAny().get());
                    holder.icon.setIcon(checkIcon);
                }


            }
        });
        selectUserListView.setScrollBarColor(Colors.SCROLL_BAR_THUMB, Colors.WINDOW_BACKGROUND);
        selectUserListView.setAdapter(selectUserItemsAdapter);

    }

    private void initView() {

        this.setBackground(Colors.LEFT_WINDOW_BACKGROUND);
        this.setLayout(new GridBagLayout());
        this.add(selectUserListView, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 1).setInsets(0, 0, 5, 0));

    }


    public void setData(List<SearchResultItem> data) {
        leftUserList.clear();
        leftUserList.addAll(data.stream()
                .filter(e -> e.getType() == SearchResultType.CONTACTS.CODE || e.getType() == SearchResultType.ROOM.CODE)
                .map(e -> new SelectUserData(e.getId(), e.getName(), false))
                .toList()
        );

    }

    /**
     * 重绘整个列表
     */
    public void notifyDataSetChanged(boolean keepSize) {

        selectUserListView.notifyDataSetChanged(keepSize);
    }

    @Override
    public void setKeyWord(String keyWord) {

    }

    @Override
    public void setSearchMessageOrFileListener(SearchResultItemsAdapter.SearchMessageOrFileListener searchMessageOrFileListener) {

    }
}
