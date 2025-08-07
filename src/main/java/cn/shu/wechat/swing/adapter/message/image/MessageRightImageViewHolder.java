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

    public RCRightImageMessageBubble imageBubble = new RCRightImageMessageBubble();
    private final JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER,5,0));
    private final JPanel messageAvatarPanel = new JPanel();
    private final Dimension imgSize;
    public MessageRightImageViewHolder(Dimension imgSize) {
        this.imgSize = imgSize;
        initComponents();
        initView();
    }

    private void initComponents() {
        timePanel.setBackground(Colors.WINDOW_BACKGROUND);
        messageAvatarPanel.setBackground(Colors.WINDOW_BACKGROUND);

        //imageBubble.add(image);

        time.setForeground(Colors.FONT_GRAY);
        time.setFont(FontUtil.getDefaultFont(12));

        ImageIcon resendIcon = IconUtil.getIcon(this,"/image/resend.png");
        resendIcon.setImage(resendIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        resend.setIcon(resendIcon);
        resend.setVisible(false);
        resend.setToolTipText("图片发送失败，点击重新发送");
        resend.setCursor(new Cursor(Cursor.HAND_CURSOR));

       // ImageIcon sendingIcon = IconUtil.getIcon(this,"/image/sending.gif");
       // sendingProgress.setIcon(sendingIcon);
       // sendingProgress.setVisible(false);
    }

    private void initView() {
        setLayout(new BorderLayout());
        timePanel.add(time);
        if (imgSize != null) {
            image.setSize(imgSize);
        }
        // 设置图标水平居中
        image.setHorizontalAlignment(SwingConstants.CENTER);

        // 设置图标垂直居中
        image.setVerticalAlignment(SwingConstants.CENTER);
        JPanel resendImagePanel = new JPanel(new BorderLayout());
        resendImagePanel.setBackground(Colors.WINDOW_BACKGROUND);
        JPanel controlPanel = new JPanel(new BorderLayout(0, 0));
        controlPanel.add(resend, BorderLayout.WEST);
        controlPanel.add(sendingProgress, BorderLayout.CENTER);
        controlPanel.add(revoke, BorderLayout.EAST);
        resendImagePanel.add(controlPanel, BorderLayout.WEST);
        resendImagePanel.add(image, BorderLayout.CENTER);
        messageAvatarPanel.setLayout(new GridBagLayout());
        messageAvatarPanel.add(resendImagePanel, new GBC(1, 0).setWeight(1000, 1).setAnchor(GBC.EAST).setInsets(0, 0, 0, 5));
        messageAvatarPanel.add(avatar, new GBC(2, 0).setWeight(1, 1).setAnchor(GBC.NORTH).setInsets(0, 0, 0, 5));

        add(timePanel, BorderLayout.NORTH);
        add(messageAvatarPanel, BorderLayout.CENTER);
    }
}
