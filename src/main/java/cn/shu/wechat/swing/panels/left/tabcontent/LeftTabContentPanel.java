package cn.shu.wechat.swing.panels.left.tabcontent;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.panels.*;

import javax.swing.*;
import java.awt.*;

/**
 * Created by 舒新胜 on 17-5-30.
 */
public class LeftTabContentPanel extends ParentAvailablePanel {
    private static LeftTabContentPanel context;
    private RoomsPanel roomsPanel;
    private ContactsPanel contactsPanel;
    private CollectionsPanel collectionPanel;
    private SearchResultPanel searchResultPanel;

    public static final String CHAT = "CHAT";
    public static final String CONTACTS = "CONTACTS";
    public static final String COLLECTIONS = "COLLECTIONS";
    public static final String SEARCH = "SEARCH";

    private String previousTab = CHAT;
    private String currentTab = CHAT;

    private final CardLayout cardLayout = new CardLayout();


    public LeftTabContentPanel(JPanel parent) {
        super(parent);
        context = this;

        initComponents();
        initView();
    }


    private void initComponents() {
        //群panel
        roomsPanel = new RoomsPanel(this);
        //联系人
        contactsPanel = new ContactsPanel(this);
        //收藏panel
        collectionPanel = new CollectionsPanel(this);
        //搜索结果panel
        searchResultPanel = new SearchResultPanel(this);

    }

    private void initView() {
        this.setLayout(cardLayout);
        setBackground(Colors.WINDOW_BACKGROUND);
        add(roomsPanel, CHAT);
        add(contactsPanel, CONTACTS);
        add(collectionPanel, COLLECTIONS);
        add(searchResultPanel, SEARCH);
    }

    /**
     * 显示指定的card
     *
     * @param who
     */
    public void showPanel(String who) {
        previousTab = currentTab;
        if (!who.equals(SEARCH)) {
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
