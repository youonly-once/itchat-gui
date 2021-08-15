package cn.shu.wechat.swing.adapter.message.app;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.SizeAutoAdjustTextArea;
import cn.shu.wechat.swing.components.message.RCLeftAppMessageBubble;
import cn.shu.wechat.swing.utils.FontUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 8/15/2021 11:58
 */
public class MessageLeftProgramOfAppViewHolder extends MessageProgramOfAppViewHolder{
    public final SizeAutoAdjustTextArea sender = new SizeAutoAdjustTextArea(500);
    public MessageLeftProgramOfAppViewHolder(boolean isGroup) {
        super(new RCLeftAppMessageBubble());
        initView(isGroup);
    }

    private void initView(boolean isGroup) {
        //群消息显示发送者名称
        if (isGroup) {
            sender.setFont(FontUtil.getDefaultFont(12));
            sender.setForeground(Colors.FONT_GRAY);
            sender.setBorder(new EmptyBorder(0,messageBubble.getSalientPointPixel(),100,0));
            senderMessagePanel.add(sender,0);
        }
        senderMessagePanel.add(messageBubble,-1);
        messageAvatarPanel.add(avatar, new GBC(1, 0).setWeight(1, 1).setAnchor(GBC.NORTH).setInsets(0, 5, 0, 0));
        messageAvatarPanel.add(senderMessagePanel, new GBC(2, 0)
                .setWeight(1000, 1)
                .setAnchor(GBC.WEST)
                .setInsets(0, 5, 0, 0));
    }

    public static void main(String[] args) {
        JFrame jFrame = new JFrame();
        jFrame.setSize(400,300);
        MessageLeftProgramOfAppViewHolder holder = new MessageLeftProgramOfAppViewHolder(true);
        holder.sender.setText("发送者");
        holder.avatar.setIcon(new ImageIcon("E:\\JAVA\\project_idea\\AutoWeChat\\wechat\\src\\main\\resources\\image\\avatar.jpg"));
        holder.sourceName.setText("sourceName");
        holder.imageLabel.setIcon(new ImageIcon("E:\\JAVA\\project_idea\\AutoWeChat\\wechat\\src\\main\\resources\\image\\avatar.jpg"));

        jFrame.getContentPane().add(holder);
        jFrame.setVisible(true);

        byte[] bytes = "GIF".getBytes();
        for (byte aByte : bytes) {
            System.out.println(aByte);
        }
    }
}
