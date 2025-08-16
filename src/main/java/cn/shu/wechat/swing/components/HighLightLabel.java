package cn.shu.wechat.swing.components;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 舒新胜 on 22/06/2017.
 */
public class HighLightLabel extends JLabel {
    private String keyWord;
    private Color highLightColor;


    public HighLightLabel() {
        this(null, Color.ORANGE);
    }

    public HighLightLabel(String keyWord, Color highLightColor) {

        this.keyWord = keyWord;
        this.highLightColor = highLightColor;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public void setHighLightColor(Color highLightColor) {
        this.highLightColor = highLightColor;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setFont(getFont()); // 保证字体样式一致
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        FontMetrics fm = g2d.getFontMetrics();
        Insets insets = getInsets();

        String text = getText();
        if (text == null || text.isEmpty()) {
            g2d.dispose();
            return;
        }

        // 垂直居中计算
        int y = insets.top + (getHeight() - insets.top - insets.bottom - fm.getHeight()) / 2 + fm.getAscent();
        int x = insets.left;

        if (keyWord == null || keyWord.isEmpty()) {
            g2d.setColor(getForeground());
            g2d.drawString(text, x, y);
        } else {
            List<Integer> posArr = keyWordPositions(text, keyWord);
            int strIndex = 0;
            int posIndex = 0;
            int keyLen = keyWord.length();

            while (strIndex < text.length()) {
                if (posIndex >= posArr.size()) {
                    String s = text.substring(strIndex);
                    g2d.setColor(getForeground());
                    g2d.drawString(s, x, y);
                    break;
                }

                int pos = posArr.get(posIndex);
                String s = text.substring(strIndex, pos);
                g2d.setColor(getForeground());
                g2d.drawString(s, x, y);
                x += fm.stringWidth(s);
                strIndex += s.length();

                g2d.setColor(highLightColor);
                g2d.drawString(keyWord, x, y);
                x += fm.stringWidth(keyWord);
                strIndex += keyLen;
                posIndex++;
            }
        }

        g2d.dispose();
    }

    private List<Integer> keyWordPositions(String str, String key) {
        int keyLen = key.length();

        // 关键字是否是叠词，第一种情况匹配aa、setAvatar，第二种情况匹配如asdasd
        boolean IsReduplication = key.matches("(.)\\1+")
                || (key.length() % 2 == 0 && key.substring(0, key.length() / 2).equals(key.substring(key.length() / 2)));

        int pos = str.indexOf(key); //第一个出现的索引位置
        List<Integer> posArr = new ArrayList<>();
        while (pos != -1) {
            posArr.add(pos);
            if (IsReduplication) {
                pos = str.indexOf(key, pos + keyLen); // 如果遇到关键字是叠词的情况，则间距为一个关键字
            } else {
                pos = str.indexOf(key, pos + 1);// 从这个索引往后开始第一个出现的位置
            }
        }

        return posArr;
    }
}