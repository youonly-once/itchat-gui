package cn.shu.wechat.swing.adapter.search;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.HighLightLabel;
import cn.shu.wechat.swing.components.RCBorder;
import cn.shu.wechat.utils.FontUtil;

import javax.swing.*;
import java.awt.*;

/**
 * 搜索结果中的每一个消息项目
 * Created by 舒新胜 on 17-6-22.
 */
public class SearchResultMessageViewHolder extends SearchResultItemViewHolder {
    public JLabel avatar = new JLabel();
    public JLabel roomName = new JLabel();
    public HighLightLabel brief = new HighLightLabel();
    public JPanel nameBrief = new JPanel();
    public JLabel time = new JLabel();

    public SearchResultMessageViewHolder() {
        initComponents();
        initView();

    }

    private void initComponents() {
        setPreferredSize(new Dimension(100, 65));
        setBackground(Colors.WINDOW_BACKGROUND);
        setBorder(new RCBorder(RCBorder.BOTTOM));
        setOpaque(true);
        setForeground(Colors.DARK);


        roomName.setFont(FontUtil.getDefaultFont(14));
        roomName.setForeground(Colors.DARK);

        brief.setForeground(Colors.DARK);
        brief.setFont(FontUtil.getDefaultFont(12));

        nameBrief.setLayout(new BorderLayout());
        setBackground(Colors.WINDOW_BACKGROUND);
        nameBrief.add(roomName, BorderLayout.WEST);
        nameBrief.add(time, BorderLayout.EAST);

        time.setForeground(Colors.FONT_GRAY);
        time.setFont(FontUtil.getDefaultFont(12));
    }

    private void initView() {
        setLayout(new GridBagLayout());
        add(avatar, new GBC(0, 0).setWeight(2, 1).setFill(GBC.BOTH).setInsets(5, 5, 5, 0).setGridHeight(2));
        add(nameBrief, new GBC(1, 0).setWeight(50, 1).setFill(GBC.BOTH).setInsets(5, 5, 0, 0));
        add(brief, new GBC(1, 1).setWeight(50, 50).setFill(GBC.BOTH).setInsets(0, 5, 5, 0));

    }


}
