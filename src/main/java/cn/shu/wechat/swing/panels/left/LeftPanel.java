package cn.shu.wechat.swing.panels.left;

import cn.shu.wechat.core.Core;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.panels.left.tabcontent.CollectionsPanel;
import cn.shu.wechat.swing.panels.left.tabcontent.ContactsPanel;
import cn.shu.wechat.swing.panels.left.tabcontent.LeftTabContentPanel;
import cn.shu.wechat.swing.panels.left.tabcontent.RoomsPanel;
import cn.shu.wechat.swing.panels.search.ContactsSearchResultPanel;
import cn.shu.wechat.swing.panels.search.SearchPanel;
import lombok.Getter;

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
    @Getter
    private SearchPanel searchPanel;

    private TabOperationPanel mainOperationPanel;

    /**
     * list panel
     */
    private LeftTabContentPanel leftTabContentPanel;

    public static final String CHAT = "CHAT";
    public static final String CONTACTS = "CONTACTS";
    public static final String COLLECTIONS = "COLLECTIONS";
    public static final String SEARCH = "SEARCH";
    private RoomsPanel roomsPanel;
    private ContactsPanel contactsPanel;
    private CollectionsPanel collectionPanel;
    private ContactsSearchResultPanel searchResultPanel;


    public LeftPanel() {

        initComponents();
        initView();
    }

    private void initComponents() {

        //群panel
        roomsPanel = new RoomsPanel(this);
        //联系人
        contactsPanel = new ContactsPanel(this);
        //收藏panel
        // collectionPanel = new CollectionsPanel(this);

        myInfoPanel = new MyInfoPanel(this);

        mainOperationPanel = new TabOperationPanel(this);

        leftTabContentPanel = new LeftTabContentPanel(this, CHAT, CHAT);

        //搜索结果panel
        searchResultPanel = new ContactsSearchResultPanel(leftTabContentPanel, SEARCH);

        searchPanel = new SearchPanel(this, searchResultPanel, Core.getMemberMap().values());
    }

    private void initView() {
        this.setBackground(Colors.LEFT_WINDOW_BACKGROUND);
        this.setLayout(new GridBagLayout());

        leftTabContentPanel.add(roomsPanel, CHAT);
        leftTabContentPanel.add(contactsPanel, CONTACTS);
        // leftTabContentPanel.add(collectionPanel, COLLECTIONS);
        leftTabContentPanel.add(searchResultPanel, SEARCH);
        leftTabContentPanel.setBackground(Colors.LEFT_WINDOW_BACKGROUND);
        add(myInfoPanel, new GBC(0, 0).setAnchor(GBC.CENTER).setFill(GBC.BOTH).setWeight(1, 1).setInsets(0,0,0,0));
        add(searchPanel, new GBC(0, 1).setAnchor(GBC.CENTER).setFill(GBC.BOTH).setWeight(1, 1).setInsets(0,0,0,0));
        add(mainOperationPanel, new GBC(0, 2).setAnchor(GBC.CENTER).setFill(GBC.BOTH).setWeight(1, 1).setInsets(0,0,0,0));
        add(leftTabContentPanel, new GBC(0, 3).setAnchor(GBC.CENTER).setFill(GBC.BOTH).setWeight(1, 100).setInsets(0,0,0,0));
    }

    public LeftTabContentPanel getListPanel() {
        return this.leftTabContentPanel;
    }


}
