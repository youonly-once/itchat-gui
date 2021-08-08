package cn.shu.wechat.swing.adapter.message;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.VerticalFlowLayout;
import cn.shu.wechat.swing.components.message.*;
import cn.shu.wechat.swing.utils.FontUtil;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author 舒新胜
 * @date 17-6-2
 */
@Getter
public class MessageLeftLinkViewHolder extends MessageLinkViewHolder {
    private JLabel sender ;
    private final boolean isGroup ;
    public MessageLeftLinkViewHolder(boolean isGroup) {
        super(new RCLeftLinkMessageBubble());
        this.isGroup = isGroup;
        initView();
    }

    private void initView() {
        //群消息显示发送者名称
        if (isGroup) {
            sender = new JLabel();
            sender.setFont(FontUtil.getDefaultFont(12));
            sender.setForeground(Colors.FONT_GRAY);
            senderMessagePanel.add(sender,0);
        }
        senderMessagePanel.add(messageBubble,-1);
        messageAvatarPanel.add(avatar, new GBC(1, 0).setWeight(1, 1).setAnchor(GBC.NORTH).setInsets(0, 5, 0, 0));
        messageAvatarPanel.add(senderMessagePanel, new GBC(2, 0)
                .setWeight(1000, 1)
                .setAnchor(GBC.WEST)
                .setInsets(0, 5, 0, 0));
    }
}
