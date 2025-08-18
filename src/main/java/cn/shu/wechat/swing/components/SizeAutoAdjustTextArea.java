package cn.shu.wechat.swing.components;

import cn.shu.wechat.swing.components.message.JIMSendTextPane;
import cn.shu.wechat.utils.EmojiUtil;
import cn.shu.wechat.utils.FontUtil;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by 舒新胜 on 17-6-4.
 */
public class SizeAutoAdjustTextArea extends JIMSendTextPane {
    private final FontMetrics fontMetrics;

    /**
     * 约束的组件最大宽度
     */
    private final int maxWidth;


    private static final Pattern emojiPattern = Pattern.compile(":.+?:");

    private static final Pattern urlPattern = Pattern.compile("(?:https?://)?(www\\.)?[\\w]+(?:\\.[\\w]+)+[\\w,\\-_/?&=#%.:]*");

    private int emojiSize = 20;
    private MouseAdapter mouseAdapter;
    private static final Pattern wxEmojiPattern = Pattern.compile("(\\[.*?\\])");
    @Setter
    private boolean parseUrl = false;
    private MouseMotionAdapter mouseMotionListener;
    // 最长一行长度
    private int maxLengthLine = 0;

    private boolean isAllEmoji = true;

    private boolean existEmoji = false;


    public SizeAutoAdjustTextArea(int maxWidth) {
        this.maxWidth = maxWidth;
        setOpaque(false);
        //setLineWrap(true);
        //setWrapStyleWord(false);
        this.setFont(FontUtil.getDefaultFont(14));
        //setEditable(false);

        fontMetrics = getFontMetrics(getFont());
        emojiSize = fontMetrics.getHeight();

    }


    @Override
    public void setText(String t) {
        if (t == null) {
            return;
        }

        // 每一行的信息
        List<Line> lineEmojiInfoList = parseLineEmojiInfo(t);

        if (lineEmojiInfoList.isEmpty()) {
            return;
        }
        super.setText("");
        int targetWidth = maxLengthLine + 7;

        int totalLine = 0;
        int targetHeight = 0;
        if (targetWidth > maxWidth) {
            targetWidth = maxWidth;
        }
        for (Line line : lineEmojiInfoList) {
            Dimension dim = computeLineDimension(line);
            targetHeight += dim.height;
            targetWidth = Math.max(targetWidth, dim.width);

        }

        try {
            insertWechatEmoji(lineEmojiInfoList);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }

        if (isAllEmoji) {
            this.setPreferredSize(new Dimension(targetWidth, targetHeight));
        } else {
            if (existEmoji) {
                this.setPreferredSize(new Dimension(targetWidth, targetHeight + 2 + 5));
            } else {
                this.setPreferredSize(new Dimension(targetWidth, targetHeight + 2));
            }
        }
        if (parseUrl) {
            // 解析网址
            highlightUrls(t);
        }

    }
    /**
     * 精确计算单行的实际宽度和换行带来的高度
     */
    private Dimension computeLineDimension(Line line) {
        int lineHeight = line.getLineHeight();
        int width = 0;
        int maxLineWidth = 0;
        int totalHeight = lineHeight;

        String str = line.getStr();
        int emojiIdx = 0;

        TabExpander expander = (x, tabOffset) -> {
            int tabSize = 22 * fontMetrics.charWidth(' ');
            return ((x / tabSize) + 1) * tabSize;
        };

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int charWidth;

            if (c == '\t') {
                charWidth = (int) (expander.nextTabStop(width, i) - width);
            } else {
                charWidth = fontMetrics.charWidth(c);
            }

            if (width + charWidth > maxWidth) {
                // 换行
                maxLineWidth = Math.max(maxLineWidth, width);
                width = charWidth;
                totalHeight += lineHeight;
            } else {
                width += charWidth;
            }
        }

        // 补偿行末宽度
        maxLineWidth = Math.max(maxLineWidth, width);

        // 加上 emoji 宽度
        if (!line.getEmojiList().isEmpty()) {
            for (EmojiInfo emoji : line.getEmojiList()) {
                int emojiWidth = emojiSize;
                if (width + emojiWidth > maxWidth) {
                    maxLineWidth = Math.max(maxLineWidth, width);
                    width = emojiWidth;
                    totalHeight += lineHeight;
                } else {
                    width += emojiWidth;
                }
            }
            maxLineWidth = Math.max(maxLineWidth, width);
        }

