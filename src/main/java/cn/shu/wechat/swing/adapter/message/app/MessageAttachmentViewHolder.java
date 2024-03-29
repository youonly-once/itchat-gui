package cn.shu.wechat.swing.adapter.message.app;

import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GradientProgressBarUI;
import cn.shu.wechat.swing.components.RCProgressBar;
import cn.shu.wechat.swing.components.SizeAutoAdjustTextArea;
import cn.shu.wechat.swing.components.message.RCAttachmentMessageBubble;
import cn.shu.wechat.swing.components.message.TagPanel;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.utils.FontUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by 舒新胜 on 16/06/2017.
 */
public class MessageAttachmentViewHolder extends BaseMessageViewHolder {
    public SizeAutoAdjustTextArea attachmentTitle;
    public RCProgressBar progressBar = new RCProgressBar(); // 进度条
    public JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER,5,0)); // 时间面板
    public JPanel messageAvatarPanel = new JPanel(); // 消息 + 头像组合面板
    public TagPanel attachmentPanel = new TagPanel(); // 附件面板
    public JLabel attachmentIcon = new JLabel(); // 附件类型icon
    public JLabel sizeLabel = new JLabel();
    public RCAttachmentMessageBubble messageBubble;

    public MessageAttachmentViewHolder() {
        initComponents();
        setListeners();
    }

    private void setListeners() {
        MouseAdapter listener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                messageBubble.setActiveStatus(true);
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                messageBubble.setActiveStatus(false);
                super.mouseExited(e);
            }
        };

        attachmentPanel.addMouseListener(listener);
        attachmentTitle.addMouseListener(listener);

    }

    private void initComponents() {
        int maxWidth = (int) (MainFrame.getContext().currentWindowWidth * 0.427);
        attachmentTitle = new SizeAutoAdjustTextArea(maxWidth);

        timePanel.setBackground(Colors.WINDOW_BACKGROUND);
        messageAvatarPanel.setBackground(Colors.WINDOW_BACKGROUND);

        time.setForeground(Colors.FONT_GRAY);
        time.setFont(FontUtil.getDefaultFont(12));

        attachmentPanel.setOpaque(false);

        progressBar.setMaximum(100);
        progressBar.setMinimum(0);
        progressBar.setValue(100);
        progressBar.setUI(new GradientProgressBarUI());
        progressBar.setVisible(false);

        sizeLabel.setFont(FontUtil.getDefaultFont(12));
        sizeLabel.setForeground(Colors.FONT_GRAY);
    }
}
