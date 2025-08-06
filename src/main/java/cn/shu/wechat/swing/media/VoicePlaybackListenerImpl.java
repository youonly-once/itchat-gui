package cn.shu.wechat.swing.media;

import cn.shu.wechat.swing.adapter.message.voice.MessageVoiceViewHolder;
import cn.shu.wechat.swing.components.RCProgressBar;

import javax.swing.*;
import java.lang.ref.WeakReference;

public class VoicePlaybackListenerImpl extends VoicePlaybackListener{
     private final WeakReference<MessageVoiceViewHolder> holder;
        private final int voiceLength;
        public VoicePlaybackListenerImpl(MessageVoiceViewHolder holder,int voiceLength) {
            this.holder = new WeakReference<>(holder);
            this.voiceLength = voiceLength;
        }

        @Override
        public void playbackPosition(int position) {
            SwingUtilities.invokeLater(() -> {
                holder.get().progressBar.setValue(position);
            });
        }

        @Override
        public void playbackStarted() {
            SwingUtilities.invokeLater(() -> {
                holder.get().removeUnreadPoint();
                RCProgressBar progressBar = holder.get().progressBar;
                progressBar.setVisible(true);
                progressBar.setMaximum((int)voiceLength);
                holder.get().durationText.start();
            });

        }

        @Override
        public void playbackFinished() {
            SwingUtilities.invokeLater(() -> {
                holder.get().progressBar.setValue(voiceLength);
                holder.get().durationText.stop();
                holder.get().progressBar.setValue(0);
            });
        }

    }
