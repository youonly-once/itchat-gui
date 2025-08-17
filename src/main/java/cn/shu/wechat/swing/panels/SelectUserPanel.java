package cn.shu.wechat.swing.panels;

import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.entity.SelectUserData;
import cn.shu.wechat.swing.adapter.selected.SelectUserItemViewHolder;
import cn.shu.wechat.swing.adapter.selected.SelectUserItemsAdapter;
import cn.shu.wechat.swing.adapter.selected.SelectedUserItemViewHolder;
import cn.shu.wechat.swing.adapter.selected.SelectedUserItemsAdapter;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCBorder;
import cn.shu.wechat.swing.components.RCListView;
import cn.shu.wechat.swing.listener.AbstractMouseListener;
import cn.shu.wechat.swing.panels.search.ForwardSearchResultPanel;
import cn.shu.wechat.swing.panels.search.SearchCardLayoutPanel;
import cn.shu.wechat.swing.panels.search.SearchPanel;
import cn.shu.wechat.utils.IconUtil;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by 舒新胜 on 19/06/2017.
 */
public class SelectUserPanel extends JPanel {
    private JPanel leftPanel;
    public static final String SEARCH = "SEARCH";
    private JPanel rightPanel;
    public static final String RECENT_CONTACTS = "CONTACTS";
    private final CardLayout cardLayout = new CardLayout();
    /*private JPanel buttonPanel;
    private JButton cancelButton;
    private JButton okButton;*/
    private final int width;
    private RCListView<SelectUserItemViewHolder, SelectUserItemsAdapter> selectUserListView;
    private RCListView<SelectedUserItemViewHolder, SelectedUserItemsAdapter> selectedUserListView;
    private final int height;
    private ForwardSearchResultPanel leftResultPanel;
    private SearchCardLayoutPanel leftCardPanel;
    private SearchPanel searchPanel;

    @Setter
    private Collection<Contacts> searchList;
    private List<SelectUserData> leftUserList;
    /**
     * 已选择的用户列表
     */
    private final List<SelectUserData> selectedUserList = new ArrayList<>();
    private SelectUserItemsAdapter selectUserItemsAdapter;
    private SelectedUserItemsAdapter selectedUserItemsAdapter;
    private ImageIcon checkIcon;
    private ImageIcon uncheckIcon;


    public SelectUserPanel(int width, int height, List<SelectUserData> leftUserList, Collection<Contacts> searchList) {
        this.width = width;
        this.height = height;
        this.leftUserList = leftUserList;
        this.searchList = searchList;
        initComponents();
        initView();
    }


    public void update() {
        selectUserListView.notifyDataSetChanged(false);
    }


    private void initComponents() {
        checkIcon = IconUtil.getIcon(this, "/image/check.png");
        uncheckIcon = IconUtil.getIcon(this, "/image/uncheck.png");

        leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(width / 2 - 1, height - 10));
        leftPanel.setBorder(new RCBorder(RCBorder.RIGHT, Colors.LIGHT_GRAY));


        rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(width / 2 - 1, height - 10));

        leftCardPanel = new SearchCardLayoutPanel(this, RECENT_CONTACTS, RECENT_CONTACTS);

        leftResultPanel = new ForwardSearchResultPanel(leftCardPanel, SEARCH, leftUserList);
        leftResultPanel.setPreferredSize(new Dimension(width / 2 - 1, height - 10));
        leftResultPanel.setBorder(new RCBorder(RCBorder.RIGHT, Colors.LIGHT_GRAY));

        searchPanel = new SearchPanel(this, leftResultPanel, searchList);
        // 选择用户列表
        selectUserListView = new RCListView<>();

        selectUserItemsAdapter = new SelectUserItemsAdapter(leftUserList);
        selectUserItemsAdapter.setMouseListener(new AbstractMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SelectUserItemViewHolder holder = (SelectUserItemViewHolder) e.getSource();

                String username = holder.username;
                if (unSelectUser(username)) {
                    holder.icon.setIcon(uncheckIcon);
                } else {
                    selectUser(username);
                    holder.icon.setIcon(checkIcon);
                }


            }
        });
        selectUserListView.setScrollBarColor(Colors.SCROLL_BAR_THUMB, Colors.WINDOW_BACKGROUND);
        selectUserListView.setAdapter(selectUserItemsAdapter);

        // 已选中用户列表
        selectedUserListView = new RCListView<>();
        selectedUserItemsAdapter = new SelectedUserItemsAdapter(selectedUserList);
        selectedUserItemsAdapter.setItemRemoveListener(username -> {
            if (unSelectUser(username)) {
                for (SelectUserItemViewHolder viewHolder : selectUserListView.getItems()) {
                    if (viewHolder.username.equals(username)) {
                        viewHolder.icon.setIcon(uncheckIcon);
                        break;
                    }
                }
            }
        });
        selectedUserListView.setScrollBarColor(Colors.SCROLL_BAR_THUMB, Colors.WINDOW_BACKGROUND);
        selectedUserListView.setAdapter(selectedUserItemsAdapter);
    }

    private void initView() {

// 中部面板，包含左/右两个子面板
        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        leftCardPanel.add(leftPanel, RECENT_CONTACTS);
        leftCardPanel.add(leftResultPanel, SEARCH);
        contentPanel.add(leftCardPanel);
        contentPanel.add(rightPanel);

// 左面板布局

        leftPanel.setLayout(new GridBagLayout());
        leftPanel.add(selectUserListView,
                new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 1).setInsets(0, 0, 5, 0));

// 原右面板布局
        rightPanel.setLayout(new GridBagLayout());
        rightPanel.add(selectedUserListView,
                new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 1));


// 最外层主面板，使用 BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        searchPanel.setPreferredSize(new Dimension(100, 50));

        //mainPanel.setPreferredSize(new Dimension(width / 2 - 1, height - 10));
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

// 添加到 JFrame 或父容器
        this.add(mainPanel);

        leftPanel.setLayout(new GridBagLayout());
        leftPanel.add(selectUserListView, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 1).setInsets(0, 0, 5, 0));

        rightPanel.setLayout(new GridBagLayout());
        rightPanel.add(selectedUserListView, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 1));
    }

    /**
     * 选择一位用户
     *
     * @param username
     */
    public void selectUser(String username) {
        for (SelectUserData item : leftUserList) {
            if (item.getUserName().equals(username)) {
                selectedUserList.add(item);
                selectedUserListView.notifyDataSetChanged(false);
                break;
            }
        }
    }

    public void selectUser(SelectUserData item) {

        selectedUserList.add(item);
        selectedUserListView.notifyDataSetChanged(false);

    }

    public boolean unSelectUser(String username) {
        Iterator<SelectUserData> itemIterator = selectedUserList.iterator();
        boolean dataChanged = false;
        while (itemIterator.hasNext()) {
            SelectUserData user = itemIterator.next();
            if (user.getUserName().equals(username)) {
                dataChanged = true;
                itemIterator.remove();
                break;
            }
        }

        if (dataChanged) {
            selectedUserListView.notifyDataSetChanged(false);
        }

        return dataChanged;
    }

    public List<SelectUserData> getSelectedUser() {
        return selectedUserList;
    }

    public void notifyDataSetChanged(List<SelectUserData> users) {
        leftUserList = users;
        selectUserItemsAdapter.setUserList(leftUserList);
        selectUserListView.notifyDataSetChanged(false);
    }

    public void setVisible(boolean aF) {
        if (!aF) {
            leftUserList.clear();
            searchList.clear();
        }
        super.setVisible(aF);
    }
}
