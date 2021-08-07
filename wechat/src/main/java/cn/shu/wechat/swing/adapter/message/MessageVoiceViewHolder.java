package cn.shu.wechat.swing.adapter.message;

import cn.shu.wechat.swing.components.*;
import cn.shu.wechat.swing.components.message.MessagePopupMenu;
import cn.shu.wechat.swing.components.message.TagPanel;
import cn.shu.wechat.swing.utils.FontUtil;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 * Created by 舒新胜 on 16/06/2017.
 */
@Getter
public class MessageVoiceViewHolder extends BaseMessageViewHolder {
    protected final TagPanel contentTagPanel = new TagPanel();
    protected JLabel durationText;
    protected final JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    protected final JPanel messageAvatarPanel = new JPanel();
    protected final MessagePopupMenu popupMenu = new MessagePopupMenu();
    protected boolean isGroup = true;
    protected final JLabel voiceImgLabel = new JLabel();

    /**
     *播放进度条
     */
    protected final RCProgressBar progressBar = new RCProgressBar(4);
    public MessageVoiceViewHolder(boolean isGroup) {
        this.isGroup = isGroup;
        initComponents();
        initView();
    }

    private void initComponents() {
        durationText = new JLabel();

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
