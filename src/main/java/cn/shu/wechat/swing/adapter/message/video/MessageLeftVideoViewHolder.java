package cn.shu.wechat.swing.adapter.message.video;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.VerticalFlowLayout;
import cn.shu.wechat.utils.FontUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created by 舒新胜 on 17-6-2.
 */

public class MessageLeftVideoViewHolder extends MessageVideoViewHolder {

    private boolean isGroup = true;


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

    private void initView() {
        setLayout(new BorderLayout());
        timePanel.add(time);

        contentTagPanel.setBackground(Colors.WINDOW_BACKGROUND);
        contentTagPanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));
        if (isGroup) {
            sender.setFont(FontUtil.getDefaultFont(12));
            sender.setForeground(Colors.FONT_GRAY);
            sender.setBorder(new EmptyBorder(0,0,5,0));
            contentTagPanel.add(sender);
        }

            videoComponent = getLayerPanel();
            videoComponent.setCursor(new Cursor(Cursor.HAND_CURSOR));
            JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));

        JPanel processBarPanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, true));
        processBarPanel.setOpaque(false);
        processBarPanel.add(videoComponent);
        processBarPanel.add(progressBar);

        controlPanel.add(processBarPanel);
            controlPanel.add(revoke);


        contentTagPanel.add(controlPanel);


        messageAvatarPanel.setLayout(new GridBagLayout());
        messageAvatarPanel.add(avatar, new GBC(1, 0).setWeight(1, 1).setAnchor(GBC.NORTH).setInsets(0, 5, 0, 0));
        messageAvatarPanel.add(contentTagPanel, new GBC(2, 0)
                .setWeight(1000, 1)
                .setAnchor(GBC.WEST)
                .setInsets(0, 5, 0, 0));
        add(timePanel, BorderLayout.NORTH);
        add(messageAvatarPanel, BorderLayout.CENTER);
    }

}
