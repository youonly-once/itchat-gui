package cn.shu.wechat.swing.adapter.search;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.HighLightLabel;
import cn.shu.wechat.swing.components.RCBorder;
import cn.shu.wechat.swing.utils.FontUtil;

import javax.swing.*;
import java.awt.*;

/**
 * 搜索结果每一个通讯录、房间项目
 * Created by 舒新胜 on 17-5-30.
 */
public class SearchResultUserItemViewHolder extends SearchResultItemViewHolder {
    public JLabel avatar = new JLabel();
    public HighLightLabel name = new HighLightLabel();
    public JLabel type = new JLabel();
    public SearchResultUserItemViewHolder() {
        initComponents();
        initView();
    }

    private void initComponents() {
        setPreferredSize(new Dimension(100, 50));
        setBackground(Colors.WINDOW_BACKGROUND);
        setBorder(new RCBorder(RCBorder.BOTTOM,Color.WHITE));
        setOpaque(true);
        setForeground(Colors.DARK);


        name.setFont(FontUtil.getDefaultFont(14));
        name.setForeground(Colors.DARK);

    }

    private void initView() {
        setLayout(new GridBagLayout());
        add(avatar, new GBC(0, 0).setWeight(2, 1).setFill(GBC.BOTH).setInsets(0, 5, 0, 0));
        add(name, new GBC(1, 0).setWeight(100, 1).setFill(GBC.BOTH).setInsets(3, 5, 0, 0));
        add(type,new GBC(2, 0).setWeight(1, 1).setFill(GBC.BOTH).setInsets(3, 5, 0, 0));
    }
}
