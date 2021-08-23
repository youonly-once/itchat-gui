package cn.shu.wechat.swing.adapter.message.app;

import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.message.RCRightLinkMessageBubble;
import lombok.Getter;

/**
 *
 * @author 舒新胜
 * @date 17-6-2
 */

public class MessageRightLinkOfAppViewHolder extends MessageLinkOfAppViewHolder {
    public MessageRightLinkOfAppViewHolder() {
        super(new RCRightLinkMessageBubble());
        initView();
    }

    private void initView() {
        //群消息显示发送者名称
        senderMessagePanel.add(messageBubble);
        senderMessagePanel.add(revoke);
        messageAvatarPanel.add(avatar, new GBC(2, 0).setWeight(1, 1).setAnchor(GBC.NORTH).setInsets(0, 5, 0, 5));
        messageAvatarPanel.add(senderMessagePanel, new GBC(1, 0)
                .setWeight(1000, 1)
                .setAnchor(GBC.EAST)
                .setInsets(0, 5, 0, 0));
    }
}
