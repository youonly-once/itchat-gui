package cn.shu.wechat.utils;

import lombok.extern.log4j.Log4j2;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;

import java.awt.image.BufferedImage;
import java.io.File;

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
            e.printStackTrace();
        }
        return duration;
    }

    /**
     * 获取 视频预览图
     *
     * @param video 视频文件
     * @return
     */
    public static BufferedImage getVideoPic(Object context, File video) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(video)) {
            grabber.start();
            Java2DFrameConverter converter = new Java2DFrameConverter();
            BufferedImage frame = converter.convert(grabber.grabImage());
            grabber.stop();
            return frame;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return IconUtil.getBufferedImage(context, "/image/video.png");
    }




}
