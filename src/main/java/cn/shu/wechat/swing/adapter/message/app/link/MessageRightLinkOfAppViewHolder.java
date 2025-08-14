package cn.shu.wechat.swing.adapter.message.app.link;

import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.message.RCLeftAttachmentMessageBubble;
import cn.shu.wechat.swing.components.message.RCRightAttachmentMessageBubble;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;

/**
 *
 * @author 舒新胜
 * @date 17-6-2
 */

public class MessageRightLinkOfAppViewHolder extends MessageLinkOfAppViewHolder {

    public MessageRightLinkOfAppViewHolder() {

    }
    @Override
    protected void setListeners() {
        super.setListeners();
    }

    @Override
    protected void initComponents() {
        super.initComponents();
        messageBubble = new RCRightAttachmentMessageBubble(new Insets(5,5,5,10));
    }
    protected void initView() {
        super.initView();
        add(time, new GBC(0, 0).setWeight(1, 1)
                .setAnchor(GBC.NORTH).setInsets(0, 0, 0, 0).setFill(GBC.HORIZONTAL).setGridWidth(3)
        );

        add(revoke, new GBC(0, 1)
                .setWeight(1, 1)
                .setFill(GBC.NONE)
                .setAnchor(GBC.EAST)
                .setInsets(0, 0, 0, 5));


        //占位，revoke被隐藏则messageBubble被拉伸到最左边，从而不能右对齐
        add(Box.createHorizontalStrut(5), new GBC(0, 2).setWeight(100, 100)); // 占位行


        add(messageBubble, new GBC(1, 1).setWeight(0, 10)
                .setAnchor(GBC.EAST).setInsets(0, 0, 0, 5)
                .setFill(GBC.NONE)
                .setGridHeight(3));

        add(avatar, new GBC(2, 1).setWeight(0, 1)
                .setAnchor(GBC.EAST).setInsets(0, 0, 0, 0).setFill(GBC.NONE)
                );
    }
}
