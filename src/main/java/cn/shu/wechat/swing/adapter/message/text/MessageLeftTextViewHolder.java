package cn.shu.wechat.swing.adapter.message.text;

import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.SizeAutoAdjustTextArea;
import cn.shu.wechat.swing.components.message.RCLeftAttachmentMessageBubble;
import cn.shu.wechat.swing.frames.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author 舒新胜
 * @date 17-6-2
 */
public class MessageLeftTextViewHolder extends BaseMessageViewHolder {

    public SizeAutoAdjustTextArea text= new SizeAutoAdjustTextArea( (int) (MainFrame.getContext().currentWindowWidth * 0.5));

    public RCLeftAttachmentMessageBubble messageBubble = new RCLeftAttachmentMessageBubble(new Insets(2, 9, 5, 2));

    private final boolean isGroup;

    public MessageLeftTextViewHolder(boolean isGroup) {
        this.isGroup = isGroup;
        initComponents();
        initView();
    }

    private void initComponents() {
        text.setParseUrl(true);
    }

    private void initView() {
        setLayout(new GridBagLayout());
        messageBubble.add(text);

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
                    .setAnchor(GBC.NORTHWEST).setInsets(-10, 10, 0, 0).setFill(GBC.NONE));
            newLine = 1;
        }

        add(messageBubble, new GBC(1, 1 + newLine).setWeight(0, 10)
                .setAnchor(GBC.WEST).setInsets(0, 5, 0, 0).setFill(GBC.NONE));

        add(revoke, new GBC(2, 1 + newLine).setWeight(1, 1)
                .setAnchor(GBC.WEST).setInsets(0, 5, 0, 0));
        //占位，revoke被隐藏则messageBubble被拉伸到最右边，从而不能左对齐
        add(Box.createHorizontalStrut(1), new GBC(3, 1 + newLine).setWeight(1, 100)); // 占位行

    }
}
