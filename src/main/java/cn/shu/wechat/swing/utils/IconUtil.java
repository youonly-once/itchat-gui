package cn.shu.wechat.swing.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.concurrent.Computable;

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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

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
    private static final Map<String, CompletableFuture<ImageIcon>> ICON_CACHE = new ConcurrentHashMap<>();

    private static final Map<String, CompletableFuture<BufferedImage>> BUFFERED_IMAGE_CACHE = new ConcurrentHashMap<>();

    public static ImageIcon getIcon(Object context, String path) {
        return getIcon(context, path, -1, -1);
    }

    public static ImageIcon getIcon(Object context, String path, int width) {
        return getIcon(context, path, width, width);
    }

    public static ImageIcon getIcon(Object context, String path, int width, int height) {
        String key = width > 0 && height > 0 ? String.format("%s@%dx%d", path, width, height) : path;

        try {
            CompletableFuture<ImageIcon> future = ICON_CACHE.computeIfAbsent(key, e -> {

                return CompletableFuture.supplyAsync(() -> {

                    URL url = context.getClass().getResource(path);
                    if (url == null) {
                        log.error("Icon resource not found: {}", path);
                        return null;
                    }
                    ImageIcon rawIcon = new ImageIcon(url);

                    if (width > 0 && height > 0) {
                        Image scaledImage = rawIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                        rawIcon = new ImageIcon(scaledImage);
                    }
                    return rawIcon;
                }).whenComplete((result, ex) -> {
                    // 结果为null或发生异常时，从缓存移除，允许后续重试
                    if (ex != null) {
                        log.error("图标缓存获取失败：{}", key, ex);
                        ICON_CACHE.remove(key);
                    }else if (result == null) {
                        log.error("图标缓存获取失败，result is null：{}", key);
                        ICON_CACHE.remove(key);
                    }
                });

            });
            return future.get();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ICON_CACHE.remove(key);
            return null;
        }

    }

    public static BufferedImage getBufferedImage(Object context, String path) {


        try {
            CompletableFuture<BufferedImage> future = BUFFERED_IMAGE_CACHE.computeIfAbsent(path, key -> CompletableFuture.supplyAsync(() -> {

                URL url = context.getClass().getResource(path);
                if (url == null) {
                    log.error("Icon resource not found: {}", path);
                    return null;
                }
                try {
                    return ImageIO.read(url);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).whenComplete((result, ex) -> {
                // 结果为null或发生异常时，从缓存移除，允许后续重试
                if (ex != null) {
                    log.error("图标缓存获取失败：{}", path, ex);
                    BUFFERED_IMAGE_CACHE.remove(path);
                }else if (result == null) {
                    log.error("图标缓存获取失败，result is null：{}", path);
                    BUFFERED_IMAGE_CACHE.remove(path);
                }
            }));
            return future.get();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            BUFFERED_IMAGE_CACHE.remove(path);
            return null;
        }

    }

    public static BufferedImage getBufferedImage(String path) {


        try {
            CompletableFuture<BufferedImage> future = BUFFERED_IMAGE_CACHE.computeIfAbsent(path, key -> CompletableFuture.supplyAsync(() -> {

                try {
                    return ImageIO.read(new File(path));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).whenComplete((result, ex) -> {
                // 结果为null或发生异常时，从缓存移除，允许后续重试
                if (ex != null) {
                    log.error("图标缓存获取失败：{}", path, ex);
                    BUFFERED_IMAGE_CACHE.remove(path);
                }else if (result == null) {
                    log.error("图标缓存获取失败，result is null：{}", path);
                    BUFFERED_IMAGE_CACHE.remove(path);
                }
            }));
            return future.get();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            BUFFERED_IMAGE_CACHE.remove(path);
            return null;
        }

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
