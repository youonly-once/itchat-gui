package cn.shu.wechat.swing.adapter.message.app.attachment;

import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.components.*;
import cn.shu.wechat.swing.components.message.RCAttachmentMessageBubble;
import cn.shu.wechat.swing.components.message.TagPanel;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.utils.FontUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by 舒新胜 on 16/06/2017.
 */
public class MessageAttachmentViewHolder extends BaseMessageViewHolder {
    public SizeAutoAdjustTextArea attachmentTitle;

    public RCProgressBar progressBar = new RCProgressBar(){
        @Override
        public void setVisible(boolean aFlag) {
            if(aFlag){
                super.setVisible(aFlag);
            }else{
                setValue(0);
            }
        }
    }; // 进度条

    public JPanel messageAvatarPanel = new JPanel(); // 消息 + 头像组合面板

    public TagPanel attachmentPanel = new TagPanel(); // 附件面板

    public JLabel attachmentIcon = new JLabel(); // 附件类型icon

    public JLabel sizeLabel = new JLabel();

    public RCAttachmentMessageBubble messageBubble;

    private MouseAdapter listener;

    public MessageAttachmentViewHolder() {

    }

    private void setListeners() {
         listener = new MouseAdapter() {
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

    protected void initComponents() {
        int maxWidth = (int) (MainFrame.getContext().currentWindowWidth * 0.427);
        attachmentTitle = new SizeAutoAdjustTextArea(maxWidth);

        messageAvatarPanel.setBackground(Colors.WINDOW_BACKGROUND);

        attachmentPanel.setOpaque(false);

        messageBubble.setCursor(new Cursor(Cursor.HAND_CURSOR));
        messageBubble.add(attachmentPanel);


        progressBar.setMaximum(100);
        progressBar.setMinimum(0);
        progressBar.setValue(0);
        progressBar.setUI(new GradientProgressBarUI());
        progressBar.setVisible(true);

        sizeLabel.setFont(FontUtil.getDefaultFont(12));
        sizeLabel.setForeground(Colors.FONT_GRAY);
        sizeLabel.setPreferredSize(new Dimension(100, 10));

        setListeners();
    }
    protected void initView(){
        setLayout(new GridBagLayout());
        attachmentPanel.setLayout(new GridBagLayout());
        attachmentPanel.add(attachmentIcon, new GBC(0, 0)
                .setWeight(1, 1)
                .setInsets(2, 5, 0, 0)
                        .setFill(GBC.BOTH)
                .setGridHeight(2));
        attachmentPanel.add(attachmentTitle, new GBC(1, 0)
                .setWeight(1, 1)
                .setFill(GBC.BOTH)
                .setAnchor(GBC.NORTHWEST)
                .setInsets(2, 5, 0, 5));
        attachmentPanel.add(sizeLabel, new GBC(1, 1)
                .setWeight(1, 1)
                .setFill(GBC.BOTH)
                .setAnchor(GBC.NORTHWEST)
                .setInsets(2, 8, 0, 5));
    }

    @Override
    public void removeNotify() {
        attachmentPanel.removeMouseListener(listener);
        attachmentTitle.removeMouseListener(listener);
        super.removeNotify();
    }
}
