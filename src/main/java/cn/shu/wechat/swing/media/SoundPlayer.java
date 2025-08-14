package cn.shu.wechat.swing.media;

import cn.shu.wechat.utils.ExecutorServiceUtil;
import lombok.extern.log4j.Log4j2;

import javax.sound.sampled.*;
import java.io.InputStream;

@Log4j2
public class SoundPlayer {

    private static final long MIN_INTERVAL_MS = 1000;
    private static volatile long lastPlayTime = 0;
    private static Clip clip;

    static {
        try (InputStream inputStream = SoundPlayer.class.getResourceAsStream("/wav/msg.wav")) {
            if (inputStream == null) {
                log.warn("音频资源未找到: /wav/msg.wav");
            } else {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
                AudioFormat format = audioInputStream.getFormat();
                DataLine.Info info = new DataLine.Info(Clip.class, format);
                clip = (Clip) AudioSystem.getLine(info);
                clip.open(audioInputStream);
            }
        } catch (Exception e) {
            log.error("音频初始化失败", e);
        }
    }

    public static synchronized void playMessageSound() {
        long now = System.currentTimeMillis();
        if (now - lastPlayTime < MIN_INTERVAL_MS || clip == null) {
            return; // 限流 或 clip不可用
        }
        lastPlayTime = now;

        ExecutorServiceUtil.getGlobalExecutorService().submit(() -> {
            try {
                if (clip.isRunning()) {
                    clip.stop();
                }
                clip.setFramePosition(0); // 回到起始位置
                clip.start();
            } catch (Exception e) {
                log.error("播放音频失败", e);
            }
        });
    }
}
