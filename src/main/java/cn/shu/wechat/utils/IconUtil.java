package cn.shu.wechat.utils;

import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
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


    /**
     * 销毁 Map 缓存：清空并断开引用
     */
    public static void destroyMapCache() {
        ICON_CACHE.clear();       // 释放 Entry 对象
        BUFFERED_IMAGE_CACHE.clear();       // 释放 Entry 对象
    }
    /**
     * 图片设置圆角
     *
     * @param srcImage
     * @param radius
     * @return
     * @throws IOException
     */
    public static BufferedImage setRadius(Image srcImage, int width, int height, int radius) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, width, height, radius, radius));

        g2.setComposite(AlphaComposite.SrcAtop);
        // 直接在绘制时缩放
        g2.drawImage(srcImage, 0, 0, width, height, null);

        g2.dispose();
        srcImage.flush();
        return image;
    }

    public static Image getScaledImageByMax(Image src, int maxWidth,int maxHeight) {
        int width = src.getWidth(null);
        int height = src.getHeight(null);
       return getScaledImage(src, width, height, maxWidth,maxHeight);
    }




    public static Image getScaledImage(Image src, int width, int height, int maxWidth,int maxHeight) {
        Dimension scaleDimen = getScaleDimension(width, height, maxWidth,maxHeight);
        if ( scaleDimen.width<=maxWidth && scaleDimen.height <= maxHeight) {
            return src; // 无需缩放
        }
        BufferedImage scaledImage = new BufferedImage(scaleDimen.width, scaleDimen.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaledImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, scaleDimen.width, scaleDimen.height, null);
        g2.dispose();
        src.flush();
        return scaledImage;
    }

    public static BufferedImage getScaledImage(Image src, int width, int height) {

        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaledImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, width, height, null);
        g2.dispose();
        src.flush();
        return scaledImage;
    }


    /**
     * 根据图片尺寸大小调整图片显示的大小
     *
     * @param
     * @param
     * @return
     */
    public static void preferredImageSize(ImageIcon imageIcon, int maxWidth,int maxHeight) {
        //动态图不能使用
        int width = imageIcon.getIconWidth();
        int height = imageIcon.getIconHeight();
        Image scaledImage = getScaledImage(imageIcon.getImage(), width, height, maxWidth,maxHeight);
        imageIcon.setImage(scaledImage);
    }

    /**
     * 根据图片尺寸大小调整图片显示的大小
     *
     * @param image
     * @param maxWidth
     * @return
     */
    public static Image preferredImageSize(BufferedImage image, int maxWidth,int maxHeight) {
        //动态图不能使用
        int width = image.getWidth();
        int height = image.getHeight();
        return getScaledImage(image, width, height, maxWidth,maxHeight);
    }

    /**
     * 获取缩放后的尺寸
     *
     * @param width    宽
     * @param height   高
     * @param maxWidth 最大宽度
     * @return 缩放后的尺寸
     */
    public static Dimension getScaleDimension(int width, int height, int maxWidth,int maxHeight) {
        if (width <= 0 || height <= 0 || maxWidth <= 0 || maxHeight <= 0) {
            log.error("width <= 0 || height <= 0 || maxWidth <= 0 || maxHeight <= 0");
            return new Dimension(1, 1); // 防止非法输入导致异常或 UI 崩溃
        }

        float widthRatio = maxWidth / (float) width;
        float heightRatio = maxHeight / (float) height;
        float scale = Math.min(widthRatio, heightRatio); // 选择更小的缩放因子以适应最大边界

        int scaledWidth = Math.max(1, Math.round(width * scale));
        int scaledHeight = Math.max(1, Math.round(height * scale));

        return new Dimension(scaledWidth, scaledHeight);
    }



    /**
     * 根据图片尺寸大小调整图片显示的大小
     *
     * @param filePath
     * @return
     */
    public static ImageIcon preferredGifSize(String filePath, int w, int h,int maxWidth,int maxHeight) {

        Dimension scaleDimen = getScaleDimension(w, h, maxWidth,maxHeight);
        if (scaleDimen.width >= w && scaleDimen.height >= h) {
            return new ImageIcon(filePath);
        }
       return preferredGifSize(filePath, scaleDimen.width, scaleDimen.height);

    }

    /**
     * 根据图片尺寸大小调整图片显示的大小
     *
     * @param filePath
     * @return
     */
    public static ImageIcon preferredGifSize(String filePath, int targetW, int targetH) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }


        try {
            String slavePath = filePath + ".slave_" + targetW + "x" + targetH;
            GifUtil.zoomGifBySize(filePath, targetW, targetH, slavePath);
            return new ImageIcon(slavePath);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据图片尺寸大小调整图片显示的大小
     *
     * @param bytes
     * @return
     */
    public static ImageIcon preferredGifSize(byte[] bytes, int w, int h,int maxWidth,int maxHeight) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            Dimension scaleDimen = getScaleDimension(w, h, maxWidth,maxHeight);
            if (scaleDimen.width == w && scaleDimen.height == h) {
                return new ImageIcon(bytes);
            }
            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
                GifUtil.zoomGifBySize(byteArrayInputStream, scaleDimen.width, scaleDimen.height, byteArrayOutputStream);
            }
            return new ImageIcon(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }


    /**
     * 判断是否为GIF
     *
     * @param file
     * @return
     */
    public static boolean isGIF(File file) {
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] header = new byte[6];
            if (in.read(header) == 6) {
                String headStr = new String(header);
                return isGifByHead(headStr);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public static boolean isGIFByFile(String path) {
        return isGIF(new File(path));
    }

    /**
     * 判断是否为GIF
     *
     * @param bytes 图像字节流
     * @return
     */
    public static boolean isGIF(byte[] bytes) {
        if (bytes.length < 6) {
            return false;
        }
        String headStr = new String(Arrays.copyOfRange(bytes, 0, 6));
        return isGifByHead(headStr);
    }

    public static boolean isGifByHead(String headStr) {
        return headStr.startsWith("GIF87a") || headStr.startsWith("GIF89a");
    }

    public static boolean isGifByFileName(String imagePath) {

        String suffix = "";
        int pos = imagePath.lastIndexOf(".");
        if (pos >= 0) {
            suffix = imagePath.substring(pos + 1).toLowerCase();
        }

        return suffix.equals("gif");
    }

    public static Dimension getImageSize(String file) {

        try (ImageInputStream iis = ImageIO.createImageInputStream(new File(file))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(iis);
                    int width = reader.getWidth(0);
                    int height = reader.getHeight(0);
                    return new Dimension(width, height);
                } finally {
                    reader.dispose();
                }
            }
        } catch (IOException e) {
            log.error("Failed to read image metadata", e);
        }
        return new Dimension(50, 50);
    }

    /**
     * @param file
     * @return
     */
    public static boolean isImage(File file) {
        String suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
        return suffix.equals("jpg") || suffix.equals("jpeg") || suffix.equals("png") || suffix.equals("gif");
    }



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
                        Image scaledImage =  getScaledImage(rawIcon.getImage(), width, height);;
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

    private static <T> BufferedImage getBufferedImage(T url) {
        return getBufferedImage(url, -1, -1);
    }

    private static <T> BufferedImage getBufferedImage(T url, int width, int height) {
        if (url == null) {
            log.error("Icon resource not found: {}", url);
            return null;
        }
        String key = switch (url) {
            case URL ignored -> ignored.getPath();
            case File ignored -> ignored.getAbsolutePath();
            default -> throw new IllegalStateException("Unexpected value: " + url);
        };
        String finalKey = width > 0 && height > 0 ? String.format("%s@%dx%d", key, width, height) : key;
        try {
            CompletableFuture<BufferedImage> future = BUFFERED_IMAGE_CACHE.computeIfAbsent(finalKey, k -> CompletableFuture.supplyAsync(() -> {

                try {
                    BufferedImage image = switch (url) {
                        case URL ignored -> ImageIO.read(ignored);
                        case File ignored -> ImageIO.read(ignored);
                        default -> throw new IllegalStateException("Unexpected value: " + url);
                    };
                    if (width == -1 && height == -1) {
                        return image;
                    }
                    return getScaledImage(image,width,height);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).whenComplete((result, ex) -> {
                // 结果为null或发生异常时，从缓存移除，允许后续重试
                if (ex != null) {
                    log.error("图标缓存获取失败：{}", finalKey, ex);
                    BUFFERED_IMAGE_CACHE.remove(finalKey);
                } else if (result == null) {
                    log.error("图标缓存获取失败，result is null：{}", finalKey);
                    BUFFERED_IMAGE_CACHE.remove(finalKey);
                }
            }));
            return future.get();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            BUFFERED_IMAGE_CACHE.remove(finalKey);
            return null;
        }

    }

    public static BufferedImage getBufferedImage(Object context, String path, int width, int height) {
        URL url = context.getClass().getResource(path);
        return getBufferedImage(url, width, height);
    }


    public static BufferedImage getBufferedImage(Object context, String path) {
        URL url = context.getClass().getResource(path);
        return getBufferedImage(url);
    }

    public static BufferedImage getBufferedImage(String path, int width, int height) {
        File file = new File(path);
        if (file.length() == 0) {
            return null;
        }
        return getBufferedImage(file,width,height);
    }

    public static BufferedImage getBufferedImage(String path) {
        return getBufferedImage(path,-1,-1);
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
