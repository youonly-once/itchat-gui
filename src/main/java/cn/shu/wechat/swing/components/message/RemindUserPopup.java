package cn.shu.wechat.swing.components.message;

import cn.shu.wechat.swing.adapter.ViewHolder;
import cn.shu.wechat.swing.utils.AvatarUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Created by 舒新胜 on 21/06/2017.
 */
public class RemindUserPopup extends JPopupMenu {
    private List<String> users;
    private String roomId = "";
    private UserSelectedCallBack selectedCallBack;

    public RemindUserPopup() {
    }


    public RemindUserPopup(List<String> users) {
        this.users = users;
    }


    public void show(Component invoker, int x, int y, String roomId) {
        if (!roomId.equals(this.roomId)) {
            this.removeAll();
            this.initComponents();
            this.revalidate();
            this.roomId = roomId;
        }

        super.show(invoker, x, y);
    }

    public void reset() {
        this.roomId = "";
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    private void initComponents() {
        if (this.users != null) {
            this.setAutoscrolls(true);

            JMenuItem item = null;
            for (String user : users) {
                item = new JMenuItem(user);
                item.setUI(new RCRemindUserMenuItemUI(120, 25));
                //Image avatar = AvatarUtil.createOrLoadAvatar(user).getScaledInstance(15, 15, Image.SCALE_SMOOTH);
                //item.setUI(new RCRemindUserMenuItemUI(80, 25, avatar));
                //TODO 小头像 18 18
                item.setIcon(AvatarUtil.createOrLoadUserAvatar(user));
                item.setIconTextGap(-2);

                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (selectedCallBack != null) {
                            selectedCallBack.onSelected(((JMenuItem) e.getSource()).getText());
                        }
                    }
                });
                add(item);
            }
        }
    }

    public void setSelectedCallBack(UserSelectedCallBack selectedCallBack) {
        this.selectedCallBack = selectedCallBack;
    }

    public interface UserSelectedCallBack {
        void onSelected(String username);
    }

    private class UserItem extends ViewHolder {
        public UserItem() {
            add(new JLabel(System.currentTimeMillis() + ""));
        }
    }
}

