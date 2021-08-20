package cn.shu.wechat.swing.adapter.message.voice;

import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.components.*;
import cn.shu.wechat.swing.components.message.MessagePopupMenu;
import cn.shu.wechat.swing.components.message.RCAttachmentMessageBubble;
import cn.shu.wechat.swing.components.message.RCLeftVoiceMessageBubble;
import cn.shu.wechat.swing.components.message.TagPanel;
import cn.shu.wechat.swing.utils.FontUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * @author 舒新胜
 * @date 16/06/2017
 */

public abstract class MessageVoiceViewHolder extends BaseMessageViewHolder {
    public final TagPanel contentTagPanel = new TagPanel();
    public final CountDownJLabel durationText = new CountDownJLabel();;
    public final JLabel gapText = new JLabel();
    protected final JLabel unitLabel = new JLabel("''");
    protected final JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    protected final JPanel messageAvatarPanel = new JPanel();
    protected final boolean isGroup;
    protected final JLabel voiceImgLabel = new JLabel();
    public final RCAttachmentMessageBubble messageBubble;
    protected final JPanel voicePanel = new JPanel();
    /**
     *播放进度条
     */
    public final RCProgressBar progressBar = new RCProgressBar(4);

    public MessageVoiceViewHolder(boolean isGroup, RCAttachmentMessageBubble messageBubble) {
        this.messageBubble = messageBubble;
        this.isGroup = isGroup;
        initComponents();
        initView();
        setListeners();
    }
    public MessageVoiceViewHolder(RCAttachmentMessageBubble messageBubble) {
        this(false,messageBubble);
    }
    private void setListeners() {
        MouseAdapter mouseListener = messageBubble.getMouseListener();
        /*durationText.addMouseListener(mouseListener);
        voiceImgLabel.addMouseListener(mouseListener);*/
    }
    private void initComponents() {

        time.setForeground(Colors.FONT_GRAY);
        time.setFont(FontUtil.getDefaultFont(12));

        messageAvatarPanel.setBackground(Colors.WINDOW_BACKGROUND);
        timePanel.setBackground(Colors.WINDOW_BACKGROUND);

        progressBar.setUI(new GradientProgressBarUI());
        progressBar.setVisible(false);
    }

    private void initView() {

    }
    public void removeUnreadPoint(){

    }
}
