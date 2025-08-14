package cn.shu.wechat.swing.adapter.message.app.card;

import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.message.RCLeftAttachmentMessageBubble;
import cn.shu.wechat.swing.components.message.RCRightAttachmentMessageBubble;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;

/**
 * right ContactsCard of app
 * @author 舒新胜
 * @date 2021-09-29
 */


public class MessageRightContactsCardOfAppViewHolder extends MessageContactsCardOfAppViewHolder {
    public MessageRightContactsCardOfAppViewHolder() {
        super();

    }

    @Override
    protected void initComponents() {
        super.initComponents();
        messageBubble = new RCRightAttachmentMessageBubble(new Insets(2, 2, 5, 13));
    }
    @Override
    protected void initView() {
        super.initView();
        setLayout(new GridBagLayout());

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
