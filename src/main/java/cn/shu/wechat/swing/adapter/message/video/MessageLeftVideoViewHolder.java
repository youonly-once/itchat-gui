package cn.shu.wechat.swing.adapter.message.video;

import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.SizeAutoAdjustTextArea;
import cn.shu.wechat.swing.components.VerticalFlowLayout;
import cn.shu.wechat.swing.components.message.RCLeftVideoMessageBubble;
import cn.shu.wechat.swing.components.message.TagJLayeredPane;
import cn.shu.wechat.swing.components.message.TagPanel;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.utils.FontUtil;
import cn.shu.wechat.swing.utils.IconUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

/**
 * Created by 舒新胜 on 17-6-2.
 */

public class MessageLeftVideoViewHolder extends BaseMessageViewHolder {


    /**
     * 发送者
     */
    public final SizeAutoAdjustTextArea sender = new SizeAutoAdjustTextArea((int)(MainFrame.getContext().currentWindowWidth * 0.5));

    /**
     * 视频层
     */
    public TagJLayeredPane videoComponent = null;
    private final TagPanel contentTagPanel = new TagPanel();

    public final RCLeftVideoMessageBubble imageBubble = new RCLeftVideoMessageBubble();
    private final JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    private final JPanel messageAvatarPanel = new JPanel();
    private boolean isGroup = true;
    private final int slaveImgWidth;
    private final int slaveImgHeight;
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

    /**
     *
     *  @param isGroup 是否为群消息
     * @param dimension 缩略图尺寸
     */
    public MessageLeftVideoViewHolder(boolean isGroup,Dimension dimension) {
        this.isGroup = isGroup;
        this.slaveImgHeight = dimension.height;
        this.slaveImgWidth = dimension.width;
        initComponents();
        initView();
    }

    private void initComponents() {
        timePanel.setBackground(Colors.WINDOW_BACKGROUND);
        messageAvatarPanel.setBackground(Colors.WINDOW_BACKGROUND);


        // imageBubble.add(image);

        time.setForeground(Colors.FONT_GRAY);
        time.setFont(FontUtil.getDefaultFont(12));

        sender.setFont(FontUtil.getDefaultFont(12));
        sender.setForeground(Colors.FONT_GRAY);
        playImgLabel.setIcon(IconUtil.getIcon(this, "/image/image_loading.gif"));

    }

    private void initView() {
        setLayout(new BorderLayout());
        timePanel.add(time);

        contentTagPanel.setBackground(Colors.WINDOW_BACKGROUND);
        contentTagPanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));
        if (isGroup) {
            sender.setBorder(new EmptyBorder(0,0,5,0));
            contentTagPanel.add(sender);
        }
        try {
            videoComponent = getLayerPanel();
            videoComponent.setCursor(new Cursor(Cursor.HAND_CURSOR));
            JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
            controlPanel.add(videoComponent);
            controlPanel.add(revoke);
            contentTagPanel.add(controlPanel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        messageAvatarPanel.setLayout(new GridBagLayout());
        messageAvatarPanel.add(avatar, new GBC(1, 0).setWeight(1, 1).setAnchor(GBC.NORTH).setInsets(0, 5, 0, 0));
        messageAvatarPanel.add(contentTagPanel, new GBC(2, 0)
                .setWeight(1000, 1)
                .setAnchor(GBC.WEST)
                .setInsets(0, 5, 0, 0));
        add(timePanel, BorderLayout.NORTH);
        add(messageAvatarPanel, BorderLayout.CENTER);
    }


    /**
     *
     * @return 组件
     * @throws IOException 读取文件异常
     */
    private TagJLayeredPane getLayerPanel() throws IOException {
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

        //视频时长
        timeLabel.setForeground(Color.white);
        timeLabel.setBounds(slaveImgWidth - 40, slaveImgHeight - 20, 40, 20);
        timeLabel.setOpaque(false);
        layeredPane.add(timeLabel,200,0);

        layeredPane.setPreferredSize(new Dimension(slaveImgWidth, slaveImgHeight));
        return layeredPane;
    }
}
