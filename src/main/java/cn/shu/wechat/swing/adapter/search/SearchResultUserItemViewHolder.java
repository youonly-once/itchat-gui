package cn.shu.wechat.swing.adapter.search;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.HighLightLabel;
import cn.shu.wechat.swing.components.RCBorder;
import cn.shu.wechat.utils.FontUtil;

import javax.swing.*;
import java.awt.*;

/**
 * 搜索结果每一个通讯录、房间项目
 * Created by 舒新胜 on 17-5-30.
 */
public class SearchResultUserItemViewHolder extends SearchResultItemViewHolder {
    public JLabel avatar = new JLabel();
    public HighLightLabel name = new HighLightLabel(null, Colors.MAIN_COLOR);
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
        name.setVerticalAlignment(SwingConstants.CENTER);

    }

    private void initView() {
        setLayout(new BorderLayout(5, 0));
        this.add(avatar, BorderLayout.WEST);
        this.add(name, BorderLayout.CENTER);
        this.add(type, BorderLayout.EAST);
    }
}
