package cn.shu.wechat.utils;

import net.coobird.thumbnailator.Thumbnails;
import ws.schild.jave.*;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.info.AudioInfo;
import ws.schild.jave.info.VideoInfo;
import ws.schild.jave.info.VideoSize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
public class MediaUtil {
    /**
     * 传视频File对象，返回压缩后File对象信息
     * @param source
     */
    public static File compressionVideo(File source, String picName,int bitRate) {
        if(source == null){
            return null;
        }
        int i = source.getAbsolutePath().lastIndexOf("/");
        if (i == -1){
            i = source.getAbsolutePath().lastIndexOf("\\");
        }
        String newPath = source.getAbsolutePath().substring(0, i).concat(picName);
        File target = new File(newPath);
        try {
            MultimediaObject object = new MultimediaObject(source);
            AudioInfo audioInfo = object.getInfo().getAudio();
            // 根据视频大小来判断是否需要进行压缩,
            int maxSize = 1;
           double mb = Math.ceil(source.length()/ 1048576.0);
            int second = (int)object.getInfo().getDuration()/1000;
            BigDecimal bd = new BigDecimal(String.format("%.4f", mb/second));
            System.out.println("开始压缩视频了--> 视频每秒平均 "+ bd +" MB ");
            // 视频 > 100MB, 或者每秒 > 0.5 MB 才做压缩， 不需要的话可以把判断去掉
            boolean temp = source.length() > maxSize*1024*1024 || bd.compareTo(new BigDecimal("0.5")) > 0;
            if(temp){
                long time = System.currentTimeMillis();
                //TODO 视频属性设置
                int maxBitRate = 128000;
                maxBitRate=128000;
                int maxSamplingRate = 44100;
                //int bitRate = 800000;

                int maxFrameRate = 20;
                int maxWidth = 1280;

                AudioAttributes audio = new AudioAttributes();
                // 设置通用编码格式
                audio.setCodec("aac");
                // 设置最大值：比特率越高，清晰度/音质越好
                // 设置音频比特率,单位:b (比特率越高，清晰度/音质越好，当然文件也就越大 128000 = 182kb)
                if(audioInfo.getBitRate() > maxBitRate){
                    audio.setBitRate(new Integer(maxBitRate));
                }

                // 设置重新编码的音频流中使用的声道数（1 =单声道，2 = 双声道（立体声））。如果未设置任何声道值，则编码器将选择默认值 0。
                audio.setChannels(audioInfo.getChannels());
                // 采样率越高声音的还原度越好，文件越大
                // 设置音频采样率，单位：赫兹 hz
                // 设置编码时候的音量值，未设置为0,如果256，则音量值不会改变
                // audio.setVolume(256);
                if(audioInfo.getSamplingRate() > maxSamplingRate){
                    audio.setSamplingRate(maxSamplingRate);
                }

                //TODO 视频编码属性配置
                VideoInfo videoInfo = object.getInfo().getVideo();
                VideoAttributes video = new VideoAttributes();
                video.setCodec("h264");
                //设置音频比特率,单位:b (比特率越高，清晰度/音质越好，当然文件也就越大 800000 = 800kb)
                if(videoInfo.getBitRate() > bitRate){
                    video.setBitRate(bitRate);
                }

                // 视频帧率：15 f / s  帧率越低，效果越差
                // 设置视频帧率（帧率越低，视频会出现断层，越高让人感觉越连续），视频帧率（Frame rate）是用于测量显示帧数的量度。所谓的测量单位为每秒显示帧数(Frames per Second，简：FPS）或“赫兹”（Hz）。
                if(videoInfo.getFrameRate() > maxFrameRate){
                    video.setFrameRate(maxFrameRate);
                }

                // 限制视频宽高
                int width = videoInfo.getSize().getWidth();
                int height = videoInfo.getSize().getHeight();
                if(width > maxWidth){
                    float rat = (float) width / maxWidth;
                    video.setSize(new VideoSize(maxWidth,(int)(height/rat)));
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
                attr.setEncodingThreads(i1/2);
                Encoder encoder = new Encoder();
                encoder.encode(new MultimediaObject(source), target, attr);
                System.out.println("压缩总耗时：" + (System.currentTimeMillis() - time)/1000);
                return target;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(target.length() > 0){
               // source.delete();
            }
        }
        return source;
    }
    /**
     * 获取视频大小
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
     * 压缩视频
     * @param source  待转换的文件
     */
    public static File toCompressFile(String source){
/*        int i = source.getAbsolutePath().lastIndexOf("/");
        if (i == -1){
            i = source.getAbsolutePath().lastIndexOf("\\");
        }*/
        String targetFile = UUID.randomUUID()+".mp4";
        try{
            Runtime runtime = Runtime.getRuntime();
            /**将视频压缩为 每秒15帧 平均码率600k 画面的宽与高 为1280*720*/
            String cutCmd="ffmpeg -i " + source + " -vf mpdecimate,setpts=N/FRAME_RATE/TB -r 15 -b:v 300k  -s 1280x720 "+ targetFile;
            System.out.println(cutCmd);
            Process exec = runtime.exec(cutCmd);
            System.out.println("文件："+source+" 正在转换中。。。");
            //等待结束
            exec.waitFor();
           /* while (!exec.isAlive()){
                try{
                    Thread.sleep(500);
                }catch (Exception e){

                }

            }*/
            System.out.println("文件："+source+" 转换完成。");

        }catch(Exception e){
            e.printStackTrace();
            System.out.println("压缩文件出现异常："+e.getMessage());
        }
        return new File(targetFile);
    }
    public static File compressImage( File file ,long size){
        //1M以上图片发不出去
        if (!file.exists()){
            return file;
        }
        String newFilePath = file.getParent() + "/thumbnails/" + file.getName();
        File newFile = new File(newFilePath);

        if (!newFile.exists()){
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
        while (newSize > size && quality>0) {
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
    public static void main(String[] ars ){

    }

}