        return new Dimension(maxLineWidth, totalHeight);
    }
    /**
     * 网址高亮
     *
     * @param src
     */
    private void highlightUrls(String src) {
        List<UrlInfo> urlInfos = parseUrl(src);
        if (!urlInfos.isEmpty()) {
            setListeners();
        }else{
            removeMouseAdapter();
        }

        StyledDocument doc = getStyledDocument();
        doc.setCharacterAttributes(0, src.length(), getCharacterAttributes(), true);

        for (UrlInfo url : urlInfos) {
            SimpleAttributeSet bSet = new SimpleAttributeSet();
            StyleConstants.setForeground(bSet, Color.blue);
            StyleConstants.setUnderline(bSet, true);
            bSet.addAttribute("url", url.getUrl());
            doc.setCharacterAttributes(url.getStart(), url.getUrl().length(), bSet, false);
        }
    }


    private void insertWechatEmoji(List<Line> lineList) throws BadLocationException {

        StyledDocument doc = this.getStyledDocument();

        for (Line line : lineList) {
            int length = doc.getLength();
            doc.insertString(length, line.getStr() + "\n", getCharacterAttributes());
            int emojiLen = 0;
            for (EmojiInfo emojiInfo : line.getEmojiList()) {
                ImageIcon rawIcon = EmojiUtil.getWeChatEmoji(this, emojiInfo.getName(), emojiSize, emojiSize);
                Icon icon = new CenteredImageIcon(rawIcon.getImage(), emojiSize, emojiSize, fontMetrics);
                // 计算插入点
                int insertPos = length + emojiInfo.getStart() - emojiLen;

                // 插入图标占位符
                SimpleAttributeSet iconAttr = new SimpleAttributeSet();
                StyleConstants.setIcon(iconAttr, icon);
                doc.insertString(insertPos, "\uFFFC", iconAttr);

                //中间插入空格 不然多个表情会被覆盖
                //最后一个表情不用
                SimpleAttributeSet spaceAttr = new SimpleAttributeSet();
                //StyleConstants.setFontSize(spaceAttr, 1); // 最小字体
                StyleConstants.setForeground(spaceAttr, new Color(0, 0, 0, 0)); // 完全透明
                doc.insertString(insertPos + 1, "", spaceAttr);

                emojiLen += (emojiInfo.getName().length() - 1) + 1; // 每插入一个图标，占用一个字符长度
            }

        }
    }

    /**
     * 分析每一行的emoji数量
     *
     * @return
     */
    private List<Line> parseLineEmojiInfo(String text) {
        List<Line> infoList = new ArrayList<>();
        String[] split = text.split("\\n");
        for (int i = 0; i < split.length; i++) {
            Line line = parseEmoji(split[i]);
            line.setLineNumber(i);
            infoList.add(line);
        }

        return infoList;
    }

    /**
     * 提取字符串中的所有emoji表情编码
     *
     * @param src
     * @return
     */
    private Line parseEmoji(String src) {
        boolean existEmoji = false;
        List<EmojiInfo> emojiList = new ArrayList<>();
        //不包含表情的纯文本
        String plainText = src;
        //查找微信表情
        Matcher matcher = wxEmojiPattern.matcher(src);
        while (matcher.find()) {
            String extracted = matcher.group(1); // 获取捕获组中的内容
            if (EmojiUtil.getWechatEmojiList().contains(extracted)) {
                plainText = plainText.replace(extracted, "");
                int start = matcher.start();  // 当前匹配起始索引
                int end = matcher.end();
                emojiList.add(EmojiInfo.builder()
                        .name(extracted)
                        .start(start).end(end).build());
                existEmoji = true;
            }
        }


        //其它表情
        Matcher emojiMatcher = emojiPattern.matcher(src);
        while (emojiMatcher.find()) {
            String code = emojiMatcher.group();
            if (EmojiUtil.isRecognizableEmoji(this, code)) {
                int start = matcher.start();  // 当前匹配起始索引
                int end = matcher.end();
                emojiList.add(EmojiInfo.builder()
                        .name(code)
                        .start(start).end(end).build());
                plainText = plainText.replace(code, "");
                existEmoji = true;
            }
        }
        if (existEmoji) {
            this.existEmoji = true;
        }
        int lineWidth = getTextWidth(plainText);

        if (lineWidth > 0) isAllEmoji = false;

        lineWidth += emojiList.size() * emojiSize;


        if (lineWidth > maxLengthLine) {
            maxLengthLine = lineWidth;
        }
        return Line.builder().emojiList(emojiList)
                .lineWidth(lineWidth)
                .str(plainText)
                .lineHeight(existEmoji ? fontMetrics.getHeight() + 3 : fontMetrics.getHeight())
                .existEmoji(existEmoji)
                .build();
    }

    private int getTextWidth(String text) {
        TabExpander expander = (x, tabOffset) -> {
            int tabSize = 22 * fontMetrics.charWidth(' ');
            return ((x / tabSize) + 1) * tabSize;
        };
        return (int)Math.ceil(Utilities.getTabbedTextWidth(new Segment(text.toCharArray(), 0, text.length()), fontMetrics, 0f, expander, 0));
    }
    private List<UrlInfo> parseUrl(String src) {
        List<UrlInfo> urlList = new ArrayList<>();

        Matcher urlMatcher = urlPattern.matcher(src);
        while (urlMatcher.find()) {
            String group = urlMatcher.group();
            int start = urlMatcher.start();  // 当前匹配起始索引
            int end = urlMatcher.end();
            urlList.add(UrlInfo.builder().url(group).start(start).end(end).build());
        }
        return urlList;
    }


    private void setListeners() {

            mouseAdapter = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        int pos = SizeAutoAdjustTextArea.this.viewToModel2D(e.getPoint());
                        Element elem = SizeAutoAdjustTextArea.this.getStyledDocument().getCharacterElement(pos);
                        AttributeSet as = elem.getAttributes();

                        if (StyleConstants.isUnderline(as) && Color.blue.equals(StyleConstants.getForeground(as))) {
                            openUrlWithDefaultBrowser(as.getAttribute("url").toString());
                        }

                    }

                    super.mouseClicked(e);
                }
            };

        mouseMotionListener = new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int pos = SizeAutoAdjustTextArea.this.viewToModel2D(e.getPoint());
                Element elem = SizeAutoAdjustTextArea.this.getStyledDocument().getCharacterElement(pos);
                AttributeSet as = elem.getAttributes();

                if (StyleConstants.isUnderline(as) && Color.blue.equals(StyleConstants.getForeground(as))) {
                    SizeAutoAdjustTextArea.this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    SizeAutoAdjustTextArea.this.setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        this.addMouseMotionListener(mouseMotionListener);

        this.addMouseListener(mouseAdapter);
    }
    public void removeMouseAdapter() {
        if (mouseAdapter != null) {
            removeMouseListener(mouseAdapter);
        }
        if (mouseMotionListener != null) {
            removeMouseMotionListener(mouseMotionListener);
        }

    }
    /**
     * 打开默认浏览器访问页面
     */
    public void openUrlWithDefaultBrowser(String url) {
        //启用系统默认浏览器来打开网址。
        try {
            URI uri = new URI(url);
            Desktop.getDesktop().browse(uri);
        } catch (Exception e) {
            System.out.println("URL打开失败");
        }
    }

    @Override
    public void removeNotify() {
        // 清理资源
        removeMouseAdapter();
        for (CaretListener caretListener : getCaretListeners()) {
            removeCaretListener(caretListener);
        }
        setCaret(null);
        super.removeNotify();
    }


    @Data
    @Builder
    static class Line {
        private List<EmojiInfo> emojiList;
        private int lineWidth;
        private int lineNumber;
        private int lineHeight;
        private boolean existEmoji;
        private String str;
    }

    @Data
    @Builder
    static
    class UrlInfo {
        private String url;
        private int start;
        private int end;
    }
    @Data
    @Builder
    static
    class EmojiInfo {
        private String name;
        private int start;
        private int end;
    }

    public class CenteredImageIcon implements Icon {
        private final Image image;
        private final int imageWidth;
        private final int imageHeight;
        private final int containerHeight; // 行高

        public CenteredImageIcon(Image image, int imageWidth, int imageHeight, FontMetrics fm) {
            this.image = image;
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
            this.containerHeight = fm.getHeight(); // 用于占位
        }

        @Override
        public int getIconWidth() {
            return imageWidth;
        }

        @Override
        public int getIconHeight() {
            return containerHeight;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            // 以整行的垂直中点居中
            int drawY = y + (containerHeight - imageHeight) / 2;
            if (isAllEmoji) {
                g.drawImage(image, x, drawY, imageWidth, imageHeight, c);
            } else {
                g.drawImage(image, x, drawY + SizeAutoAdjustTextArea.this.getInsets().top, imageWidth, imageHeight, c);
            }

        }
    }

}

