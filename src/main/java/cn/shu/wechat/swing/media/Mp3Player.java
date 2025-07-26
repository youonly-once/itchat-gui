package cn.shu.wechat.swing.media;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Log4j2
public class Mp3Player  {
    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory());
    private Player advancedPlayer;
    private String filePath;
    private VoicePlaybackListener listener;
    private Thread playerThread;
    private volatile boolean stopped;
    //一定要加 volatile
    private volatile ScheduledFuture<?> monitorFuture;

    public void play(String filePath,VoicePlaybackListener listener) throws JavaLayerException, FileNotFoundException {

        if (filePath .equals(this.filePath)) {
            //点击的正在播放的语音 则停止
            if (advancedPlayer != null) {
                advancedPlayer.close();
            }
            return;
        }

        //停掉正在播放的线程
        if (advancedPlayer != null) {
            advancedPlayer.close();
        }
        //等待之前的播放停止
        // 等待之前的播放线程停止（非 busy 等待）
        if (playerThread != null) {
            try {
                playerThread.join(); // 优于 sleep + while
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while waiting for previous player thread to finish.");
            }
        }

        // 播放完自动清除状态
        this.listener = listener;
        this.filePath = filePath;


        playerThread = Thread.ofVirtual().start(() -> {

            try (FileInputStream fis = new FileInputStream(filePath); BufferedInputStream bis = new BufferedInputStream(fis)) {
                advancedPlayer = new Player(bis);
                listener.playbackStarted();
                stopped = false;
                //确保当前线程启动后再启动监听线程
                // 启动监听任务：播放位置轮询
                // AtomicInteger prePos = new AtomicInteger();
                monitorFuture = executor.scheduleAtFixedRate(() -> {
                    if (stopped || advancedPlayer == null || advancedPlayer.isComplete()) {
                        if (monitorFuture != null && !monitorFuture.isCancelled()) {
                            monitorFuture.cancel(true);
                        }
                        return;
                    }
                    System.out.println(Thread.currentThread().getId());
                    //if (prePos.get()!=advancedPlayer.getPosition()){
                    // prePos.set(advancedPlayer.getPosition());
                    listener.playbackPosition(advancedPlayer.getPosition());
                    // }
                }, 0, 100, TimeUnit.MILLISECONDS);

                advancedPlayer.play();

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                clear();
            }
        });

    }

    private void clear() {
        stopped = true;
        if (this.listener != null) {
            this.listener.playbackFinished();
        }
        filePath = null;
        listener = null;
    }


}
