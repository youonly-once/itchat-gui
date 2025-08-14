package cn.shu.wechat.swing.media;

public abstract class VoicePlaybackListener {
    public abstract void playbackPosition(int position);

    public abstract void playbackStarted();

    public abstract void playbackFinished();
}
