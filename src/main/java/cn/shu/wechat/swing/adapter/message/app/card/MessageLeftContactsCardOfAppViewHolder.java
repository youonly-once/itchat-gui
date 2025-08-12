package cn.shu.wechat.swing.adapter.message.app.card;

import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.message.RCRightLinkMessageBubble;

/**
 * right ContactsCard of app
 * @author 舒新胜
 * @date 2021-09-29
 */


public class MessageLeftContactsCardOfAppViewHolder extends MessageContactsCardOfAppViewHolder {
    private final boolean isGroup ;
    public MessageLeftContactsCardOfAppViewHolder(boolean isGroup) {
        super(new RCRightLinkMessageBubble());
        this.isGroup = isGroup;
        initView();
    }

    private void initView() {
        //群消息显示发送者名称
        senderMessagePanel.add(messageBubble);
        senderMessagePanel.add(revoke);
        messageAvatarPanel.add(avatar, new GBC(1, 0).setWeight(1, 1).setAnchor(GBC.NORTH).setInsets(0, 5, 0, 0));
        messageAvatarPanel.add(senderMessagePanel, new GBC(2, 0)
                .setWeight(1000, 1)
                .setAnchor(GBC.WEST)
                .setInsets(0, 5, 0, 0));
    }
}
