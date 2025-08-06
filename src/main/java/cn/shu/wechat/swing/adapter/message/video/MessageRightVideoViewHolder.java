package cn.shu.wechat.swing.adapter.message.video;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.VerticalFlowLayout;
import cn.shu.wechat.utils.IconUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Created by 舒新胜 on 17-6-2.
 */

public class MessageRightVideoViewHolder extends MessageVideoViewHolder {

    public JLabel resend = new JLabel();
    public JLabel sendingProgress = new JLabel();

    /**
     *
     * @param dimension 缩略图尺寸
     */
    public MessageRightVideoViewHolder(Dimension dimension) {
        super(dimension);
        initComponents();
        initView();
    }

    protected void initComponents() {
        super.initComponents();
        ImageIcon resendIcon = IconUtil.getIcon(this,"/image/resend.png",20,20);
        resend.setIcon(resendIcon);
        resend.setVisible(false);
        resend.setToolTipText("图片发送失败，点击重新发送");
        resend.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void initView() {
        setLayout(new BorderLayout());
        timePanel.add(time);

        contentTagPanel.setBackground(Colors.WINDOW_BACKGROUND);
        contentTagPanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));

            videoComponent = getLayerPanel();
            videoComponent.setCursor(new Cursor(Cursor.HAND_CURSOR));
            JPanel controlPanel = new JPanel(new BorderLayout(0, 0));
            controlPanel.add(resend, BorderLayout.WEST);
            controlPanel.add(sendingProgress, BorderLayout.CENTER);
            controlPanel.add(revoke, BorderLayout.EAST);
            JPanel resendImagePanel = new JPanel(new BorderLayout());
            resendImagePanel.setOpaque(false);

            JPanel processBarPanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, true));
            processBarPanel.setOpaque(false);
            processBarPanel.add(videoComponent);
            processBarPanel.add(progressBar);

            resendImagePanel.add(processBarPanel, BorderLayout.CENTER);


        resendImagePanel.add(controlPanel, BorderLayout.WEST);

        contentTagPanel.add(resendImagePanel);


        messageAvatarPanel.setLayout(new GridBagLayout());
        messageAvatarPanel.add(avatar, new GBC(2, 0).setWeight(1, 1).setAnchor(GBC.NORTH).setInsets(0, 5, 0, 5));
        messageAvatarPanel.add(contentTagPanel, new GBC(1, 0)
                .setWeight(1000, 1)
                .setAnchor(GBC.EAST)
                .setInsets(0, 5, 0, 0));
        add(timePanel, BorderLayout.NORTH);
        add(messageAvatarPanel, BorderLayout.CENTER);
    }



}
