package cn.shu.wechat.swing.utils;

import java.awt.*;

/**
 * Created by 舒新胜 on 17-5-29.
 */
public class FontUtil {
    private static final Font font;
    private static final Font iconFont;

    static {
        if (OSUtil.getOsType() == OSUtil.Windows) {
            //SansSerif
            iconFont = new Font("SansSerif", Font.PLAIN, 14);
            font=iconFont;
            //font = new Font("微软雅黑", Font.PLAIN, 14);
        } else {
            font = new Font("PingFang SC", Font.PLAIN, 14);
            iconFont = font;
        }
    }

    public static Font getDefaultFont() {
        return getDefaultFont(14, Font.PLAIN);
    }

    public static Font getDefaultFont(int size) {
        return getDefaultFont(size, Font.PLAIN);
    }

    public static Font getDefaultFont(int size, int style) {
        return font.deriveFont(style, size);
        //return new Font("YaHei Consolas Hybrid",  style, size);
        //return new Font("微软雅黑", style, size);
    }
    public static Font getDefaultIconFont() {
        return getDefaultIconFont(14, Font.PLAIN);
    }
    public static Font getDefaultIconFont(int size, int style) {
        return iconFont.deriveFont(style, size);
        //return new Font("YaHei Consolas Hybrid",  style, size);
        //return new Font("微软雅黑", style, size);
    }



}
