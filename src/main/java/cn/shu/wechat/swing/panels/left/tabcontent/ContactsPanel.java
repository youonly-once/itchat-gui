package cn.shu.wechat.swing.panels.left.tabcontent;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.pojo.entity.Contacts;
import cn.shu.wechat.swing.adapter.ContactsItemViewHolder;
import cn.shu.wechat.swing.adapter.ContactsItemsAdapter;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCListView;
import cn.shu.wechat.swing.entity.ContactsItem;
import cn.shu.wechat.swing.panels.ParentAvailablePanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by 舒新胜 on 17-5-30.
 */
public class ContactsPanel extends ParentAvailablePanel {
    private static ContactsPanel context;

    private RCListView contactsListView;
    private final List<ContactsItem> contactsItemList = new ArrayList<>();
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
        //绑定list到Adapter
        contactsListView.setAdapter(new ContactsItemsAdapter(contactsItemList));
        loadedCount.set(contactsItemList.size());
    }


    private void initComponents() {
        contactsListView = new RCListView();
    }

    private void initView() {
        setLayout(new GridBagLayout());
        contactsListView.setContentPanelBackground(Colors.WINDOW_BACKGROUND);
        contactsListView.setScrollBarColor(Colors.SCROLL_BAR_TRACK_LIGHT,Colors.WINDOW_BACKGROUND);
        contactsListView.getVerticalScrollBar().setUnitIncrement(ContactsItemViewHolder.HEIGHT);
        //滑轮滚动逐步加载
        contactsListView.setScrollListener(new RCListView.ScrollListener() {
            @Override
            public void onScroll(int currValue,int maxValue) {
                int visibleAmount = contactsListView.getVerticalScrollBar().getVisibleAmount();

                int count = initialCount;
                if (loadedCount.get()+count >= contactsItemList.size()){
                    count = contactsItemList.size() - loadedCount.get();
                }
                if (count <= 0){
                    return;
                }
                //到底了
                if (currValue + visibleAmount + contactsListView.getVerticalScrollBar().getUnitIncrement() >= maxValue){
                    contactsListView.notifyItemAppend(loadedCount.getAndAdd(count),count);
                    contactsListView.getVerticalScrollBar().setValue(currValue-50);
                }


            }
        });
        add(contactsListView, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 1));
    }

    /**
     * 初始化数据，加载所有联系人到List中
     */
    private void initData() {

        contactsItemList.clear();
        for (Map.Entry<String, Contacts> entry : Core.getMemberMap().entrySet()) {
            Contacts value = entry.getValue();
            ContactsItem item = ContactsItem.builder()
                    .id(entry.getKey())
                    .displayName(ContactsTools.getContactDisplayNameByUserName(entry.getKey()))
                    //.avatar()
                    .type(value.getType())
                    .build();
            contactsItemList.add(item);
        }


    }

    /**
     * 联系人数据刷新
     */
    public void notifyDataSetChanged() {
        new SwingWorker<Object,Object>(){

            @Override
            protected Object doInBackground() throws Exception {
                initData();
                loadedCount.set(0);
                ((ContactsItemsAdapter) contactsListView.getAdapter()).processData();
                return null;
            }

            @Override
            protected void done() {
                int count = Math.min(initialCount,contactsItemList.size());
                contactsListView.notifyItemAppend(loadedCount.getAndAdd(count),count);
            }
        }.execute();

    }



    /**
     * 更新联系人头像
     * @param contactId 联系人id
     * @param image 联系人头像
     */
    public void updateAvatar(String contactId, ImageIcon image) {
        for (int i = 0; i < contactsItemList.size(); i++) {
            ContactsItem contactsItem = contactsItemList.get(i);
            if (contactsItem.getId().equals(contactId)){
                updateAvatar(i,image);
            }
        }
    }

    /**
     * 更新联系人头像
     * @param pos 联系人位置
     * @param image 联系人头像
     */
    public void updateAvatar(int pos, ImageIcon image) {
        ContactsItem contactsItem = contactsItemList.get(pos);
        contactsItem.setAvatar(image);
        contactsListView.notifyItemChanged(pos);
    }

    public static ContactsPanel getContext() {
        return context;
    }


}
