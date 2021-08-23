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
        senderMessagePanel.add(revoke,-1);
        messageAvatarPanel.add(avatar, new GBC(2, 0).setWeight(1, 1).setAnchor(GBC.NORTH).setInsets(0, 5, 0, 5));
        messageAvatarPanel.add(senderMessagePanel, new GBC(1, 0)
                .setWeight(1000, 1)
                .setAnchor(GBC.EAST)
                .setInsets(0, 5, 0, 0));
    }

}
