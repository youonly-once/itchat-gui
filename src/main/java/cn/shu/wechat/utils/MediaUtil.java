package cn.shu.wechat.utils;

import lombok.extern.log4j.Log4j2;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.info.MultimediaInfo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @作者 舒新胜
 * @项目 weixin
 * @创建时间 2/2/2021 5:02 PM
 */
@Log4j2
public class MediaUtil {

    /**
     * 获取视频时长
     * @param source
     * @return
     */
    public static  long getVideoDuration(File source){
        MultimediaObject object = new MultimediaObject(source);
        long duration = 0;
        try {
            duration = object.getInfo().getDuration();
        } catch (EncoderException e) {
            log.error(e.getMessage(),e);
        }
        return duration;
    }

    public static VideoInfo getVideoInfo(File video) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(video)) {
            grabber.start();
            int width = grabber.getImageWidth();
            int height = grabber.getImageHeight();
            long durationSeconds = grabber.getLengthInTime() / 1_000_000;

            grabber.stop();
            return new VideoInfo(
                    width,
                    height,
                    durationSeconds,null,null
            );

        } catch (FrameGrabber.Exception e) {
            log.error(e.getMessage(),e);
        }
        return new VideoInfo(
               100,
               50,
               0,null,null
        );
    }


    /**
     * 获取 视频预览图
     *
     * @param video 视频文件
     * @return
     */
    public static VideoInfo getVideoPic(Object context, File video,int maxWidth,int maxHeight) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(video);
             Java2DFrameConverter converter = new Java2DFrameConverter();) {
            grabber.start();
            int width = grabber.getImageWidth();
            int height = grabber.getImageHeight();
            long durationSeconds = grabber.getLengthInTime() / 1_000_000;

            BufferedImage frame = converter.convert(grabber.grabImage());
            Image scaledImage = IconUtil.getScaledImageByMax(frame, maxWidth,maxHeight);
            grabber.stop();
            return new VideoInfo(width, height, durationSeconds, (BufferedImage)scaledImage,null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return new VideoInfo(0, 0, 0,IconUtil.getBufferedImage(context, "/image/video.png"),null);
    }

    /**
     * 获取 视频预览图
     *
     * @param video 视频文件
     * @return
     */
    public static VideoInfo getVideoPic(Object context, File video,File outPutFile,int maxWidth,int maxHeight) {
        VideoInfo videoInfo = getVideoPic(context, video,maxWidth,maxHeight);
        try {
            Files.createDirectories(outPutFile.getParentFile().toPath());
            ImageIO.write(videoInfo.frame(),"png",outPutFile);
            return new VideoInfo(videoInfo.width, videoInfo.height, videoInfo.durationSeconds, null, outPutFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public record VideoInfo(
            int width,
            int height,
            long durationSeconds,
            BufferedImage frame,
            String picPath
    ) {}


}
