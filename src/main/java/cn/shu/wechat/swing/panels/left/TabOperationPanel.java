package cn.shu.wechat.swing.panels.left;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCBorder;
import cn.shu.wechat.swing.label.CornerMarkJLabel;
import cn.shu.wechat.swing.panels.ParentAvailablePanel;
import cn.shu.wechat.swing.panels.RightPanel;
import cn.shu.wechat.swing.panels.left.tabcontent.LeftTabContentPanel;
import cn.shu.wechat.swing.utils.IconUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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
    public static TabOperationPanel getContext() {
        return context;
    }

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
        chatLabel.setBorder(rcBorder);
        chatLabel.setHorizontalAlignment(JLabel.CENTER);
        chatLabel.setCursor(handCursor);
        chatLabel.addMouseListener(clickListener);

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
        setBackground(Colors.WINDOW_BACKGROUND);
        setBorder(new RCBorder(RCBorder.BOTTOM,Colors.SCROLL_BAR_TRACK_LIGHT));
        add(chatLabel, new GBC(0, 0).setFill(GBC.HORIZONTAL).setWeight(1, 1).setInsets(0, 10, 0, 10));
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
            RightPanel.getContext().show(RightPanel.USER_INFO);
            chatLabel.setIcon(chatIconNormal);
            contactsLabel.setIcon(contactIconActive);
            meLable.setIcon(meIconNormal);
            parent.getListPanel().showPanel(LeftTabContentPanel.CONTACTS);
            RightPanel.getContext().show(RightPanel.USER_INFO);
        } else if (e.getComponent() == meLable) {
            chatLabel.setIcon(chatIconNormal);
            contactsLabel.setIcon(contactIconNormal);
            meLable.setIcon(meIconActive);
            parent.getListPanel().showPanel(LeftTabContentPanel.COLLECTIONS);
        }
    }

    /**
     * 切换到聊天列表
     */
    public void switchToChatLabel() {
        chatLabel.setIcon(chatIconActive);
        contactsLabel.setIcon(contactIconNormal);
        meLable.setIcon(meIconNormal);
        parent.getListPanel().showPanel(LeftTabContentPanel.CHAT);
        RightPanel.getContext().show(RightPanel.CHAT_ROOM);
    }

    class TabItemClickListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            // 搜索框内容清空
            SearchPanel.getContext().clearSearchText();
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
