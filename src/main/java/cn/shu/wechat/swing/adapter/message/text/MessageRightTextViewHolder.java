package cn.shu.wechat.swing.adapter.message.text;

import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.SizeAutoAdjustTextArea;
import cn.shu.wechat.swing.components.message.RCRightImageMessageBubble;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.utils.IconUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Created by 舒新胜 on 17-6-2.
 */
public class MessageRightTextViewHolder extends BaseMessageViewHolder {

    public SizeAutoAdjustTextArea text;

    public RCRightImageMessageBubble messageBubble = new RCRightImageMessageBubble();

    // 重发按钮
    public JLabel resend = new JLabel();
    // 正在发送
    public JLabel sendingProgress = new JLabel();


    public MessageRightTextViewHolder() {
        initComponents();
        initView();
    }

    private void initComponents() {

        int maxWidth = (int) (MainFrame.getContext().currentWindowWidth * 0.5);
        text = new SizeAutoAdjustTextArea(maxWidth);
        text.setParseUrl(true);

        resend.setIcon(IconUtil.getIcon(this, "/image/resend.png", 20, 20));
        resend.setVisible(false);
        resend.setToolTipText("消息发送失败，点击重新发送");
        resend.setCursor(new Cursor(Cursor.HAND_CURSOR));

        sendingProgress.setIcon(IconUtil.getIcon(this, "/image/sending.gif"));
        sendingProgress.setVisible(false);


    }

    private void initView() {

        setLayout(new GridBagLayout());
        messageBubble.add(text);


        add(time, new GBC(0, 0).setWeight(1, 1)
                .setAnchor(GBC.NORTH).setInsets(0, 0, 0, 0)
                .setFill(GBC.HORIZONTAL)
                .setGridWidth(3)
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


        add(messageBubble, new GBC(1, 1).setWeight(0, 10)
                .setAnchor(GBC.CENTER).setInsets(0, 0, 0, 5)
                .setFill(GBC.NONE)
                .setGridHeight(3));

        add(avatar, new GBC(2, 1).setWeight(0, 1)
                .setAnchor(GBC.EAST).setInsets(0, 0, 0, 0).setFill(GBC.NONE)
                .setGridHeight(3));


    }

}
