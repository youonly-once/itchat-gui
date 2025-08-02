package cn.shu.wechat.swing.utils;

import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by 舒新胜 on 2017/6/11.
 */
@Log4j2
public class FileCache {
    private static final long KB = 1024;
    private static final long MB = KB * 1024;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    private FileCache(){
    }

    public static String fileSizeString(String path) {
       return fileSizeString(new File(path).length());

    }

    /**
     * 将字节大小转换为 KB / MB 字符串表示（保留两位小数）
     * 例如：1024 → 1 KB，1048576 → 1 MB
     */
    public static String fileSizeString(Long size) {
        if (size == null || size < 0) {
            return "未知大小";
        }

        if (size < KB) {
            return size + " 字节";
        } else if (size < MB) {
            return DECIMAL_FORMAT.format(size / (double) KB) + " KB";
        } else {
            return DECIMAL_FORMAT.format(size / (double) MB) + " MB";
        }
    }
}
