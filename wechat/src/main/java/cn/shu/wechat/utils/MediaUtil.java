package cn.shu.wechat.utils;

import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.info.AudioInfo;
import ws.schild.jave.info.VideoInfo;
import ws.schild.jave.info.VideoSize;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.channels.FileChannel;
import java.util.UUID;

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
     * 传视频File对象，返回压缩后File对象信息
     *
     * @param source
     */
    public static File compressionVideo(File source, String picName, int bitRate) {
        if (source == null) {
            return null;
        }
        int i = source.getAbsolutePath().lastIndexOf("/");
        if (i == -1) {
            i = source.getAbsolutePath().lastIndexOf("\\");
        }
        String newPath = source.getAbsolutePath().substring(0, i).concat(picName);
        File target = new File(newPath);
        try {
            MultimediaObject object = new MultimediaObject(source);

            AudioInfo audioInfo = object.getInfo().getAudio();
            // 根据视频大小来判断是否需要进行压缩,
            int maxSize = 1;
            double mb = Math.ceil(source.length() / 1048576.0);
            int second = (int) object.getInfo().getDuration() / 1000;
            BigDecimal bd = new BigDecimal(String.format("%.4f", mb / second));
            System.out.println("开始压缩视频了--> 视频每秒平均 " + bd + " MB ");
            // 视频 > 100MB, 或者每秒 > 0.5 MB 才做压缩， 不需要的话可以把判断去掉
            boolean temp = source.length() > maxSize * 1024 * 1024 || bd.compareTo(new BigDecimal("0.5")) > 0;
            if (temp) {
                long time = System.currentTimeMillis();
                //TODO 视频属性设置
                int maxBitRate = 128000;
                maxBitRate = 128000;
                int maxSamplingRate = 44100;
                //int bitRate = 800000;

                int maxFrameRate = 20;
                int maxWidth = 1280;

                AudioAttributes audio = new AudioAttributes();
                // 设置通用编码格式
                audio.setCodec("aac");
                // 设置最大值：比特率越高，清晰度/音质越好
                // 设置音频比特率,单位:b (比特率越高，清晰度/音质越好，当然文件也就越大 128000 = 182kb)
                if (audioInfo.getBitRate() > maxBitRate) {
                    audio.setBitRate(new Integer(maxBitRate));
                }

                // 设置重新编码的音频流中使用的声道数（1 =单声道，2 = 双声道（立体声））。如果未设置任何声道值，则编码器将选择默认值 0。
                audio.setChannels(audioInfo.getChannels());
                // 采样率越高声音的还原度越好，文件越大
                // 设置音频采样率，单位：赫兹 hz
                // 设置编码时候的音量值，未设置为0,如果256，则音量值不会改变
                // audio.setVolume(256);
                if (audioInfo.getSamplingRate() > maxSamplingRate) {
                    audio.setSamplingRate(maxSamplingRate);
                }

                //TODO 视频编码属性配置
                VideoInfo videoInfo = object.getInfo().getVideo();
                VideoAttributes video = new VideoAttributes();
                video.setCodec("h264");
                //设置音频比特率,单位:b (比特率越高，清晰度/音质越好，当然文件也就越大 800000 = 800kb)
                if (videoInfo.getBitRate() > bitRate) {
                    video.setBitRate(bitRate);
                }

                // 视频帧率：15 f / s  帧率越低，效果越差
                // 设置视频帧率（帧率越低，视频会出现断层，越高让人感觉越连续），视频帧率（Frame rate）是用于测量显示帧数的量度。所谓的测量单位为每秒显示帧数(Frames per Second，简：FPS）或“赫兹”（Hz）。
                if (videoInfo.getFrameRate() > maxFrameRate) {
                    video.setFrameRate(maxFrameRate);
                }

                // 限制视频宽高
                int width = videoInfo.getSize().getWidth();
                int height = videoInfo.getSize().getHeight();
                if (width > maxWidth) {
                    float rat = (float) width / maxWidth;
                    video.setSize(new VideoSize(maxWidth, (int) (height / rat)));
                }

                EncodingAttributes attr = new EncodingAttributes();
                attr.setOutputFormat("mp4");
                attr.setAudioAttributes(audio);
                attr.setVideoAttributes(video);

                // 速度最快的压缩方式， 压缩速度 从快到慢： ultrafast, superfast, veryfast, faster, fast, medium,  slow, slower, veryslow and placebo.
//                attr.setPreset(PresetUtil.VERYFAST);
//                attr.setCrf(27);
//                // 设置线程数
                int i1 = Runtime.getRuntime().availableProcessors();
                attr.setEncodingThreads(i1 / 2);
                Encoder encoder = new Encoder();
                encoder.encode(new MultimediaObject(source), target, attr);
                System.out.println("压缩总耗时：" + (System.currentTimeMillis() - time) / 1000);
                return target;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (target.length() > 0) {
                // source.delete();
            }
        }
        return source;
    }

    /**
     * 获取视频大小
     *
     * @param source
     * @return
     */

    public static BigDecimal getVideoSize(File source) {
        FileChannel fc = null;
        try {
            FileInputStream fis = new FileInputStream(source);
            fc = fis.getChannel();
            BigDecimal fileSize = new BigDecimal(fc.size());
            return fileSize.divide(new BigDecimal(1048576), 2, RoundingMode.HALF_UP);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != fc) {
                try {
                    fc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 获取 视频预览图
     * @param video 视频文件
     * @return
     */
    public static BufferedImage getVideoPic(File video) {
        FFmpegFrameGrabber ff = new FFmpegFrameGrabber(video);
        try {
            ff.start();
            int length = ff.getLengthInFrames();
            int i = 0;
            Frame f = null;
            while (i < length) {
                f = ff.grabFrame();
                //过滤前5帧，避免出现全黑的图片，依自己情况而定f = ff.grabFrame();
                if ((i > 5) && (f.image != null)) {
                    break;
                }
                i++;
            }
            //截取的帧图片
            Java2DFrameConverter converter = new Java2DFrameConverter();
            BufferedImage srcImage = converter.getBufferedImage(f);
            int srcImageWidth = srcImage.getWidth();
            int srcImageHeight = srcImage.getHeight();
            //对截图进行等比例宿放(宿略图)
            int width = 200;
            int height = (int) (((double) width / srcImageWidth) * srcImageHeight);
            BufferedImage thumbnailImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            thumbnailImage.getGraphics().drawImage(srcImage.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0,
                    null);
            ff.stop();
            return thumbnailImage;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new BufferedImage(200, 200, BufferedImage.TYPE_3BYTE_BGR);
    }
    /**
     * 压缩视频
     *
     * @param source 待转换的文件
     */
    public static File toCompressFile(String source) {

        String targetFile = UUID.randomUUID() + ".mp4";
        try {
            Runtime runtime = Runtime.getRuntime();
            /**将视频压缩为 每秒15帧 平均码率600k 画面的宽与高 为1280*720*/
            String cutCmd = "ffmpeg -i " + source + " -vf mpdecimate,setpts=N/FRAME_RATE/TB -r 15 -b:v 300k  -s 1280x720 " + targetFile;
            System.out.println(cutCmd);
            Process exec = runtime.exec(cutCmd);
            System.out.println("文件：" + source + " 正在转换中。。。");
            //等待结束
            exec.waitFor();

            System.out.println("文件：" + source + " 转换完成。");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("压缩文件出现异常：" + e.getMessage());
        }
        return new File(targetFile);
    }

    public static File compressImage(File file, long size) {
        //1M以上图片发不出去
        if (!file.exists()) {
            return file;
        }
        String newFilePath = file.getParent() + "/thumbnails/" + file.getName();
        File newFile = new File(newFilePath);

        if (!newFile.exists()) {
            try {
                String parent = newFile.getParent();
                new File(parent).mkdirs();
                newFile.createNewFile();
            } catch (IOException e) {
                return file;
            }
        }
        float quality = 0.9f;
        long newSize = file.length();
        while (newSize > size && quality > 0) {
            try {
                Thumbnails.of(file.getAbsolutePath())
                        .scale(1f)
                        .outputQuality(quality)
                        .toFile(newFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            quality = quality - 0.1f;
            newSize = newFile.length();
        }
        return newFile;
    }

    /**
     * 在使用时发现视频压缩和视频时长有关系
     * 一个9M的56s的视频压缩后视频7M多
     * 一个22M的5s的视频压缩后视频624K
     *
     * @param file
     * @param checkCompress
     * @return
     */
    public static File compressVideo(File file, Boolean checkCompress) {
        int FRAME_RATE = 30;
        int VIDEO_BITRATE = 1048576;
        int COMPRESS_WIDTH = 320;
        FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(file.getAbsolutePath());
        String fileName = null;

        Frame captured_frame = null;

        FFmpegFrameRecorder recorder = null;

        try {
            frameGrabber.start();
            fileName = file.getAbsolutePath().replace(".mp4", "_edited.mp4");
            log.info("wight:{},height:{}", frameGrabber.getImageWidth(), frameGrabber.getImageHeight());

            int height = frameGrabber.getImageHeight();
            int widht = frameGrabber.getImageWidth();
            if (checkCompress && needCompress(file.length())) {
                height = calculateHeight(frameGrabber.getImageWidth(), frameGrabber.getImageHeight(), COMPRESS_WIDTH);
                widht = COMPRESS_WIDTH;
                log.info("new wight:{},height:{}", widht, height);
            }
            recorder = new FFmpegFrameRecorder(fileName, widht, height, frameGrabber.getAudioChannels());
            recorder.setFrameRate(FRAME_RATE);
            //下面这行打开就报错
            //recorder.setSampleFormat(frameGrabber.getSampleFormat());
            recorder.setSampleRate(frameGrabber.getSampleRate());
            //recorder.setAudioChannels(1);
            recorder.setVideoOption("preset", "veryfast");
            // yuv420p,像素
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            recorder.setFormat("mp4");
            //比特
            //recorder.setVideoBitrate(VIDEO_BITRATE);
            recorder.start();

            while (true) {
                try {
                    captured_frame = frameGrabber.grabFrame();
                    if (captured_frame == null) {
                        System.out.println("!!! end cvQueryFrame");
                        break;
                    }
                    recorder.setTimestamp(frameGrabber.getTimestamp());
                    recorder.record(captured_frame);
                } catch (Exception e) {
                }
            }
            recorder.stop();
            recorder.release();
            frameGrabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //file.delete();
        return new File(fileName);
    }

    /**
     * 是否需要压缩，大于3MB
     *
     * @param length
     * @return
     */
    public static boolean needCompress(long length) {
        log.info("video size:{}", length);
        return length >= 3145728;
    }

    /**
     * 等比计算新高度
     *
     * @param w
     * @param h
     * @param nw
     * @return
     */
    private static int calculateHeight(int w, int h, int nw) {
        double s = Integer.valueOf(h).doubleValue() / Integer.valueOf(w).doubleValue();
        int height = (int) (nw * s);
        //如果宽和高不是偶数recorder.start();会报错
        if (height % 2 != 0) {
            height += 1;
        }
        return height;
    }

}
