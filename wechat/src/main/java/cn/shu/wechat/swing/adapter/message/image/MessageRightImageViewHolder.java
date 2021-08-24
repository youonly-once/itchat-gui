package cn.shu.wechat.swing.adapter.message.image;

import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.message.MessageImageLabel;
import cn.shu.wechat.swing.components.message.MessagePopupMenu;
import cn.shu.wechat.swing.components.message.RCRightImageMessageBubble;
import cn.shu.wechat.swing.utils.FontUtil;
import cn.shu.wechat.swing.utils.IconUtil;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author 舒新胜
 * @date 17-6-3
 */
public class MessageRightImageViewHolder extends BaseMessageViewHolder {
    public MessageImageLabel image = new MessageImageLabel();

    public JLabel resend = new JLabel();
    public JLabel sendingProgress = new JLabel();

    public RCRightImageMessageBubble imageBubble = new RCRightImageMessageBubble();
    private final JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER,5,0));
    private final JPanel messageAvatarPanel = new JPanel();

    public MessageRightImageViewHolder() {
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

        ImageIcon sendingIcon = IconUtil.getIcon(this,"/image/sending.gif");
        sendingProgress.setIcon(sendingIcon);
        sendingProgress.setVisible(false);
    }

    private void initView() {
        setLayout(new BorderLayout());
        timePanel.add(time);

        JPanel resendImagePanel = new JPanel(new BorderLayout());
        resendImagePanel.setBackground(Colors.WINDOW_BACKGROUND);
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        controlPanel.add(resend);
        controlPanel.add(sendingProgress);
        controlPanel.add(revoke);
        resendImagePanel.add(controlPanel, BorderLayout.WEST);
        resendImagePanel.add(image, BorderLayout.CENTER);
        messageAvatarPanel.setLayout(new GridBagLayout());
        messageAvatarPanel.add(resendImagePanel, new GBC(1, 0).setWeight(1000, 1).setAnchor(GBC.EAST).setInsets(0, 0, 0, 5));
        messageAvatarPanel.add(avatar, new GBC(2, 0).setWeight(1, 1).setAnchor(GBC.NORTH).setInsets(0, 0, 0, 5));

        add(timePanel, BorderLayout.NORTH);
        add(messageAvatarPanel, BorderLayout.CENTER);
    }
}
