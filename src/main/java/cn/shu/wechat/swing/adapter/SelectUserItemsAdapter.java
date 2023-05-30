package cn.shu.wechat.swing.adapter;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.RCBorder;
import cn.shu.wechat.swing.entity.SelectUserData;
import cn.shu.wechat.swing.listener.AbstractMouseListener;
import cn.shu.wechat.swing.utils.AvatarUtil;
import cn.shu.wechat.swing.utils.CharacterParser;
import cn.shu.wechat.swing.utils.IconUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by 舒新胜 on 17-5-30.
 */
public class SelectUserItemsAdapter extends BaseAdapter<SelectUserItemViewHolder> {
    private final ImageIcon checkIcon;
    private final ImageIcon uncheckIcon;
    private List<SelectUserData> userList;
    private final List<SelectUserItemViewHolder> viewHolders = new ArrayList<>();
    Map<Integer, String> positionMap = new HashMap<>();
    private AbstractMouseListener mouseListener;

    public SelectUserItemsAdapter(List<SelectUserData> userList) {
        checkIcon = IconUtil.getIcon(this, "/image/check.png");
        uncheckIcon = IconUtil.getIcon(this, "/image/uncheck.png");
        setUserList(userList);
    }

    public void setUserList(List<SelectUserData> userList) {
        this.userList = userList;

        if (userList != null) {
            processData();
        }
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public SelectUserItemViewHolder onCreateViewHolder(int viewType, int subViewType, int position) {

        return new SelectUserItemViewHolder();
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
        holder.setBackground(Colors.LIGHT_GRAY);
        holder.setBorder(new RCBorder(RCBorder.BOTTOM, Colors.LIGHT_GRAY));
        holder.setOpaque(true);

        holder.letterLabel = new JLabel();
        holder.letterLabel.setText(holder.getLetter());
        holder.letterLabel.setForeground(Colors.FONT_GRAY_DARKER);

        holder.setLayout(new BorderLayout());
        holder.add(holder.letterLabel, BorderLayout.WEST);
    }

    @Override
    public void onBindViewHolder(SelectUserItemViewHolder viewHolder, int position) {
        viewHolders.add(position, viewHolder);
        String name = userList.get(position).getDisplayName();

        AvatarUtil.loadAvatar(userList.get(position).getUserName(),viewHolder.avatar);
        viewHolder.username = userList.get(position).getUserName();
        // 名字
        viewHolder.disPlayNameLabel.setText(name);

        if (userList.get(position).isSelected()) {
            viewHolder.icon.setIcon(checkIcon);
        } else {
            viewHolder.icon.setIcon(uncheckIcon);
        }

        viewHolder.addMouseListener(mouseListener);
    }


    private void processData() {
        userList.sort((o1, o2) -> {
            String tc = CharacterParser.getSelling(o1.getDisplayName().toUpperCase());
            String oc = CharacterParser.getSelling(o2.getDisplayName().toUpperCase());
            return tc.compareTo(oc);
        });

        int index = 0;
        String lastChara = "";
        for (SelectUserData item : userList) {
            String ch = CharacterParser.getSelling(item.getDisplayName()).substring(0, 1).toUpperCase();
            if (!ch.equals(lastChara)) {
                lastChara = ch;
                positionMap.put(index, ch);
            }
            index++;
        }
    }

    public void setMouseListener(AbstractMouseListener mouseListener) {
        this.mouseListener = mouseListener;
    }
}
