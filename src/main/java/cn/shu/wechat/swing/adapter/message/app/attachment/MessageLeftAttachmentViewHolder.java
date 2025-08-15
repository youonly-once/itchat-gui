package cn.shu.wechat.swing.adapter.message.app.attachment;

import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.message.RCLeftAttachmentMessageBubble;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseListener;

/**
 * Created by 舒新胜 on 17-6-2.
 */
public class MessageLeftAttachmentViewHolder extends MessageAttachmentViewHolder {

    private final boolean isGroup;

    public MessageLeftAttachmentViewHolder(boolean isGroup) {
        this.isGroup = isGroup;
        initComponents();
        initView();
        setListeners();
    }

    protected void initComponents() {
        super.initComponents();
        messageBubble = new RCLeftAttachmentMessageBubble(new Insets(8, 10, 8, 8));
        progressBar.setBorder(new EmptyBorder(0, messageBubble.getSalientPointPixel(), 0, 0));
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
                    .setAnchor(GBC.NORTHWEST).setInsets(-10, 5, 0, 0).setFill(GBC.NONE));
            newLine = 1;
        }

        add(messageBubble, new GBC(1, 1 + newLine).setWeight(0, 10)
                .setAnchor(GBC.NORTHWEST).setInsets(0, 5, 0, 0).setFill(GBC.NONE));


        add(progressBar, new GBC(1, 2 + newLine).setWeight(0, 10)
                .setAnchor(GBC.NORTHWEST).setInsets(0, 5, 0, 0).setFill(GBC.BOTH));

        add(revoke, new GBC(2, 1 + newLine).setWeight(1, 1)
                .setAnchor(GBC.WEST).setInsets(0, 5, 0, 0).setGridHeight(2));
        //占位，revoke被隐藏则messageBubble被拉伸到最右边，从而不能左对齐
        add(Box.createHorizontalStrut(5), new GBC(3, 1 + newLine).setWeight(1, 100)); // 占位行

    }
}
