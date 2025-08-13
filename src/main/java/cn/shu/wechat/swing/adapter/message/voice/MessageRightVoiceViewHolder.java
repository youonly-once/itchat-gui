package cn.shu.wechat.swing.adapter.message.voice;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.VerticalFlowLayout;
import cn.shu.wechat.swing.components.message.RCRightVoiceMessageBubble;
import cn.shu.wechat.utils.IconUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 *
 * @author 舒新胜
 * @date 17-6-2
 */

public class MessageRightVoiceViewHolder extends MessageVoiceViewHolder {

    public MessageRightVoiceViewHolder() {
        super(new RCRightVoiceMessageBubble());
        initComponents();
        initView();
    }
    protected void initComponents(){
        super.initComponents();
        progressBar.setBorder(new EmptyBorder(0, 0, 0, messageBubble.getSalientPointPixel()));
    }
    protected void initView(){
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
                .setAnchor(GBC.NORTHEAST).setInsets(0, 0, 0, 5)
                .setFill(GBC.NONE)
                .setGridHeight(3));

        add(progressBar, new GBC(1, 2).setWeight(0, 10)
                .setAnchor(GBC.SOUTHEAST).setInsets(0, 0, 0, 5).setFill(GBC.HORIZONTAL));

        add(avatar, new GBC(2, 1).setWeight(0, 1)
                .setAnchor(GBC.EAST).setInsets(0, 0, 0, 0).setFill(GBC.NONE)
                .setGridHeight(2));
    }
}
