package cn.shu.wechat.swing.adapter.message.app.attachment;

import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.components.*;
import cn.shu.wechat.swing.components.message.RCAttachmentMessageBubble;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.utils.FontUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;

/**
 * Created by 舒新胜 on 16/06/2017.
 */
public abstract class MessageAttachmentViewHolder extends BaseMessageViewHolder {
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


    public JLabel attachmentIcon = new JLabel(); // 附件类型icon

    public JLabel sizeLabel = new JLabel();

    public RCAttachmentMessageBubble messageBubble;

    private MouseAdapter listener;

    public MessageAttachmentViewHolder() {

    }

    protected void setListeners() {
        listener = messageBubble.getMouseListener();
        attachmentTitle.addMouseListener(listener);
        sizeLabel.addMouseListener(listener);
        attachmentIcon.addMouseListener(listener);

    }

    protected void initComponents() {
        int maxWidth = (int) (MainFrame.getContext().currentWindowWidth * 0.427);
        attachmentTitle = new SizeAutoAdjustTextArea(maxWidth);


        progressBar.setMaximum(100);
        progressBar.setMinimum(0);
        progressBar.setValue(0);
        progressBar.setUI(new GradientProgressBarUI());
        progressBar.setVisible(true);

        sizeLabel.setFont(FontUtil.getDefaultFont(12));
        sizeLabel.setForeground(Colors.FONT_GRAY);
        sizeLabel.setPreferredSize(new Dimension(100, 10));

    }
    protected void initView(){
        setLayout(new GridBagLayout());
        messageBubble.setCursor(new Cursor(Cursor.HAND_CURSOR));
        messageBubble.setLayout(new GridBagLayout());
        messageBubble.add(attachmentIcon, new GBC(0, 0)
                .setWeight(0, 1)
                .setInsets(0, 5, 0, 0)
                        .setFill(GBC.BOTH)
                .setGridHeight(2));
        messageBubble.add(attachmentTitle, new GBC(1, 0)
                .setWeight(1, 1)
                .setFill(GBC.BOTH)
                .setAnchor(GBC.NORTHWEST)
                .setInsets(0, 5, 0, 0));

        messageBubble.add(sizeLabel, new GBC(1, 1)
                .setWeight(1, 1)
                .setFill(GBC.BOTH)
                .setAnchor(GBC.NORTHWEST)
                .setInsets(2, 8, 0, 0));
    }

    @Override
    public void removeNotify() {
        messageBubble.removeMouseListener(listener);
        super.removeNotify();
    }
}
