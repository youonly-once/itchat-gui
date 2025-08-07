package cn.shu.wechat.swing.adapter.message.video;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.VerticalFlowLayout;
import cn.shu.wechat.swing.components.message.TagPanel;
import cn.shu.wechat.utils.FontUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created by 舒新胜 on 17-6-2.
 */

public class MessageLeftVideoViewHolder extends MessageVideoViewHolder {

    private final boolean isGroup;


    /**
     *
     *  @param isGroup 是否为群消息
     * @param dimension 缩略图尺寸
     */
    public MessageLeftVideoViewHolder(boolean isGroup,Dimension dimension) {
        super(dimension);
        this.isGroup = isGroup;
        initComponents();
        initView();
    }

    protected void initComponents() {
        super.initComponents();
    }

    protected void initView() {
        super.initView();

        JPanel contentSender = new JPanel();
        if (isGroup) {
            contentSender.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));
            sender.setFont(FontUtil.getDefaultFont(12));
            sender.setForeground(Colors.FONT_GRAY);
            sender.setBorder(new EmptyBorder(0,0,5,0));
            contentSender.add(videoProgressBarPanel);
            contentSender.add(sender);
        }
        JPanel statusContentSender = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusContentSender.add(contentSender);
        statusContentSender.add(statusPanel);


        messageAvatarPanel.setLayout(new GridBagLayout());
        messageAvatarPanel.add(avatar, new GBC(1, 0).setWeight(1, 1).setAnchor(GBC.NORTH).setInsets(0, 5, 0, 0));
        messageAvatarPanel.add(statusContentSender, new GBC(2, 0)
                .setWeight(1000, 1)
                .setAnchor(GBC.WEST)
                .setInsets(0, 5, 0, 0));

    }

}
