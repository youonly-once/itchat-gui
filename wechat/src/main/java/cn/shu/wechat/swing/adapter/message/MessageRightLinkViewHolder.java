package cn.shu.wechat.swing.adapter.message;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.message.RCLeftImageMessageBubble;
import cn.shu.wechat.swing.components.message.RCRightImageMessageBubble;
import cn.shu.wechat.swing.components.message.RCRightLinkMessageBubble;
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
public class MessageRightLinkViewHolder extends MessageLinkViewHolder {
    public MessageRightLinkViewHolder() {
        super(new RCRightLinkMessageBubble());
        initView();
    }

    private void initView() {
        //群消息显示发送者名称
        senderMessagePanel.add(messageBubble);
        messageAvatarPanel.add(avatar, new GBC(2, 0).setWeight(1, 1).setAnchor(GBC.NORTH).setInsets(0, 5, 0, 0));
        messageAvatarPanel.add(senderMessagePanel, new GBC(1, 0)
                .setWeight(1000, 1)
                .setAnchor(GBC.EAST)
                .setInsets(0, 5, 0, 0));
    }
}
