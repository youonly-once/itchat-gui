package cn.shu.wechat.swing.components.message;

import cn.shu.wechat.swing.utils.FontUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * 文本气泡
 * <p>
 * Created by 舒新胜 on 17-6-3.
 */
public class RCTextMessageBubble extends JTextArea implements RCMessageBubble {
    private NinePatchImageIcon backgroundNormalIcon;
    private NinePatchImageIcon backgroundActiveIcon;
    private Icon currentBackgroundIcon;
    //private String[] lineArr;


    public RCTextMessageBubble() {
        setOpaque(false);
        setLineWrap(true);
        setWrapStyleWord(false);
        this.setFont(FontUtil.getDefaultFont(14));
        setEditable(false);

        setListener();
    }

    @Override
    public void setBackgroundIcon(Icon icon) {
        currentBackgroundIcon = icon;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (currentBackgroundIcon != null) {
            currentBackgroundIcon.paintIcon(this, g, 0, 0);
        }
        super.paintComponent(g);
    }

    private void setListener() {
        addMouseListener(getMouseListener());
    }

    /*@Override
    public void setText(String t)
    {
        if (t == null)
        {
            return;
        }

        int maxWidth = (int) (MainFrame.getContext().currentWindowWidth * 0.5);
        FontMetrics fm = getFontMetrics(getFont());

        int[] info = parseLineCountAndMaxLengthPosition(getText());
        int lineCount = info[0];
        int lineHeight = fm.getHeight();

        int targetHeight = lineHeight * lineCount + 20;
        int targetWidth = fm.stringWidth(lineArr[info[1]]) + 25;


        if (targetWidth > maxWidth)
        {
            targetWidth = maxWidth;

            // 解析每一行的宽度
            int totalLine = 0;
            for (String line : lineArr)
            {
                int w = fm.stringWidth(line);
                int ret = w / (maxWidth - 25);
                int l = ret == 0 ? ret : ret + 1;
                totalLine += l == 0 ? 1 : l;
            }
            targetHeight = lineHeight * totalLine + 20;
        }

        this.setPreferredSize(new Dimension(targetWidth, targetHeight));

        super.setText(t);
    }*/

    @Override
    public Insets getInsets() {
        return new Insets(10, 10, 10, 10);
    }

    /*public int[] parseLineCountAndMaxLengthPosition(String text)
    {
        int[] retArr = new int[2];

        lineArr = text.split("\\n");
        int maxLength = 0;
        int position = 0;
        for (int i = 0; i < lineArr.length; i++)
        {
            if (lineArr[i].length() > maxLength)
            {
                maxLength = lineArr[i].length();
                position = i;
            }
        }

        retArr[0] = lineArr.length;
        retArr[1] = position;

        return retArr;
    }*/


    @Override
    public NinePatchImageIcon getBackgroundNormalIcon() {
        return backgroundNormalIcon;
    }

    public void setBackgroundNormalIcon(NinePatchImageIcon backgroundNormalIcon) {
        this.backgroundNormalIcon = backgroundNormalIcon;
    }

    @Override
    public NinePatchImageIcon getBackgroundActiveIcon() {
        return backgroundActiveIcon;
    }

    @Override
    public void setActiveStatus(boolean status) {
        if (status) {
            setBackgroundIcon(backgroundActiveIcon);
        } else {
            setBackgroundIcon(backgroundNormalIcon);
        }

        this.repaint();
    }



    public void setBackgroundActiveIcon(NinePatchImageIcon backgroundActiveIcon) {
        this.backgroundActiveIcon = backgroundActiveIcon;
    }

    public Icon getCurrentBackgroundIcon() {
        return currentBackgroundIcon;
    }

    public void setCurrentBackgroundIcon(Icon currentBackgroundIcon) {
        this.currentBackgroundIcon = currentBackgroundIcon;
    }

    @Override
    public synchronized void addMouseListener(MouseListener l) {
        for (MouseListener listener : getMouseListeners()) {
            if (listener == l) {
                return;
            }
        }

        super.addMouseListener(l);
    }
}
