package cn.shu.wechat.swing.adapter.message.app;

import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.message.RCRightAppMessageBubble;

import javax.swing.*;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 8/15/2021 11:58
 */
public class MessageRightProgramOfAppViewHolder extends MessageProgramOfAppViewHolder{

    public MessageRightProgramOfAppViewHolder() {
        super(new RCRightAppMessageBubble());
        initView();
    }

    private void initView() {

        senderMessagePanel.add(messageBubble,-1);
        messageAvatarPanel.add(avatar, new GBC(2, 0).setWeight(1, 1).setAnchor(GBC.NORTH).setInsets(0, 5, 0, 5));
        messageAvatarPanel.add(senderMessagePanel, new GBC(1, 0)
                .setWeight(1000, 1)
                .setAnchor(GBC.EAST)
                .setInsets(0, 5, 0, 0));
    }

    public static void main(String[] args) {
        JFrame jFrame = new JFrame();
        jFrame.setSize(400,300);
        MessageRightProgramOfAppViewHolder holder = new MessageRightProgramOfAppViewHolder();

        holder.avatar.setIcon(new ImageIcon("E:\\JAVA\\project_idea\\AutoWeChat\\wechat\\src\\main\\resources\\image\\avatar.jpg"));
        holder.sourceName.setText("sourceName");
        holder.imageLabel.setIcon(new ImageIcon("E:\\JAVA\\project_idea\\AutoWeChat\\wechat\\src\\main\\resources\\image\\avatar.jpg"));

        jFrame.getContentPane().add(holder);
        jFrame.setVisible(true);
    }
}
