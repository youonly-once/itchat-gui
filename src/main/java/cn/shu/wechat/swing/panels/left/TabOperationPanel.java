package cn.shu.wechat.swing.panels.left;

import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.swing.adapter.room.RoomItemsAdapter;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCBorder;
import cn.shu.wechat.swing.label.CornerMarkJLabel;
import cn.shu.wechat.swing.panels.ParentAvailablePanel;
import cn.shu.wechat.swing.panels.RightPanel;
import cn.shu.wechat.swing.panels.left.tabcontent.ContactsPanel;
import cn.shu.wechat.swing.panels.left.tabcontent.RoomsPanel;
import cn.shu.wechat.utils.IconUtil;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Set;

/**
 * Created by 舒新胜 on 17-5-29.
 */
public class TabOperationPanel extends ParentAvailablePanel {
    public CornerMarkJLabel getChatLabel() {
        return chatLabel;
    }

    private CornerMarkJLabel chatLabel;
    private JLabel contactsLabel;
    private JLabel meLable;
    private TabItemClickListener clickListener;
    private ImageIcon chatIconActive;
    private ImageIcon chatIconNormal;
    private ImageIcon contactIconNormal;
    private ImageIcon contactIconActive;
    private ImageIcon meIconNormal;
    private ImageIcon meIconActive;
    JPanel chatPanel;
    @Getter
    private static TabOperationPanel context;
    private LeftPanel parent;

    public TabOperationPanel(JPanel parent) {
        super(parent);

        initComponents();
        initView();
        context = this;
    }

    private void initComponents() {
        Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
        clickListener = new TabItemClickListener();
        RCBorder rcBorder = new RCBorder(RCBorder.RIGHT,Colors.SCROLL_BAR_TRACK_LIGHT);
        rcBorder.setHeightScale(0.2F);

        chatIconActive = IconUtil.getIcon(this,"/image/chat_active.png");
        chatIconNormal = IconUtil.getIcon(this,"/image/chat_normal.png");

        chatLabel = new CornerMarkJLabel();
        chatLabel.setIcon(chatIconActive);

        chatLabel.setHorizontalAlignment(JLabel.CENTER);
        chatLabel.setCursor(handCursor);
        chatLabel.addMouseListener(clickListener);

        // 初始化右边的菜单按钮
        JLabel menuButton = new JLabel(); // 也可以换成一个小图标
        menuButton.setCursor(handCursor);
        menuButton.setIcon(IconUtil.getIcon(this,"/image/options.png"));

        // 创建菜单
        JPopupMenu filterMenu = new JPopupMenu();

        ;
        for (Contacts.ContactsType type : Contacts.ContactsType.values()) {
            Set<Contacts.ContactsType> visibleTypes = RoomItemsAdapter.getVisibleType();
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(type.desc,visibleTypes.contains(type));
            item.addActionListener(e -> {

                boolean changed = false;

                if (item.isSelected()) {
                    // 只有当集合中没有该元素时，才添加
                    if (!visibleTypes.contains(type) && visibleTypes.add(type)) {
                        changed = true;
                    }
                } else {
                    // 只有当集合中存在该元素时，才删除
                    if (visibleTypes.contains(type) && visibleTypes.remove(type)) {
                        changed = true;
                    }
                }

                // 只有列表真正变化才刷新 UI
                if (changed) {
                    RoomsPanel.getContext().updateAll();
                }
            });
            filterMenu.add(item);
        }

        // 给按钮绑定弹出菜单事件
        menuButton.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {

                    filterMenu.show(menuButton, 0, menuButton.getHeight());

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        // 下面假设你有一个 JPanel 存放 chatLabel 和按钮
         chatPanel = new JPanel(new GridBagLayout());
        chatPanel.setBorder(rcBorder);
        chatPanel.setBackground(chatLabel.getBackground());
        chatPanel.add(chatLabel, new GBC(0, 0).setFill(GBC.HORIZONTAL).setWeight(100, 1).setInsets(0, 10, 0, 10));
        chatPanel.add(menuButton, new GBC(1, 0).setFill(GBC.HORIZONTAL).setWeight(1, 1).setInsets(0, 10, 0, 10));

        // 最终把 chatPanel 添加到父容器
        parent = (LeftPanel) getParentPanel();
        parent.add(chatPanel);



        contactIconNormal = IconUtil.getIcon(this,"/image/contacts_normal.png");
        contactIconActive = IconUtil.getIcon(this,"/image/contacts_active.png");
        contactsLabel = new JLabel();
        contactsLabel.setIcon(contactIconNormal);
        //contactsLabel.setBorder(rcBorder);
        contactsLabel.setHorizontalAlignment(JLabel.CENTER);
        contactsLabel.setCursor(handCursor);
        contactsLabel.addMouseListener(clickListener);

        meIconNormal = IconUtil.getIcon(this,"/image/me_normal.png");
        meIconActive = IconUtil.getIcon(this,"/image/me_active.png");
        meLable = new JLabel();
        meLable.setIcon(meIconNormal);
        meLable.setHorizontalAlignment(JLabel.CENTER);
        meLable.setCursor(handCursor);
        meLable.addMouseListener(clickListener);

        parent = (LeftPanel) getParentPanel();
    }

    private void initView() {
        setLayout(new GridBagLayout());
        this.setBackground(Colors.LEFT_WINDOW_BACKGROUND);
        setBorder(new RCBorder(RCBorder.BOTTOM,Colors.SCROLL_BAR_TRACK_LIGHT));
        add(chatPanel, new GBC(0, 0).setFill(GBC.HORIZONTAL).setWeight(1, 1).setInsets(0, 10, 0, 10));
        add(contactsLabel, new GBC(1, 0).setFill(GBC.HORIZONTAL).setWeight(1, 1).setInsets(0, 10, 0, 10));
       // add(meLable, new GBC(2, 0).setFill(GBC.HORIZONTAL).setWeight(1, 1).setInsets(0, 10, 0, 10));
    }

    @Override
    protected void printBorder(Graphics g) {
        super.printBorder(g);
    }

    /**
     * 切换窗口
     *
     * @param e 事件
     */
    private void switchOperationPanel(MouseEvent e) {

        if (e.getComponent() == chatLabel) {
            switchToChatLabel();
        } else if (e.getComponent() == contactsLabel) {
            ContactsPanel.getContext().notifyDataSetChanged();
            RightPanel.getContext().show(RightPanel.USER_INFO);
            chatLabel.setIcon(chatIconNormal);
            contactsLabel.setIcon(contactIconActive);
            meLable.setIcon(meIconNormal);
            parent.getListPanel().showPanel(LeftPanel.CONTACTS);
            RightPanel.getContext().show(RightPanel.USER_INFO);
        } else if (e.getComponent() == meLable) {
            chatLabel.setIcon(chatIconNormal);
            contactsLabel.setIcon(contactIconNormal);
            meLable.setIcon(meIconActive);
            parent.getListPanel().showPanel(LeftPanel.COLLECTIONS);
        }
    }

    /**
     * 切换到聊天列表
     */
    public void switchToChatLabel() {
        chatLabel.setIcon(chatIconActive);
        contactsLabel.setIcon(contactIconNormal);
        meLable.setIcon(meIconNormal);
        parent.getListPanel().showPanel(LeftPanel.CHAT);
        RightPanel.getContext().show(RightPanel.CHAT_ROOM);
    }

    class TabItemClickListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            // 搜索框内容清空
            // SearchPanel.getContext().clearSearchText();
            switchOperationPanel(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }
}
