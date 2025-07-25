package cn.shu.wechat.swing.media;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Log4j2
public class Mp3Player  {
    private volatile Player advancedPlayer;
    private volatile String filePath;
    private volatile VoicePlaybackListener listener;
    private volatile Thread playerThread;


    public void play(String filePath,VoicePlaybackListener listener) throws JavaLayerException, FileNotFoundException {
        if (filePath .equals(this.filePath)) {
            //点击的正在播放的语音 则停止
            if (advancedPlayer != null) {
                advancedPlayer.close();
            }
            if (playerThread!=null){
                playerThread.interrupt();
            }
            listener.playbackFinished(null);
            return;
        }
        if (advancedPlayer != null) {
            advancedPlayer.close();
            if (playerThread!=null){
                playerThread.interrupt();
            }
        }

        clear();
        // 播放完自动清除状态
        listener.playbackFinished(null);
        this.listener = listener;
        this.filePath = filePath;
        FileInputStream fis = new FileInputStream(filePath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        advancedPlayer = new Player(bis);
        playerThread = new Thread(() -> {
            try (fis; bis) {
                listener.playbackStarted(null);
                advancedPlayer.play();

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                clear();
                // 播放完自动清除状态
                listener.playbackFinished(null);
            }
        });
         new Thread(() -> {
             while (playerThread!=null && playerThread.isAlive()) {
                if (advancedPlayer != null && !advancedPlayer.isComplete()){
                    listener.playbackPosition(advancedPlayer.getPosition());
                }else{
                     listener.playbackFinished(null);
                }
             }
        }).start();

        playerThread.start();

    }

    public void setPlayBackListener(VoicePlaybackListener listener) {

        this.listener = listener;
    }
    public void stop() {

    }
    private void clear() {
        advancedPlayer = null;
        playerThread = null;
        filePath = null;
    }

    public abstract static class VoicePlaybackListener extends PlaybackListener {
        public void playbackPosition(int position) {

        }
    }
}
