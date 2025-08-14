package cn.shu.wechat.swing.adapter.message.voice;

import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.components.CountDownJLabel;
import cn.shu.wechat.swing.components.GradientProgressBarUI;
import cn.shu.wechat.swing.components.RCProgressBar;
import cn.shu.wechat.swing.components.message.RCAttachmentMessageBubble;
import cn.shu.wechat.utils.IconUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;

/**
 *
 * @author 舒新胜
 * @date 16/06/2017
 */

public abstract class MessageVoiceViewHolder extends BaseMessageViewHolder {

    public final CountDownJLabel durationText = new CountDownJLabel();

    public final JLabel gapText = new JLabel();

    public final JLabel unitLabel = new JLabel("''");


    public final JLabel voiceImgLabel = new JLabel();

    public RCAttachmentMessageBubble messageBubble;
    /**
     *播放进度条
     */
    public final RCProgressBar progressBar = new RCProgressBar(2){
        @Override
        public void setVisible(boolean aFlag) {
            if(aFlag){
                super.setVisible(aFlag);
            }else{
                setValue(0);
            }
        }
    };

    public MessageVoiceViewHolder() {


    }

    protected void setListeners() {

        MouseAdapter mouseListener = messageBubble.getMouseListener();
        durationText.addMouseListener(mouseListener);
        voiceImgLabel.addMouseListener(mouseListener);
        unitLabel.addMouseListener(mouseListener);
        gapText.addMouseListener(mouseListener);
    }
    protected void initComponents() {

        progressBar.setUI(new GradientProgressBarUI());
        progressBar.setVisible(true);
        progressBar.setMinimum(0);
        progressBar.setValue(0);
        progressBar.setMaximum(100);


    }

    protected void initView() {
        setLayout(new GridBagLayout());
        messageBubble.setCursor(new Cursor(Cursor.HAND_CURSOR));


    }
    public void removeUnreadPoint(){

    }

    @Override
    public void removeNotify() {
        for (MouseListener mouseListener : messageBubble.getMouseListeners()) {
            messageBubble.removeMouseListener(mouseListener);
        }
        super.removeNotify();
    }
}
