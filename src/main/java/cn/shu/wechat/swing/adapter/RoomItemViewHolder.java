package cn.shu.wechat.swing.adapter;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCBorder;
import cn.shu.wechat.swing.utils.FontUtil;
import cn.shu.wechat.swing.utils.IconUtil;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author 舒新胜
 * @date 17-5-30
 */
public class RoomItemViewHolder extends ViewHolder {
    public JLabel avatar = new JLabel();
    public JLabel roomName = new JLabel();
    public JLabel brief = new JLabel();
    public JPanel nameBrief = new JPanel();
    public JLabel time = new JLabel();
    public JLabel unreadCount = new JLabel();
    public JLabel mutePoint = new JLabel();
    public JPanel timeUnread = new JPanel();
    public static final int HEIGHT = 64;
    private Object tag;
    protected RoomItemsAdapter.RoomItemAbstractMouseListener mouseListener;
    public RoomItemViewHolder() {
        initComponents();
        initView();

    }

    private void initComponents() {
        setPreferredSize(new Dimension(100, HEIGHT));
        setBackground(Colors.WINDOW_BACKGROUND);
        setBorder(new RCBorder(RCBorder.BOTTOM, Colors.SCROLL_BAR_TRACK_LIGHT));
        setOpaque(true);
        setForeground(Colors.DARK);




        roomName.setFont(FontUtil.getDefaultFont(14));
        roomName.setForeground(Colors.DARK);

        brief.setForeground(Colors.FONT_GRAY);
        brief.setFont(FontUtil.getDefaultFont(12));

        nameBrief.setLayout(new BorderLayout());
        nameBrief.setBackground(Colors.WINDOW_BACKGROUND);
        nameBrief.add(roomName, BorderLayout.NORTH);
        nameBrief.add(brief, BorderLayout.CENTER);

        time.setForeground(Colors.FONT_GRAY);
        time.setFont(FontUtil.getDefaultFont(12));

        unreadCount.setIcon(IconUtil.getIcon(this,"/image/count_bg.png"));
        unreadCount.setFont(FontUtil.getDefaultFont(12));
        unreadCount.setPreferredSize(new Dimension(10, 10));
        unreadCount.setForeground(Color.WHITE);
        unreadCount.setHorizontalTextPosition(SwingConstants.CENTER);
        unreadCount.setHorizontalAlignment(SwingConstants.CENTER);
        unreadCount.setVerticalAlignment(SwingConstants.CENTER);
        unreadCount.setVerticalTextPosition(SwingConstants.CENTER);

        mutePoint.setIcon(IconUtil.getIcon(this,"/image/red_point.png"));
        mutePoint.setVisible(false);
        mutePoint.setHorizontalAlignment(SwingConstants.CENTER);
        mutePoint.setVerticalAlignment(SwingConstants.CENTER);

        timeUnread = new JPanel();
        timeUnread.setLayout(new BorderLayout());
        timeUnread.setBackground(Colors.WINDOW_BACKGROUND);
        timeUnread.add(time, BorderLayout.NORTH);

        timeUnread.add(unreadCount, BorderLayout.CENTER);

    }

    private void initView() {
        setLayout(new GridBagLayout());
        add(avatar, new GBC(0, 0).setWeight(2, 1).setFill(GBC.BOTH).setInsets(0, 5, 0, 0));
        add(nameBrief, new GBC(1, 0).setWeight(100, 1).setFill(GBC.BOTH).setInsets(5, 5, 0, 0));
        add(timeUnread, new GBC(2, 0).setWeight(1, 1).setFill(GBC.BOTH).setInsets(5, 0, 0, 0));

    }


    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }
}
