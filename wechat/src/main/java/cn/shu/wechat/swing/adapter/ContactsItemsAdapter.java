package cn.shu.wechat.swing.adapter;

import cn.shu.wechat.core.Core;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.RCBorder;
import cn.shu.wechat.swing.entity.ContactsItem;
import cn.shu.wechat.swing.listener.AbstractMouseListener;
import cn.shu.wechat.swing.panels.RightPanel;
import cn.shu.wechat.swing.panels.RoomChatPanelCard;
import cn.shu.wechat.swing.panels.UserInfoPanel;
import cn.shu.wechat.swing.utils.CharacterParser;
import org.apache.commons.lang.StringUtils;

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
     * 所有联系人列表
     */
    private final List<ContactsItem> contactsItems;

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
    public ContactsItemViewHolder onCreateViewHolder(int viewType, int position) {
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
        for (int pos : positionMap.keySet()) {
            if (pos == position) {
                String ch = positionMap.get(pos);

                return new ContactsHeaderViewHolder(ch.toUpperCase());
            }
        }

        return null;
    }

    @Override
    public void onBindHeaderViewHolder(HeaderViewHolder viewHolder, int position) {
        ContactsHeaderViewHolder holder = (ContactsHeaderViewHolder) viewHolder;
        holder.setPreferredSize(new Dimension(100, 25));
        holder.setBackground(Colors.DARKER);
        holder.setBorder(new RCBorder(RCBorder.BOTTOM));
        holder.setOpaque(true);

        holder.letterLabel = new JLabel();
        holder.letterLabel.setText(holder.getLetter());
        holder.letterLabel.setForeground(Colors.FONT_GRAY);

        holder.setLayout(new BorderLayout());
        holder.add(holder.letterLabel, BorderLayout.WEST);
    }

    @Override
    public void onBindViewHolder(ContactsItemViewHolder viewHolder, int position) {

        ContactsItem item = contactsItems.get(position);

        if (item.getAvatar() != null){
            ImageIcon icon = item.getAvatar();
            viewHolder.avatar.setIcon(icon);
        }

        viewHolder.roomName.setText(item.getDisplayName());
        if (viewHolder.mouseListener!=null){
            viewHolder.removeMouseListener(viewHolder.mouseListener);
        }
        viewHolder.mouseListener =  new AbstractMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                UserInfoPanel.getContext().setContacts(Core.getMemberMap().get(item.getId()));
                RightPanel.getContext().show(RoomChatPanelCard.USER_INFO);

                setBackground(viewHolder, Colors.ITEM_SELECTED);
                selectedViewHolder = viewHolder;

                for (ContactsItemViewHolder holder : viewHolders) {
                    if (holder != viewHolder) {
                        setBackground(holder, Colors.DARK);
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (selectedViewHolder != viewHolder) {
                    setBackground(viewHolder, Colors.ITEM_SELECTED_DARK);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (selectedViewHolder != viewHolder) {
                    setBackground(viewHolder, Colors.DARK);
                }
            }
        };
        viewHolder.addMouseListener(viewHolder.mouseListener);
    }

    private void setBackground(ContactsItemViewHolder holder, Color color) {
        holder.setBackground(color);
    }

    public void processData() {
        Collections.sort(contactsItems);

        int index = 0;
        String lastChara = "";
        for (ContactsItem item : contactsItems) {
            String selling = CharacterParser.getSelling(item.getDisplayName());
            if (StringUtils.isEmpty(selling)) {
                selling = "NONE";
            }
            String ch = selling.substring(0, 1).toUpperCase();
            if (!ch.equals(lastChara)) {
                lastChara = ch;
                positionMap.put(index, ch);
            }

            index++;
        }
    }
}
