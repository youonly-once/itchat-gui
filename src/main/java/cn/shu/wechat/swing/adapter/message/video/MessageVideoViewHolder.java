package cn.shu.wechat.swing.adapter.message.video;

import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GradientProgressBarUI;
import cn.shu.wechat.swing.components.RCProgressBar;
import cn.shu.wechat.swing.components.VerticalFlowLayout;
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


    public final TagPanel videoProgressBarPanel = new TagPanel();

    protected final JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

    protected final JPanel messageAvatarPanel = new JPanel();

    protected final int slaveImgWidth;

    protected final int slaveImgHeight;

    public RCProgressBar progressBar = new RCProgressBar();

    /**
     * 撤回 加载中 重发 等状态操作 panel
     */
    protected JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));



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

    protected void initView(){
        videoProgressBarPanel.setBackground(Colors.WINDOW_BACKGROUND);
        videoProgressBarPanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));

        videoProgressBarPanel.setOpaque(false);
        videoProgressBarPanel.add(getLayerPanel());
        videoProgressBarPanel.add(progressBar);


        statusPanel.add(revoke);

        timePanel.add(time);

        setLayout(new BorderLayout());
        add(timePanel, BorderLayout.NORTH);
        add(messageAvatarPanel, BorderLayout.CENTER);
    }


    /**
     * @return 组件
     */
    protected TagJLayeredPane getLayerPanel() {
        TagJLayeredPane layeredPane = new TagJLayeredPane();
        layeredPane.setPreferredSize(new Dimension(slaveImgWidth, slaveImgHeight));
        layeredPane.setCursor(new Cursor(Cursor.HAND_CURSOR));
        //******************缩略图
        // 设置图标水平居中
        slaveImgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // 设置图标垂直居中
        slaveImgLabel.setVerticalAlignment(SwingConstants.CENTER);
        slaveImgLabel.setBounds(0, 0, slaveImgWidth, slaveImgHeight);
        slaveImgLabel.setOpaque(false);
        layeredPane.add(slaveImgLabel, 200, 1);

        //******************播放按钮
        ImageIcon icon = IconUtil.getIcon(this, "/image/play48.png");
        int playHeight = icon.getIconHeight();
        int playWidth = icon.getIconWidth();
        int x = (slaveImgWidth - playWidth) / 2;
        int y = (slaveImgHeight - playHeight) / 2;

        playImgLabel.setBounds(x, y, playWidth, playHeight);
        playImgLabel.setOpaque(false);
        layeredPane.add(playImgLabel, 300, 0);


        //****************** 视频时长
        int margin = 3;
        int timeWidth = 30;
        int timeHeight = 10;
        timeLabel.setBounds(slaveImgWidth - timeWidth - margin, slaveImgHeight - timeHeight - margin, timeWidth, timeHeight);
        timeLabel.setOpaque(false);
        timeLabel.setFont(FontUtil.getDefaultFont(10));
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        timeLabel.setForeground(Color.white);
        layeredPane.add(timeLabel, 200, 0);


        return layeredPane;
    }
}
