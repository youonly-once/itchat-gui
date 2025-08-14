package cn.shu.wechat.swing.adapter.message.video;

import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.components.GradientProgressBarUI;
import cn.shu.wechat.swing.components.RCProgressBar;
import cn.shu.wechat.swing.components.message.TagJLayeredPane;
import cn.shu.wechat.utils.FontUtil;
import cn.shu.wechat.utils.IconUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Created by 舒新胜 on 17-6-2.
 */

public class MessageVideoViewHolder extends BaseMessageViewHolder {
    public static final int maxHeight = 120;
    public static final int maxWidth = 80;

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


    protected final int slaveImgWidth;

    protected final int slaveImgHeight;

    public RCProgressBar progressBar = new RCProgressBar(3){
        @Override
        public void setVisible(boolean aFlag) {
            if(aFlag){
                super.setVisible(aFlag);
            }else{
                setValue(0);
            }
        }
    };

    public TagJLayeredPane contentLayeredPane;


    /**
     * @param dimension 缩略图尺寸
     */
    public MessageVideoViewHolder(Dimension dimension) {
        this.slaveImgHeight = dimension.height;
        this.slaveImgWidth = dimension.width;

    }

    protected void initComponents() {

        progressBar.setMaximum(100);
        progressBar.setMinimum(0);
        progressBar.setValue(0);
        progressBar.setUI(new GradientProgressBarUI());
        progressBar.setVisible(true);

        progressBar.setBorder(null);
        contentLayeredPane = getLayerPanel();
    }

    protected void initView(){

        setLayout(new GridBagLayout());

    }


    /**
     * @return 组件
     */
    private TagJLayeredPane getLayerPanel() {
        contentLayeredPane = new TagJLayeredPane();
        contentLayeredPane.setPreferredSize(new Dimension(slaveImgWidth, slaveImgHeight+progressBar.getHeight()));
        contentLayeredPane.setCursor(new Cursor(Cursor.HAND_CURSOR));
        //******************缩略图
        // 设置图标水平居中
        slaveImgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // 设置图标垂直居中
        slaveImgLabel.setVerticalAlignment(SwingConstants.CENTER);
        slaveImgLabel.setBounds(0, 0, slaveImgWidth, slaveImgHeight);
        slaveImgLabel.setOpaque(false);
        contentLayeredPane.add(slaveImgLabel, 200, 1);

        //******************播放按钮
        ImageIcon icon = IconUtil.getIcon(this, "/image/play48.png");
        int playHeight = icon.getIconHeight();
        int playWidth = icon.getIconWidth();
        int x = (slaveImgWidth - playWidth) / 2;
        int y = (slaveImgHeight - playHeight) / 2;

        playImgLabel.setBounds(x, y, playWidth, playHeight);
        playImgLabel.setOpaque(false);
        contentLayeredPane.add(playImgLabel, 300, 0);


        //****************** 视频时长
        int margin = 3;
        int timeWidth = 30;
        int timeHeight = 10;
        timeLabel.setBounds(slaveImgWidth - timeWidth - margin, slaveImgHeight - timeHeight - margin, timeWidth, timeHeight);
        timeLabel.setOpaque(false);
        timeLabel.setFont(FontUtil.getDefaultFont(10));
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        timeLabel.setForeground(Color.white);
        contentLayeredPane.add(timeLabel, 200, 0);


        progressBar.setBounds(0, slaveImgHeight , slaveImgWidth, progressBar.getHeight());
        contentLayeredPane.add(progressBar, 400, 0); // 层级比图片高


        return contentLayeredPane;
    }
}
