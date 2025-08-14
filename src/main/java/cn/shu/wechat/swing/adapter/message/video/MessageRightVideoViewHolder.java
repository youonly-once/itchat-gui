package cn.shu.wechat.swing.adapter.message.video;

import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.utils.IconUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Created by 舒新胜 on 17-6-2.
 */

public class MessageRightVideoViewHolder extends MessageVideoViewHolder {

    public JLabel resend = new JLabel();
    public JLabel sendingProgress = new JLabel();

    /**
     *
     * @param dimension 缩略图尺寸
     */
    public MessageRightVideoViewHolder(Dimension dimension) {
        super(dimension);
        initComponents();
        initView();
    }

    protected void initComponents() {
        super.initComponents();
        resend.setIcon(IconUtil.getIcon(this,"/image/resend.png",20,20));
        resend.setVisible(false);
        resend.setToolTipText("图片发送失败，点击重新发送");
        resend.setCursor(new Cursor(Cursor.HAND_CURSOR));

    }

    protected void initView() {
        super.initView();

        add(time, new GBC(0, 0).setWeight(1, 1)
                .setAnchor(GBC.NORTH).setInsets(0, 0, 0, 0).setFill(GBC.HORIZONTAL).setGridWidth(3)
        );


        add(resend, new GBC(0, 1)
                .setWeight(1, 1)
                .setFill(GBC.NONE)
                .setAnchor(GBC.EAST)
                .setInsets(0, 0, 0, 5));


        add(sendingProgress, new GBC(0, 2)
                .setWeight(1, 1)
                .setAnchor(GBC.EAST)
                .setInsets(0, 0, 0, 5)
                .setFill(GBC.NONE));

        add(revoke, new GBC(0, 3)
                .setWeight(1, 1)
                .setFill(GBC.NONE)
                .setAnchor(GBC.EAST)
                .setInsets(0, 0, 0, 5));


        //占位，revoke被隐藏则messageBubble被拉伸到最左边，从而不能右对齐
        add(Box.createHorizontalStrut(5), new GBC(0, 4).setWeight(100, 100)); // 占位行


        add(contentLayeredPane, new GBC(1, 1).setWeight(0, 10)
                .setAnchor(GBC.NORTHEAST).setInsets(0, 0, 0, 5)
                .setFill(GBC.BOTH)
                .setGridHeight(3));

        add(avatar, new GBC(2, 1).setWeight(0, 1)
                .setAnchor(GBC.NORTHEAST).setInsets(0, 0, 0, 0).setFill(GBC.NONE)
                .setGridHeight(3));

    }



}
