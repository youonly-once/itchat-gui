package cn.shu.wechat.swing.adapter.search;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.HighLightLabel;
import cn.shu.wechat.swing.components.RCBorder;
import cn.shu.wechat.utils.FontUtil;

import javax.swing.*;
import java.awt.*;

/**
 * 搜索结果中的每一个文件项目
 * Created by 舒新胜 on 17-6-22.
 */
public class SearchResultFileItemViewHolder extends SearchResultItemViewHolder {
    public JLabel avatar = new JLabel();
    public HighLightLabel name = new HighLightLabel(null, Colors.MAIN_COLOR);
    public JLabel size = new JLabel();
    public JLabel dateTime = new JLabel();
    public SearchResultFileItemViewHolder() {
        initComponents();
        initView();

    }

    private void initComponents() {
        setPreferredSize(new Dimension(100, 50));
        setBackground(Colors.WINDOW_BACKGROUND);
        setBorder(new RCBorder(RCBorder.BOTTOM,Color.WHITE));
        setForeground(Colors.DARK);

        name.setFont(FontUtil.getDefaultFont(12));
        name.setForeground(Colors.DARK);
        name.setVerticalAlignment(SwingConstants.TOP);
        name.setPreferredSize(new Dimension(100, 35));

        dateTime.setForeground(Colors.FONT_GRAY);
        dateTime.setVisible(true);
        dateTime.setFont(FontUtil.getDefaultFont(10));

        size.setForeground(Colors.FONT_GRAY);
        size.setFont(FontUtil.getDefaultFont(12));


    }

    private void initView() {

        setLayout(new GridBagLayout());
        add(avatar, new GBC(0, 0).setWeight(2, 1)
                .setFill(GBC.BOTH).setInsets(0, 5, 0, 0).setAnchor(GridBagConstraints.CENTER)
                .setGridHeight(2));
        add(name, new GBC(1, 0).setWeight(100, 1).setFill(GBC.BOTH).setInsets(0, 5, 0, 0)
                .setAnchor(GridBagConstraints.CENTER));
        add(size, new GBC(2, 0).setWeight(1, 1).setFill(GBC.BOTH).setInsets(0, 3, 0, 0)
                .setAnchor(GridBagConstraints.CENTER));
        add(dateTime, new GBC(1, 1).setWeight(2, 1)
                .setFill(GBC.BOTH).setInsets(0, 5, 0, 0).setAnchor(GridBagConstraints.CENTER)
        );

        //add(size);

    }


}
