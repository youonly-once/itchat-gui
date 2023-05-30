package cn.shu.wechat.swing.adapter;

import cn.shu.wechat.swing.entity.SelectUserData;
import cn.shu.wechat.swing.listener.AbstractMouseListener;
import cn.shu.wechat.swing.utils.AvatarUtil;
import cn.shu.wechat.swing.utils.CharacterParser;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.*;

/**
 * Created by 舒新胜 on 17-5-30.
 */
public class SelectedUserItemsAdapter extends BaseAdapter<SelectedUserItemViewHolder> {
    private final List<SelectUserData> userList;
    Map<Integer, String> positionMap = new HashMap<>();
    private ItemRemoveListener itemRemoveListener;
    private final List<SelectedUserItemViewHolder> viewHolders = new ArrayList<>();
    public SelectedUserItemsAdapter(List<SelectUserData> userList) {
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
    public SelectedUserItemViewHolder onCreateViewHolder(int viewType, int subViewType, int position) {
        //避免重复创建
        return new SelectedUserItemViewHolder();

    }

    @Override
    public void onBindViewHolder(SelectedUserItemViewHolder viewHolder, int position) {

        SelectUserData user = userList.get(position);


        ImageIcon imageIcon = AvatarUtil.createOrLoadUserAvatar(user.getUserName());
        viewHolder.avatar.setIcon(imageIcon);
        viewHolder.username = user.getUserName();
        // 名字
        viewHolder.disPlayNameLabel.setText(user.getDisplayName());
        viewHolder.icon.addMouseListener(new AbstractMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (itemRemoveListener != null) {
                    itemRemoveListener.onRemove(viewHolder.username);
                }
                super.mouseClicked(e);
            }
        });
    }


    private void processData() {
        userList.sort(new Comparator<SelectUserData>() {
            @Override
            public int compare(SelectUserData o1, SelectUserData o2) {
                String tc = CharacterParser.getSelling(o1.getDisplayName().toUpperCase());
                String oc = CharacterParser.getSelling(o2.getDisplayName().toUpperCase());
                return tc.compareTo(oc);
            }
        });

        int index = 0;
        String lastChara = "";
        for (SelectUserData user : userList) {
            String ch = CharacterParser.getSelling(user.getDisplayName()).substring(0, 1).toUpperCase();
            if (!ch.equals(lastChara)) {
                lastChara = ch;
                positionMap.put(index, ch);
            }

            index++;
        }
    }

    public void setItemRemoveListener(ItemRemoveListener itemRemoveListener) {
        this.itemRemoveListener = itemRemoveListener;
    }


    public interface ItemRemoveListener {
        void onRemove(String username);
    }

}
