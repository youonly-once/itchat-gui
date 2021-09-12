package cn.shu.wechat.swing.utils;

import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 图标缓存工具类
 * @author 舒新胜
 * @date 2021/8/7
 */
@Log4j2
public class IconUtil {
    /**
     * 图标缓存
     * key：图标名
     *
     */
    private static final Map<String, ImageIcon> ICON_CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<String, BufferedImage> BUFFERED_IMAGE_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    public static ImageIcon getIcon(Object context, String path) {
        return getIcon(context, path, -1, -1);
    }

    public static ImageIcon getIcon(Object context, String path, int width) {
        return getIcon(context, path, width, width);
    }

    public static ImageIcon getIcon(Object context, String path, int width, int height) {
        ImageIcon imageIcon = ICON_CACHE.get(path);
        if (imageIcon == null) {
            URL url = context.getClass().getResource(path);
            if (url == null) {
                return null;
            }

            imageIcon = new ImageIcon(url);

            if (width > 0 && height > 0) {
                imageIcon.setImage(imageIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
            }

            ICON_CACHE.put(path, imageIcon);
        }

        return imageIcon;
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
                e.printStackTrace();
            }

        }
        return bufferedImage;
    }
}
