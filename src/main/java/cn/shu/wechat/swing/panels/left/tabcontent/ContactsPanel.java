package cn.shu.wechat.swing.panels.left.tabcontent;

import cn.shu.wechat.core.Core;
import cn.shu.wechat.swing.adapter.ContactsItemViewHolder;
import cn.shu.wechat.swing.adapter.ContactsItemsAdapter;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCListView;
import cn.shu.wechat.swing.panels.ParentAvailablePanel;
import cn.shu.wechat.swing.panels.left.LeftPanel;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by 舒新胜 on 17-5-30.
 */
public class ContactsPanel extends ParentAvailablePanel {
    @Getter
    private static ContactsPanel context;

    private RCListView<ContactsItemViewHolder, ContactsItemsAdapter> contactsListView;
    private final List<String> contactsItemList = new ArrayList<>();
    /**
     * 每次加载的联系人数量
     */
    public static final int initialCount = 10;
    /**
     * 已更新头像的联系人数量 从上往下
     */
    private final AtomicInteger loadedCount = new AtomicInteger(0);
    public ContactsPanel(JPanel parent) {
        super(parent);
        context = this;

        initComponents();
        initView();

    }


    private void initComponents() {
        contactsListView = new RCListView<>();
    }

    private void initView() {
        //绑定list到Adapter
        this.setBackground(Colors.LEFT_WINDOW_BACKGROUND);
        contactsListView.setAdapter(new ContactsItemsAdapter(contactsItemList));
        loadedCount.set(contactsItemList.size());
        setLayout(new GridBagLayout());
        contactsListView.setContentPanelBackground(Colors.WINDOW_BACKGROUND);
        contactsListView.setScrollBarColor(Colors.SCROLL_BAR_TRACK_LIGHT,Colors.WINDOW_BACKGROUND);
        contactsListView.getVerticalScrollBar().setUnitIncrement(ContactsItemViewHolder.HEIGHT);
        //滑轮滚动逐步加载
        contactsListView.setScrollListener((currValue, maxValue) -> {
            int visibleAmount = contactsListView.getVerticalScrollBar().getVisibleAmount();

            int count = initialCount;
            if (loadedCount.get() + count >= contactsItemList.size()) {
                count = contactsItemList.size() - loadedCount.get();
            }
            if (count <= 0) {
                return;
            }
            //到底了
            if (currValue + visibleAmount + contactsListView.getVerticalScrollBar().getUnitIncrement() >= maxValue) {
                contactsListView.notifyItemAppend(loadedCount.getAndAdd(count), count);
                contactsListView.getVerticalScrollBar().setValue(currValue - 50);
            }


        });
        add(contactsListView, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 1));
        java.util.Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!LeftTabContentPanel.getContext().getCurrentTab().equals(LeftPanel.CONTACTS)) {
                    if (contactsListView.getContentPanel().getComponentCount() > initialCount && !LeftTabContentPanel.getContext().getCurrentTab().equals(LeftPanel.CONTACTS)) {
                        SwingUtilities.invokeLater(() -> {
                            contactsListView.notifyItemRemoved(initialCount, contactsListView.getContentPanel().getComponentCount());
                            loadedCount.set(initialCount);
                        });
                    }
                }

            }
        }, 1000 * 60 * 10, 1000 * 60 * 20);
    }

    @Override
    public void setVisible(boolean aFlag) {
        if (aFlag) {
            initData();
        }
        super.setVisible(aFlag);
    }

    /**
     * 初始化数据，加载所有联系人到List中
     */
    public void initData() {
        contactsItemList.clear();
        contactsItemList.addAll(Core.getMemberMap().keySet());
        contactsListView.getAdapter().processData();

    }

    /**
     * 联系人数据刷新
     */
    public void notifyDataSetChanged() {
        ExecutorServiceUtil.getGlobalExecutorService().submit(() -> {
            loadedCount.set(0);
            initData();
            SwingUtilities.invokeLater(() -> {
                int count = Math.min(initialCount, contactsItemList.size());
                contactsListView.notifyItemAppend(loadedCount.getAndAdd(count), count);
            });
        });


    }


}
