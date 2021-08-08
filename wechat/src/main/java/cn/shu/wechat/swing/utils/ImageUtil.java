package cn.shu.wechat.swing.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 图像处理工具类
 * <p>
 * Created by 舒新胜 on 2017/6/24.
 */
public class ImageUtil {
    /**
     * 图片设置圆角
     *
     * @param srcImage
     * @param radius
     * @return
     * @throws IOException
     */
    public static BufferedImage setRadius(Image srcImage, int width, int height, int radius) throws IOException {

        if (srcImage.getWidth(null) > width || srcImage.getHeight(null) > height) {
            // 图片过大，进行缩放
            ImageIcon imageIcon = new ImageIcon();
            imageIcon.setImage(srcImage.getScaledInstance(width, height, Image.SCALE_SMOOTH));
            srcImage = imageIcon.getImage();
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gs = image.createGraphics();
        gs.setComposite(AlphaComposite.Src);
        gs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gs.setColor(Color.WHITE);
        gs.fill(new RoundRectangle2D.Float(0, 0, width, height, radius, radius));
        gs.setComposite(AlphaComposite.SrcAtop);
        gs.drawImage(srcImage, 0, 0, null);
        gs.dispose();
        return image;
    }

    /**
     * 根据图片尺寸大小调整图片显示的大小
     * @param imageIcon
     * @param maxWidth
     * @return
     */
    public static ImageIcon preferredImageSize(ImageIcon imageIcon,int maxWidth) {
        //动态图不能使用
        int width = imageIcon.getIconWidth();
        int height = imageIcon.getIconHeight();
        Dimension scaleDimen = getScaleDimen(width, height, maxWidth);
        imageIcon.setImage(imageIcon.getImage().getScaledInstance(scaleDimen.width, scaleDimen.height, Image.SCALE_SMOOTH));
        return imageIcon;
    }
    /**
     * 根据图片尺寸大小调整图片显示的大小
     * @param image
     * @param maxWidth
     * @return
     */
    public static Image preferredImageSize(BufferedImage image,int maxWidth) {
        //动态图不能使用
        int width = image.getWidth();
        int height = image.getHeight();
        Dimension scaleDimen = getScaleDimen(width, height, maxWidth);
        return image.getScaledInstance(scaleDimen.width, scaleDimen.height, Image.SCALE_SMOOTH);
    }

    /**
     * 获取缩放后的尺寸
     * @param width 宽
     * @param height 高
     * @param maxWidth 最大宽度
     * @return 缩放后的尺寸
     */
    public static Dimension getScaleDimen(int width,int height,int maxWidth){
        //动态图不能使用
        float scale = width * 1.0F / height;

        // 限制图片显示大小
        int maxImageWidth = maxWidth;
        if (width > maxImageWidth) {
            width = maxImageWidth;
            height = (int) (width / scale);
        }
        return new Dimension(width,height);
    }
    /**
     * 获取缩放后的尺寸
     * @param width 宽
     * @param height 高
     * @return 缩放后的尺寸
     */
    public static Dimension getScaleDimen(int width,int height){
        return getScaleDimen(width,height,128);
    }
    /**
     * 根据图片尺寸大小调整图片显示的大小
     * @param imageIcon
     * @return
     */
    public static ImageIcon preferredImageSize(ImageIcon imageIcon) {
        return preferredImageSize(imageIcon,128);
    }
}
