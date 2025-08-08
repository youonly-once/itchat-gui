package cn.shu.wechat.swing.adapter.message.app;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.message.RCLeftAppMessageBubble;
import cn.shu.wechat.utils.FontUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 8/15/2021 11:58
 */
public class MessageLeftProgramOfAppViewHolder extends MessageProgramOfAppViewHolder{
    public MessageLeftProgramOfAppViewHolder(boolean isGroup, Dimension size,String title) {
        super(new RCLeftAppMessageBubble(),size,title);
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
        senderMessagePanel.add(revoke,-1);
        messageAvatarPanel.add(avatar, new GBC(1, 0).setWeight(1, 1).setAnchor(GBC.NORTH).setInsets(0, 5, 0, 0));
        messageAvatarPanel.add(senderMessagePanel, new GBC(2, 0)
                .setWeight(1000, 1)
                .setAnchor(GBC.WEST)
                .setInsets(0, 5, 0, 0));
    }

}
