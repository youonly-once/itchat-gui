package cn.shu.wechat.swing.adapter.message.app.program;

import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.message.RCLeftAttachmentMessageBubble;
import cn.shu.wechat.swing.components.message.RCRightAttachmentMessageBubble;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 8/15/2021 11:58
 */
public class MessageRightProgramOfAppViewHolder extends MessageProgramOfAppViewHolder{

    public MessageRightProgramOfAppViewHolder(Dimension imgSize,String title) {
        super(imgSize,title);
        initComponents();
        initView();
        setListeners();
    }
    @Override
    protected void initComponents() {
        super.initComponents();
        messageBubble = new RCRightAttachmentMessageBubble(new Insets(5,10,5,15));
    }

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
