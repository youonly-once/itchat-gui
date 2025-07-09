package cn.shu.wechat.swing.utils;

import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 图标缓存工具类
 * @author 舒新胜
 * @date 2021/8/7
 */
@Log4j2
public class IconUtil {
    /**
     * 图标缓存
     * key：图标名相对路径
     */
    private static final Map<String, ImageIcon> ICON_CACHE =
            Collections.synchronizedMap(new LinkedHashMap<String, ImageIcon>(128, 0.75f, true) {
                protected boolean removeEldestEntry(Map.Entry<String, ImageIcon> eldest) {
                    return size() > 100; // 最多缓存 100 张图标
                }
            });
    private static final Map<String, BufferedImage> BUFFERED_IMAGE_CACHE =
            Collections.synchronizedMap(new LinkedHashMap<String, BufferedImage>(128, 0.75f, true) {
                protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
                    return size() > 10; // 最多缓存 10 张图标
                }
            });

    public static ImageIcon getIcon(Object context, String path) {
        return getIcon(context, path, -1, -1);
    }

    public static ImageIcon getIcon(Object context, String path, int width) {
        return getIcon(context, path, width, width);
    }

    public static ImageIcon getIcon(Object context, String path, int width, int height) {
        ImageIcon rawIcon = ICON_CACHE.get(path);
        if (rawIcon == null) {
            URL url = context.getClass().getResource(path);
            if (url == null) {
                return null;
            }
            rawIcon = new ImageIcon(url);

            if (width > 0 && height > 0) {
                // 返回一个新的缩放副本，不影响缓存
                Image scaledImage = rawIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }

            ICON_CACHE.put(path, rawIcon);
        }

        return rawIcon;
    }

    public static BufferedImage getBufferedImage(Object context, String path) {
        BufferedImage bufferedImage = BUFFERED_IMAGE_CACHE.get(path);
        if (bufferedImage == null) {
            URL url = context.getClass().getResource(path);
            if (url == null) {
                return null;
            }
            try {
                bufferedImage = ImageIO.read(url);
                BUFFERED_IMAGE_CACHE.put(path, bufferedImage);
            } catch (IOException e) {
                log.error(e.getMessage());
            }

        }
        return bufferedImage;
    }

    public static BufferedImage getBufferedImage(String path) {
        BufferedImage bufferedImage = BUFFERED_IMAGE_CACHE.get(path);
        if (bufferedImage !=null){
            return bufferedImage;
        }
        try {
            bufferedImage = ImageIO.read(new File(path));
            if (bufferedImage != null) {
                BUFFERED_IMAGE_CACHE.put(path, bufferedImage);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return bufferedImage;
    }

    public static BufferedImage getBufferedImageByUrl(String url) {
        BufferedImage bufferedImage = BUFFERED_IMAGE_CACHE.get(url);
        if (bufferedImage !=null){
            return bufferedImage;
        }
        try {
            bufferedImage = ImageIO.read(new URL(url));
            if (bufferedImage != null) {
                BUFFERED_IMAGE_CACHE.put(url, bufferedImage);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return bufferedImage;
    }


    /**
     * 文件转ImageIcon
     * @param file 文件
     * @return ImageIcon
     */
    public static ImageIcon getIconFromFile(File file) {
        ImageIcon icon = new ImageIcon(file.getAbsolutePath());
        icon.setDescription(file.getAbsolutePath());
        return icon;
    }
}
