package cn.shu.wechat.swing.utils;

import cn.shu.wechat.swing.Launcher;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by 舒新胜 on 2017/6/11.
 */
@Log4j2
public class FileCache {

    public static String FILE_CACHE_ROOT_PATH;
    private static DecimalFormat decimalFormat = new DecimalFormat("#.0");

    private FileCache(){
    }

    static  {
        try {
            //FILE_CACHE_ROOT_PATH = getClass().getResource("/cache").getPath() + "/file";
            FILE_CACHE_ROOT_PATH = Launcher.wechatConfiguration.getBasePath() + "/cache/file";
            File file = new File(FILE_CACHE_ROOT_PATH);
            if (!file.exists()) {
                file.mkdirs();
                log.info("创建文件缓存目录：{}",file.getAbsolutePath() );
            }
        } catch (Exception e) {
            FILE_CACHE_ROOT_PATH = "./";
        }
    }

    public static String tryGetFileCache(String identify, String name) {
        File cacheFile = new File(FILE_CACHE_ROOT_PATH + "/" + identify + "_" + name);
        if (cacheFile.exists()) {
            return cacheFile.getAbsolutePath();
        }

        return null;
    }

    public String cacheFile(String identify, String name, byte[] data) {
        if (data == null || data.length < 1) {
            return null;
        }

        File cacheFile = new File(FILE_CACHE_ROOT_PATH + "/" + identify + "_" + name);
        try ( FileOutputStream outputStream = new FileOutputStream(cacheFile)){
            outputStream.write(data);
            return cacheFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String fileSizeString(String path) {
        File file = new File(path);

        long size = file.length();
       return fileSizeString(size);

    }

    /**
     * 获取文件大小
     * @param size 字节
     * @return 字符串表示
     */
    public static String fileSizeString(Long size){
        if (size < 1024) {
            return  size + " 字节";
        } else if (size < 1024 * 1024) {
            return  decimalFormat.format(size * 1.0F / 1024) + " KB";
        } else {
            return  decimalFormat.format(size * 1.0F / 1024 / 1024) + " MB";
        }
    }
}
