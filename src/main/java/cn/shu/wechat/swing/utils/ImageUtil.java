package cn.shu.wechat.swing.utils;

import cn.shu.wechat.utils.GifUtil;
import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

/**
 * 图像处理工具类
 * <p>
 * Created by 舒新胜 on 2017/6/24.
 */
@Log4j2
public class ImageUtil {

    private static final int maxWidth = 98;


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
        return image;
    }

    private static Image getScaledImage(Image src, int width, int height, int maxWidth) {
        Dimension scaleDimen = getScaleDimension(width, height, maxWidth);
        if (width <= scaleDimen.width && height <= scaleDimen.height) {
            return src; // 无需缩放
        }
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaledImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, scaleDimen.width, scaleDimen.height, null);
        g2.dispose();
        return scaledImage;
    }


    /**
     * 根据图片尺寸大小调整图片显示的大小
     * @param
     * @param
     * @return
     */
    public static void preferredImageSize(ImageIcon imageIcon, int maxWidth) {
        //动态图不能使用
        int width = imageIcon.getIconWidth();
        int height = imageIcon.getIconHeight();
        Image scaledImage = getScaledImage(imageIcon.getImage(), width, height, maxWidth);
        Image oldImage = imageIcon.getImage();
        if (oldImage != null) {
            oldImage.flush(); // 主动释放
        }
        imageIcon.setImage(scaledImage);
    }
    /**
     * 根据图片尺寸大小调整图片显示的大小
     * @param image
     * @param maxWidth
     * @return
     */
    public static Image preferredImageSize(BufferedImage image, int maxWidth) {
        //动态图不能使用
        int width = image.getWidth();
        int height = image.getHeight();
        return getScaledImage(image, width, height, maxWidth);
    }

    /**
     * 获取缩放后的尺寸
     * @param width 宽
     * @param height 高
     * @param maxWidth 最大宽度
     * @return 缩放后的尺寸
     */
    public static Dimension getScaleDimension(int width, int height, int maxWidth) {
        if (width <= 0 || height <= 0 || maxWidth <= 0) {
            return new Dimension(1, 1); // 防止非法输入导致异常或 UI 崩溃
        }

        if (width <= maxWidth) {
            return new Dimension(width, height); // 不需要缩放
        }

        float aspectRatio = width / (float) height;
        int scaledHeight = Math.max(1, Math.round(maxWidth / aspectRatio)); // 避免高度为 0

        return new Dimension(maxWidth, scaledHeight);
    }
    /**
     * 获取缩放后的尺寸
     * @param width 宽
     * @param height 高
     * @return 缩放后的尺寸
     */
    public static Dimension getScaleDimension(int width, int height) {
        return getScaleDimension(width, height, maxWidth);
    }
    /**
     * 根据图片尺寸大小调整图片显示的大小
     * @param imageIcon
     * @return
     */
    public static void preferredImageSize(ImageIcon imageIcon) {
        preferredImageSize(imageIcon, maxWidth);
    }

    /**
     * 根据图片尺寸大小调整图片显示的大小
     * @param filePath
     * @return
     */
    public static ImageIcon preferredGifSize(String filePath,int w,int h) {
        if (filePath == null||filePath.isEmpty()){
            return null;
        }
        File file = new File(filePath);
        if (!file.exists()){
            return null;
        }
        try {
            Dimension scaleDimen = getScaleDimension(w, h, maxWidth);
            if (scaleDimen.width <= w && scaleDimen.height <= h) {
                return new ImageIcon(filePath);
            }
            String slavePath = filePath + ".slave_" + scaleDimen.width + "x" + scaleDimen.height;

            GifUtil.zoomGifBySize(filePath, scaleDimen.width, scaleDimen.height, slavePath);
            return new ImageIcon(slavePath);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
    /**
     * 根据图片尺寸大小调整图片显示的大小
     * @param filePath
     * @return
     */
    public static ImageIcon preferredGifSizeWithTargetDimension(String filePath,int targetW,int targetH) {
        if (filePath == null||filePath.isEmpty()){
            return null;
        }
        File file = new File(filePath);
        if (!file.exists()){
            return null;
        }
        try {
            String slavePath = filePath + ".slave_" + targetW + "x" + targetH;
            GifUtil.zoomGifBySize(filePath, targetW, targetH, slavePath);
            return new ImageIcon(filePath + slavePath);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
    /**
     * 根据图片尺寸大小调整图片显示的大小
     * @param bytes
     * @return
     */
    public static ImageIcon preferredGifSize(byte[] bytes, int w, int h) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            Dimension scaleDimen = getScaleDimension(w, h, maxWidth);
            if (scaleDimen.width ==w && scaleDimen.height == h){
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


    /**
     * 获取图片的宽高
     *
     * @param file
     * @return
     */
    public static Dimension getImageSize(String file) {
        try {
            BufferedImage image = ImageIO.read(new File(file));
            return new Dimension(image.getWidth(), image.getHeight());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return new Dimension(0, 0);
    }

    /**
     * @param file
     * @return
     */
    public static boolean isImage(File file) {
        String suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
        return suffix.equals("jpg") || suffix.equals("jpeg") || suffix.equals("png") || suffix.equals("gif");
    }

}
