package cn.shu.wechat.swing.adapter.message.video;

import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GradientProgressBarUI;
import cn.shu.wechat.swing.components.RCProgressBar;
import cn.shu.wechat.swing.components.message.RCLeftVideoMessageBubble;
import cn.shu.wechat.swing.components.message.TagJLayeredPane;
import cn.shu.wechat.swing.components.message.TagPanel;
import cn.shu.wechat.utils.FontUtil;
import cn.shu.wechat.utils.IconUtil;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Created by 舒新胜 on 17-6-2.
 */

public class MessageVideoViewHolder extends BaseMessageViewHolder {
    public static final int maxHeight = 120;
    public static final int maxWidth = 80;
    public final RCLeftVideoMessageBubble imageBubble = new RCLeftVideoMessageBubble();
    /**
     * 缩略图label
     */
    public final JLabel slaveImgLabel = new JLabel();
    /**
     * 播放按钮label
     */
    public final JLabel playImgLabel = new JLabel();
    /**
     * 播放时长label
     */
    public final JLabel timeLabel = new JLabel();
    protected final TagPanel contentTagPanel = new TagPanel();
    protected final JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    protected final JPanel messageAvatarPanel = new JPanel();
    protected final int slaveImgWidth;
    protected final int slaveImgHeight;
    public RCProgressBar progressBar = new RCProgressBar();
    public TagJLayeredPane videoComponent = null;

    /**
     * @param dimension 缩略图尺寸
     */
    public MessageVideoViewHolder(Dimension dimension) {
        this.slaveImgHeight = dimension.height;
        this.slaveImgWidth = dimension.width;

    }

    protected void initComponents() {
        timePanel.setBackground(Colors.WINDOW_BACKGROUND);
        messageAvatarPanel.setBackground(Colors.WINDOW_BACKGROUND);


        time.setForeground(Colors.FONT_GRAY);
        time.setFont(FontUtil.getDefaultFont(12));


        progressBar.setMaximum(100);
        progressBar.setMinimum(0);
        progressBar.setValue(0);
        progressBar.setUI(new GradientProgressBarUI());
        progressBar.setVisible(true);
    }

    /**
     * @return 组件
     * @throws IOException 读取文件异常
     */
    protected TagJLayeredPane getLayerPanel() {
        TagJLayeredPane layeredPane = new TagJLayeredPane();
        JPanel imgPanel = new JPanel(new GridLayout(1, 1));
        imgPanel.setBounds(0, 0, slaveImgWidth, slaveImgHeight);
        imgPanel.setOpaque(false);
        imgPanel.add(slaveImgLabel);
        layeredPane.add(imgPanel, 200, 1);

        //播放按钮
        ImageIcon icon = IconUtil.getIcon(this, "/image/play48.png");
        int playHeight = icon.getIconHeight();
        int playWidth = icon.getIconWidth();
        int x = (slaveImgWidth - playWidth) / 2;
        int y = (slaveImgHeight - playHeight) / 2;

        JPanel playImgPanel = new JPanel(new GridLayout(1, 1));
        playImgPanel.setBounds(x, y, playWidth, playHeight);
        playImgPanel.setOpaque(false);
        playImgPanel.add(playImgLabel);
        layeredPane.add(playImgPanel, 200, 0);

        layeredPane.setPreferredSize(new Dimension(slaveImgWidth, slaveImgHeight));
        //视频时长
        timeLabel.setForeground(Color.white);
        int margin = 3;
        int timeWidth = 30;
        int timeHeight = 10;
        timeLabel.setBounds(slaveImgWidth - timeWidth - margin, slaveImgHeight - timeHeight - margin, timeWidth, timeHeight);
        timeLabel.setOpaque(false);
        timeLabel.setFont(FontUtil.getDefaultFont(10));
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        layeredPane.add(timeLabel, 200, 0);


        return layeredPane;
    }
}
