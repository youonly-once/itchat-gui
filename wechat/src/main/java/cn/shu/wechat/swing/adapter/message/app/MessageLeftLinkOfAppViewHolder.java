package cn.shu.wechat.swing.adapter.message.app;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.SizeAutoAdjustTextArea;
import cn.shu.wechat.swing.components.message.*;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.utils.FontUtil;
import lombok.Getter;

import javax.swing.border.EmptyBorder;

/**
 *
 * @author 舒新胜
 * @date 17-6-2
 */

public class MessageLeftLinkOfAppViewHolder extends MessageLinkOfAppViewHolder {
    public final SizeAutoAdjustTextArea sender = new SizeAutoAdjustTextArea((int)(MainFrame.getContext().currentWindowWidth * 0.5));
    private final boolean isGroup ;
    public MessageLeftLinkOfAppViewHolder(boolean isGroup) {
        super(new RCLeftLinkMessageBubble());
        this.isGroup = isGroup;
        initView();
    }

    private void initView() {
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
}
