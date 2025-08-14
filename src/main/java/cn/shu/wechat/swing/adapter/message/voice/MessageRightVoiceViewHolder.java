package cn.shu.wechat.swing.adapter.message.voice;

import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.message.RCRightAttachmentMessageBubble;
import cn.shu.wechat.swing.components.message.RCRightGreenMessageBubble;
import cn.shu.wechat.utils.IconUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.MouseListener;

/**
 *
 * @author 舒新胜
 * @date 17-6-2
 */

public class MessageRightVoiceViewHolder extends MessageVoiceViewHolder {

    public MessageRightVoiceViewHolder() {
        super();
        initComponents();
        initView();
        super.setListeners();
    }
    protected void initComponents(){
        super.initComponents();
        messageBubble = new RCRightGreenMessageBubble();
        progressBar.setBorder(new EmptyBorder(0, 0, 0, messageBubble.getSalientPointPixel()));
        voiceImgLabel.setIcon(IconUtil.getIcon(this, "/image/right_voice.png"));
    }
    protected void initView(){
        super.initView();
        messageBubble.add(gapText);
        messageBubble.add(durationText);
        messageBubble.add(unitLabel);
        messageBubble.add(voiceImgLabel);

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
