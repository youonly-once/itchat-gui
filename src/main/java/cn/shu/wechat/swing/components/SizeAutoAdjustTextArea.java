package cn.shu.wechat.swing.components;

import cn.shu.wechat.swing.components.message.JIMSendTextPane;
import cn.shu.wechat.utils.EmojiUtil;
import cn.shu.wechat.utils.FontUtil;
import cn.shu.wechat.utils.OSUtil;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private String[] lineArr;
    /**
     * 约束的组件最大宽度
     */
    private final int maxWidth;
    private Object tag;
    private Pattern emojiPattern;

    private String emojiRegx;
    private int emojiSize = 20;
    private MouseAdapter mouseAdapter;
    @Setter
    private boolean parseUrl = false;

    private final Pattern wxEmojiPattern = Pattern.compile("(\\[.*?\\])");
    // 最长一行长度
    private int maxLengthLinePosition = 0;

    // 所有的url地址
    private List<String> urlList;


    // 记录每个url地址的起始位置与结束位置
    int[][] urlRange;

    private boolean isAllEmoji = true;

    private boolean existEmoji = false;


    public SizeAutoAdjustTextArea(int maxWidth) {
        this.maxWidth = maxWidth;
        setOpaque(false);
        //setLineWrap(true);
        //setWrapStyleWord(false);
        this.setFont(FontUtil.getDefaultFont(14));
        //setEditable(false);

        emojiRegx = ":.+?:";
        emojiPattern = Pattern.compile(emojiRegx);// 懒惰匹配，最小匹配
        fontMetrics = getFontMetrics(getFont());
        emojiSize = fontMetrics.getHeight();

    }


    @Override
    public void setText(String t) {
        // 对emoji的Unicode编码转别名

  /*      try{
            t = EmojiParser.parseToAliases(t);
        }catch (Exception e){

        }*/

        if (t == null) {
            return;
        }

        // 总行数
        int lineCount = parseLineCount(t);
        if (lineCount == 0) {
            return;
        }

        // 每一行的信息
        List<Line> lineEmojiInfoList = parseLineEmojiInfo();


        int lineHeight = fontMetrics.getHeight();

        int targetHeight = lineHeight * lineCount;
        int targetWidth = 20;

        Insets borderInsets = this.getBorder().getBorderInsets(this);
        Insets marginInsets = this.getMargin();

        if (lineCount > 0) {
            targetWidth = maxLengthLinePosition + borderInsets.left + borderInsets.right + marginInsets.left + marginInsets.right;
        }

        // 如果最长的一行宽度超过了最大宽度，就要重新计算高度
        int totalLine = 0;
        if (targetWidth > maxWidth) {
            targetWidth = maxWidth;


            for (Line line : lineEmojiInfoList) {
                int ret = line.getLineWidth() / maxWidth;
                int l = ret == 0 ? ret : ret + 1;
                totalLine += l == 0 ? 1 : l;
            }

            targetHeight = lineHeight * totalLine;
        }


       // String targetText = t.replaceAll(emojiRegx, "");
//        for (String code : EmojiUtil.getWechatEmojiList()) {
//            targetText = targetText.replace(code, "");
//        }
        super.setText("");

        // 插入emoji表情，并计算需要增加的高度
        //Map<Integer, String> emojiPositionMap = insertEmoji(t);

        try {
            insertWechatEmoji(lineEmojiInfoList);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }

//        String exceptEmoji = t.replaceAll("\\r\\n", "\n");
//        for (String emoji : emojiPositionMap.values()) {
//            exceptEmoji = exceptEmoji.replace(emoji, "\0");
//        }
        // int emojiCount = emojiPositionMap.size();

        //全是emoji的情况
        if (isAllEmoji) {
            int emojiCount = lineEmojiInfoList.get(0).getEmojiList().size();
            int totalWidth = emojiCount * emojiSize;
            targetWidth = maxLengthLinePosition;
            targetHeight = emojiSize;
            if (totalWidth > maxWidth) {
                targetWidth = maxWidth;
                int ret = totalWidth / maxWidth;
                int l = ret == 0 ? ret : ret + 1;
                targetHeight = l * emojiSize;
            }

            int emojiExtraHeight = 0;
            if (totalLine > 1) {
                int i = 0;
                for (Line line : lineEmojiInfoList) {
                    if (!line.getEmojiList().isEmpty()) {
                        emojiExtraHeight += (i * 5);
                        i++;
                    }
                }
            }
            int h = OSUtil.getOsType() == OSUtil.Mac_OS ? 0 : 3;
            setPreferredSize(new Dimension(targetWidth, targetHeight + h + emojiExtraHeight));
            return;
        }
//
//        int emojiExtraHeight = OSUtil.getOsType() == OSUtil.Mac_OS ? 8 : 10;
//        int emojiIndex = 1;
//        for (int pos : emojiPositionMap.keySet()) {
//            String substr = exceptEmoji.substring(0, pos + 1);
//            int width = fontMetrics.stringWidth(substr) + emojiSize;
//            if (width > maxWidth || substr.contains("\n")) {
//                targetHeight += emojiExtraHeight;
//                break;
//            }
//            emojiIndex++;
//        }
//        if (emojiIndex < emojiCount) {
//            targetHeight += (emojiCount - emojiIndex) * emojiExtraHeight;
//        }

        // 如果有emoji表情，高度就要适当增加
        int emojiExtraHeight = 0;
        if (totalLine > 1) {
            int i = 0;
            for (Line line : lineEmojiInfoList) {
                if (!line.getEmojiList().isEmpty()) {
                    emojiExtraHeight += (i * 20);
                    i++;
                }
            }
        } else {
            emojiExtraHeight = 2;
        }


        if (isAllEmoji) {
            this.setPreferredSize(new Dimension(targetWidth, targetHeight + emojiExtraHeight));
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
     * 网址高亮
     *
     * @param src
     */
    private void highlightUrls(String src) {
        urlList = parseUrl(src);
        urlRange = new int[urlList.size()][2];
        if (!urlList.isEmpty()) {
            setListeners();
        }else{
            removeMouseAdapter();
        }
        SimpleAttributeSet bSet = new SimpleAttributeSet();
        StyleConstants.setForeground(bSet, Color.blue);
        StyleConstants.setUnderline(bSet, true);
        StyledDocument doc = getStyledDocument();
        doc.setCharacterAttributes(0, src.length(), getCharacterAttributes(), true);

        int startIndex = 0;
        int endIndex = 0;
        for (int i = 0; i < urlList.size(); i++) {
            String url = urlList.get(i);
            startIndex = src.indexOf(url);
            if (startIndex > -1) {
                endIndex = startIndex + url.length();
                doc.setCharacterAttributes(src.indexOf(url, startIndex), url.length(), bSet, false);

                urlRange[i][0] = startIndex;
                urlRange[i][1] = endIndex;

                startIndex++;
            }
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
                SimpleAttributeSet spaceAttr = new SimpleAttributeSet();
                StyleConstants.setFontSize(spaceAttr, 1); // 最小字体
                StyleConstants.setForeground(spaceAttr, new Color(0, 0, 0, 0)); // 完全透明
                doc.insertString(insertPos + 1, " ", spaceAttr);

                emojiLen += (emojiInfo.getName().length() - 1) + 1; // 每插入一个图标，占用一个字符长度

            }
        }
    }

    /**
     * 分析每一行的emoji数量
     *
     * @return
     */
    private List<Line> parseLineEmojiInfo() {
        List<Line> infoList = new ArrayList<>();
        for (int i = 0; i < lineArr.length; i++) {
            Line line = parseEmoji(lineArr[i]);
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

        int lineWidth = fontMetrics.stringWidth(plainText);

        if (lineWidth > 0) isAllEmoji = false;

        lineWidth += emojiList.size() * emojiSize;


        //因为在插入表情的时候插入了额外的空格，这里要添加这一部分宽度
        String s = " ".repeat(emojiList.size());
        lineWidth += fontMetrics.stringWidth(s);

        if (lineWidth > maxLengthLinePosition) {
            maxLengthLinePosition = lineWidth;
        }
        return Line.builder().emojiList(emojiList)
                .lineWidth(lineWidth).str(plainText).build();
    }


    /**
     * 分析消息文本一共有几行
     *
     * @param text 消息文本
     * @return 消息的行数，以\n分隔
     */
    private int parseLineCount(String text) {
        lineArr = text.split("\\n");

        return lineArr.length;
    }

    private List<String> parseUrl(String src) {
        List<String> urlList = new ArrayList<>();


        //long start = System.currentTimeMillis();
        String regex = "(?:https?://)?(www\\.)?[\\w]+(?:\\.[\\w]+)+[\\w,\\-_/?&=#%.:]*";
        Pattern urlPattern = Pattern.compile(regex);
        Matcher urlMatcher = urlPattern.matcher(src);
        while (urlMatcher.find()) {
            urlList.add(urlMatcher.group());
        }

        //System.out.println("花费时间 ：" + (System.currentTimeMillis() - start));

        return urlList;
    }


    private void setListeners() {
        if (mouseAdapter ==null) {
            mouseAdapter = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (StringUtils.isEmpty(urlRange)) {
                            return;
                        }
                        int position = getCaretPosition();
                        int urlIndex = 0;
                        for (int[] range : urlRange) {
                            if (position >= range[0] && position <= range[1]) {
                                String url = urlList.get(urlIndex);
                                openUrlWithDefaultBrowser(url);
                            }

                            urlIndex++;
                        }
                    }

                    super.mouseClicked(e);
                }
            };
        }
        this.addMouseListener(mouseAdapter);
    }
    public void removeMouseAdapter() {
        if (mouseAdapter != null) {
            removeMouseListener(mouseAdapter);
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
        List<EmojiInfo> emojiList;
        int lineWidth;
        int lineNumber;
        String str;
    }

    @Data
    @Builder
    static
    class EmojiInfo {
        String name;
        int start;
        int end;
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

