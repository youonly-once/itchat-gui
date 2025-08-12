package cn.shu.wechat.swing.adapter.message.image;

import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.message.MessageImageLabel;
import cn.shu.wechat.swing.components.message.RCRightImageMessageBubble;
import cn.shu.wechat.utils.FontUtil;
import cn.shu.wechat.utils.IconUtil;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author 舒新胜
 * @date 17-6-3
 */
public class MessageRightImageViewHolder extends BaseMessageViewHolder {

    public MessageImageLabel image = new MessageImageLabel();

    public static final int maxHeight = 150;

    public static final int maxWidth = 100;

    public JLabel resend = new JLabel();

    public JLabel sendingProgress = new JLabel();

    private final Dimension imgSize;

    public MessageRightImageViewHolder(Dimension imgSize) {
        this.imgSize = imgSize;
        initComponents();
        initView();
    }

    private void initComponents() {

        resend.setIcon(IconUtil.getIcon(this,"/image/resend.png",20,20));
        resend.setVisible(false);
        resend.setToolTipText("图片发送失败，点击重新发送");
        resend.setCursor(new Cursor(Cursor.HAND_CURSOR));


        if (imgSize != null) {
            image.setPreferredSize(imgSize);
        }
        // 设置图标水平居中
        image.setHorizontalAlignment(SwingConstants.CENTER);
        // 设置图标垂直居中
        image.setVerticalAlignment(SwingConstants.CENTER);
    }

    private void initView() {
        setLayout(new GridBagLayout());


        add(time, new GBC(0, 0).setWeight(1, 1)
                .setAnchor(GBC.NORTH).setInsets(0, 0, 0, 0)
        );


        add(resend, new GBC(0, 1)
                .setWeight(1, 1)
                .setFill(GBC.NONE)
                .setAnchor(GBC.EAST)
                .setInsets(0, 0, 0, 5));


        add(sendingProgress, new GBC(0, 2)
                .setWeight(1, 1)
                .setAnchor(GBC.EAST)
                .setInsets(0, 0, 0, 5)
                .setFill(GBC.NONE));

        add(revoke, new GBC(0, 3)
                .setWeight(1, 1)
                .setFill(GBC.NONE)
                .setAnchor(GBC.EAST)
                .setInsets(0, 0, 0, 5));


        //占位，revoke被隐藏则messageBubble被拉伸到最左边，从而不能右对齐
        add(Box.createHorizontalStrut(5), new GBC(0, 4).setWeight(100, 100)); // 占位行


        add(image, new GBC(1, 1).setWeight(0, 10)
                .setAnchor(GBC.CENTER).setInsets(0, 0, 0, 5)
                .setFill(GBC.NONE)
                .setGridHeight(3));

        add(avatar, new GBC(2, 1).setWeight(0, 1)
                .setAnchor(GBC.EAST).setInsets(0, 0, 0, 0).setFill(GBC.NONE)
                .setGridHeight(3));

    }
}
