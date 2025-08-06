package cn.shu.wechat.swing.adapter.message.video;

import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.components.*;
import cn.shu.wechat.swing.components.message.RCRightVideoMessageBubble;
import cn.shu.wechat.swing.components.message.TagJLayeredPane;
import cn.shu.wechat.swing.components.message.TagPanel;
import cn.shu.wechat.utils.FontUtil;
import cn.shu.wechat.utils.IconUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

/**
 * Created by 舒新胜 on 17-6-2.
 */

public class MessageRightVideoViewHolder extends BaseMessageViewHolder {
    public static int maxHeight = 120;
    public static int maxWidth = 80;
    public RCProgressBar progressBar = new RCProgressBar(); // 进度条
    /**
     * 视频层
     */
    public TagJLayeredPane videoComponent = null;
    public JLabel resend = new JLabel();
    public JLabel sendingProgress = new JLabel();
    private final TagPanel contentTagPanel = new TagPanel();
    public final RCRightVideoMessageBubble imageBubble = new RCRightVideoMessageBubble();
    private final JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    private final JPanel messageAvatarPanel = new JPanel();
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
     * @param dimension 缩略图尺寸
     */
    public MessageRightVideoViewHolder(Dimension dimension) {
        this.slaveImgHeight = dimension.height;
        this.slaveImgWidth = dimension.width;
        initComponents();
        initView();
    }

    private void initComponents() {
        timePanel.setBackground(Colors.WINDOW_BACKGROUND);
        messageAvatarPanel.setBackground(Colors.WINDOW_BACKGROUND);


        time.setForeground(Colors.FONT_GRAY);
        time.setFont(FontUtil.getDefaultFont(12));

        ImageIcon resendIcon = IconUtil.getIcon(this,"/image/resend.png",20,20);
        resend.setIcon(resendIcon);
        resend.setVisible(false);
        resend.setToolTipText("图片发送失败，点击重新发送");
        resend.setCursor(new Cursor(Cursor.HAND_CURSOR));

       // playImgLabel.setIcon(IconUtil.getIcon(this, "/image/image_loading.gif"));
        //ImageIcon sendingIcon = IconUtil.getIcon(this,"/image/sending.gif");
        //sendingProgress.setIcon(sendingIcon);
        //sendingProgress.setVisible(false);

        progressBar.setMaximum(100);
        progressBar.setMinimum(0);
        progressBar.setValue(0);
        progressBar.setUI(new GradientProgressBarUI());
        progressBar.setVisible(true);
    }

    private void initView() {
        setLayout(new BorderLayout());
        timePanel.add(time);

        contentTagPanel.setBackground(Colors.WINDOW_BACKGROUND);
        contentTagPanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));
        try {
            videoComponent = getLayerPanel();
            videoComponent.setCursor(new Cursor(Cursor.HAND_CURSOR));
            JPanel controlPanel = new JPanel(new BorderLayout(0, 0));
            controlPanel.add(resend, BorderLayout.WEST);
            controlPanel.add(sendingProgress, BorderLayout.CENTER);
            controlPanel.add(revoke, BorderLayout.EAST);
            JPanel resendImagePanel = new JPanel(new BorderLayout());
            resendImagePanel.setOpaque(false);

            JPanel processBarPanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, true));
            processBarPanel.setOpaque(false);
            //processBarPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
            processBarPanel.add(videoComponent);
            processBarPanel.add(progressBar);

            resendImagePanel.add(processBarPanel, BorderLayout.CENTER);
            resendImagePanel.add(controlPanel, BorderLayout.WEST);
            contentTagPanel.add(resendImagePanel);
        } catch (IOException e) {
            e.printStackTrace();
        }





        messageAvatarPanel.setLayout(new GridBagLayout());
        messageAvatarPanel.add(avatar, new GBC(2, 0).setWeight(1, 1).setAnchor(GBC.NORTH).setInsets(0, 5, 0, 5));
        messageAvatarPanel.add(contentTagPanel, new GBC(1, 0)
                .setWeight(1000, 1)
                .setAnchor(GBC.EAST)
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

        layeredPane.setPreferredSize(new Dimension(slaveImgWidth, slaveImgHeight));

        //视频时长
        timeLabel.setForeground(Color.white);
        int margin = 3;
        int timeWidth= 30;
        int timeHeight= 10;
        timeLabel.setBounds(slaveImgWidth-timeWidth-margin, slaveImgHeight-timeHeight-margin, timeWidth, timeHeight);
        timeLabel.setOpaque(false);
        timeLabel.setFont(FontUtil.getDefaultFont(10));
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        layeredPane.add(timeLabel,200,0);


        return layeredPane;
    }
}
