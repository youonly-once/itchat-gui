package cn.shu.wechat.swing.adapter;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.pojo.entity.Contacts;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.RCBorder;
import cn.shu.wechat.swing.entity.ContactsItem;
import cn.shu.wechat.swing.listener.AbstractMouseListener;
import cn.shu.wechat.swing.panels.RightPanel;
import cn.shu.wechat.swing.panels.UserInfoPanel;
import cn.shu.wechat.swing.utils.AvatarUtil;
import cn.shu.wechat.swing.utils.FontUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;

/**
 * Created by 舒新胜 on 17-5-30.
 */
public class ContactsItemsAdapter extends BaseAdapter<ContactsItemViewHolder> {

    /**
     * 当前显示的联系人列表
     */
    private final List<ContactsItem> contactsItems;

    private  int count = 0;
    /**
     * 所有联系人Holders
     */
    private final List<ContactsItemViewHolder> viewHolders = new ArrayList<>();

    @Override
    public Map<Integer, String> getPositionMap() {
        return positionMap;
    }

    Map<Integer, String> positionMap = new TreeMap<>();

    /**
     * 当前被选中的联系人
     */
    private ContactsItemViewHolder selectedViewHolder;

    public ContactsItemsAdapter(List<ContactsItem> contactsItems) {
        this.contactsItems = contactsItems;

        if (contactsItems != null) {
            processData();
        }
    }

    @Override
    public int getCount() {
        return contactsItems.size();
    }

    @Override
    public ContactsItemViewHolder onCreateViewHolder(int viewType, int subViewType, int position) {

        //避免重复创建
        ContactsItemViewHolder contactsItemViewHolder;
        if (viewHolders.size() > position){
            //存在
            contactsItemViewHolder = viewHolders.get(position);
            if (contactsItemViewHolder == null){
                contactsItemViewHolder = new ContactsItemViewHolder();
                viewHolders.set(position,contactsItemViewHolder);
            }
        }else{
            contactsItemViewHolder = new ContactsItemViewHolder();
            viewHolders.add(position,contactsItemViewHolder);
        }
        return contactsItemViewHolder;
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder(int viewType, int position) {
        if (positionMap.containsKey(position)){
            return new ContactsHeaderViewHolder(positionMap.get(position));
        }
        return null;
    }

    @Override
    public void onBindHeaderViewHolder(HeaderViewHolder viewHolder, int position) {
        ContactsHeaderViewHolder holder = (ContactsHeaderViewHolder) viewHolder;
        holder.setPreferredSize(new Dimension(100, 25));
        holder.setBackground(Colors.SCROLL_BAR_TRACK_LIGHT);
        holder.setBorder(new RCBorder(RCBorder.BOTTOM, Colors.BG_GRAY));
        holder.setOpaque(true);

        holder.letterLabel = new JLabel();
        holder.letterLabel.setText(holder.getLetter());
        holder.letterLabel.setForeground(Colors.DARK);
        holder.letterLabel.setFont(FontUtil.getDefaultFont(14, Font.BOLD));
        holder.setLayout(new BorderLayout());
        holder.add(holder.letterLabel, BorderLayout.WEST);
    }

    @Override
    public void onBindViewHolder(ContactsItemViewHolder viewHolder, int position) {

        ContactsItem item = contactsItems.get(position);

        new SwingWorker<Object,Object>(){
            ImageIcon orLoadAvatar = null;
            @Override
            protected Object doInBackground() throws Exception {
                orLoadAvatar = AvatarUtil.createOrLoadUserAvatar(item.getId());
                return null;
            }

            @Override
            protected void done() {
                if (orLoadAvatar != null){
                    viewHolder.avatar.setIcon(orLoadAvatar);
                }
            }
        }.execute();

        viewHolder.roomName.setText(item.getDisplayName());
        if (viewHolder.mouseListener!=null){
            viewHolder.removeMouseListener(viewHolder.mouseListener);
        }
        viewHolder.mouseListener =  new AbstractMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                UserInfoPanel.getContext().setContacts(Core.getMemberMap().get(item.getId()));
                RightPanel.getContext().show(RightPanel.USER_INFO);

                setBackground(viewHolder, Colors.SCROLL_BAR_TRACK_LIGHT);
                selectedViewHolder = viewHolder;

                for (ContactsItemViewHolder holder : viewHolders) {
                    if (holder != viewHolder) {
                        setBackground(holder, Colors.WINDOW_BACKGROUND);
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (selectedViewHolder != viewHolder) {
                    setBackground(viewHolder, Colors.ITEM_SELECTED_LIGHT);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (selectedViewHolder != viewHolder) {
                    setBackground(viewHolder, Colors.WINDOW_BACKGROUND);
                }
            }
        };
        viewHolder.addMouseListener(viewHolder.mouseListener);
    }

    private void setBackground(ContactsItemViewHolder holder, Color color) {
        holder.setBackground(color);
    }

    public void processData() {

        positionMap.clear();
        Collections.sort(contactsItems);
        int index = 0;
        String lastChara = "";
        for (ContactsItem item : contactsItems) {

            if (item.getType()!= Contacts.ContactsType.ORDINARY_USER){
                if (!item.getType().desc.equals(lastChara)){
                    lastChara = item.getType().desc;
                    positionMap.put(index, item.getType().desc);
                }
            }else{
                String selling = ContactsTools.getContactDisplayNameInitialByUserName(item.getId());
                if (StringUtils.isEmpty(selling)) {
                    selling = "#";
                }
                String ch = selling.substring(0, 1).toUpperCase();
                if (!ch.equals(lastChara)) {
                    lastChara = ch;
                    positionMap.put(index, ch);
                }
            }


            index++;
        }
    }
}
