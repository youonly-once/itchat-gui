package cn.shu.wechat.swing.adapter.message.video;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.VerticalFlowLayout;
import cn.shu.wechat.swing.components.message.TagPanel;
import cn.shu.wechat.utils.FontUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created by 舒新胜 on 17-6-2.
 */

public class MessageLeftVideoViewHolder extends MessageVideoViewHolder {

    private final boolean isGroup;


    /**
     *
     *  @param isGroup 是否为群消息
     * @param dimension 缩略图尺寸
     */
    public MessageLeftVideoViewHolder(boolean isGroup,Dimension dimension) {
        super(dimension);
        this.isGroup = isGroup;
        initComponents();
        initView();
    }

    protected void initComponents() {
        super.initComponents();
    }

    protected void initView() {
        super.initView();



        add(time, new GBC(0, 0).setWeight(1, 1)
                .setAnchor(GBC.NORTH).setInsets(0, 0, 0, 0).setGridWidth(4)
                .setFill(GBC.HORIZONTAL));


        add(avatar, new GBC(0, 1).setWeight(0, 1)
                .setAnchor(GBC.NORTHWEST).setInsets(0, 5, 0, 0).setFill(GBC.NONE)
                .setGridHeight(2));
        int newLine = 0;
        if (isGroup) {
            //占位，当sender设置top为-10，而time被隐藏则sender被遮住
            add(Box.createVerticalStrut(10), new GBC(1, 0)
                    .setWeight(10, 100).setGridWidth(4)); // 占位行

            add(sender, new GBC(1, 1).setWeight(0, 1)
                    .setAnchor(GBC.NORTHWEST).setInsets(-10, 9, 0, 0).setFill(GBC.NONE));
            newLine = 1;
        }

        add(getLayerPanel(), new GBC(1, 1 + newLine).setWeight(0, 10)
                .setAnchor(GBC.CENTER).setInsets(0, 12, 0, 0).setFill(GBC.NONE));


        add(revoke, new GBC(2, 1 + newLine).setWeight(1, 1)
                .setAnchor(GBC.WEST).setInsets(0, 5, 0, 0));
        //占位，revoke被隐藏则messageBubble被拉伸到最右边，从而不能左对齐
        add(Box.createHorizontalStrut(5), new GBC(3, 1 + newLine).setWeight(1, 100)); // 占位行

    }

}
