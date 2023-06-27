package cn.shu.wechat.swing.components.message;

import cn.shu.wechat.core.Core;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.RCMainOperationMenuItemUI;
import cn.shu.wechat.swing.frames.CreateGroupDialog;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.frames.SystemConfigDialog;
import cn.shu.wechat.swing.panels.ChartPopupMenu;
import cn.shu.wechat.swing.utils.IconUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 *
 * @author 舒新胜
 * @date 2017/6/5
 */
public class MainOperationPopupMenu extends JPopupMenu {
    public MainOperationPopupMenu() {
        initMenuItem();
    }

    private void initMenuItem() {
        JMenuItem createRoomMenuItem = new JMenuItem("创建群聊");
        JMenuItem statistics = new JMenuItem("好友统计");
        JMenuItem setting = new JMenuItem("设置");
        JMenuItem lock = new JMenuItem("锁屏");

        createRoomMenuItem.setUI(new RCMainOperationMenuItemUI());
        createRoomMenuItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCreateGroupDialog();
            }
        });
        ImageIcon icon1 = IconUtil.getIcon(this,"/image/chat.png");
        icon1.setImage(icon1.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        createRoomMenuItem.setIcon(icon1);
        createRoomMenuItem.setIconTextGap(5);
        ImageIcon icon = IconUtil.getIcon(this, "/image/chart.png");
        icon.setImage(icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        createRoomMenuItem.setIcon(icon1);
        statistics.setIcon(icon);
        statistics.addActionListener(e -> {

            ChartPopupMenu instance = ChartPopupMenu.getInstance(Core.getUserName());
            int posX =  (MainFrame.getContext().currentWindowWidth - instance.getWidth()) / 2;
            int  posY =  (MainFrame.getContext().currentWindowHeight - instance.getHeight()) / 2;

            instance.show(MainFrame.getContext(), posX, posY);
        });
        statistics.setIconTextGap(5);
        statistics.setUI(new RCMainOperationMenuItemUI());
        setting.setUI(new RCMainOperationMenuItemUI());
        setting.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                SystemConfigDialog dialog = new SystemConfigDialog(MainFrame.getContext(), true);
                dialog.setVisible(true);
            }
        });
        ImageIcon icon2 = IconUtil.getIcon(this,"/image/setting.png");
        icon2.setImage(icon2.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        setting.setIcon(icon2);
        setting.setIconTextGap(5);

        lock.setUI(new RCMainOperationMenuItemUI());
        lock.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainFrame.getContext().lock();
            }
        });
        ImageIcon lockIcon = IconUtil.getIcon(this, "/image/lock.png");
        lockIcon.setImage(lockIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        lock.setIcon(lockIcon);
        lock.setIconTextGap(5);

        this.add(createRoomMenuItem);
        this.add(statistics);
        this.add(setting);
        this.add(lock);

        setBorder(new LineBorder(Colors.SCROLL_BAR_TRACK_LIGHT));
        setBackground(Colors.FONT_WHITE);
    }

    /**
     * 弹出创建群聊窗口
     */
    private void showCreateGroupDialog() {
        CreateGroupDialog dialog = new CreateGroupDialog(null, true);
        dialog.setVisible(true);

    }
}
