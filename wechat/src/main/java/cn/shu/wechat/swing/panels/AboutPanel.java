package cn.shu.wechat.swing.panels;

import cn.shu.wechat.swing.app.Launcher;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.utils.FontUtil;
import cn.shu.wechat.swing.utils.IconUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Created by song on 26/06/2017.
 */
public class AboutPanel extends JPanel
{
    private JLabel imageLabel;
    private JLabel versionLabel;

    public AboutPanel()
    {
        initComponents();
        initView();
    }

    private void initComponents()
    {
        imageLabel = new JLabel();
        ImageIcon icon  = IconUtil.getIcon(this, "/image/ic_launcher.png", 100,100);
        imageLabel.setIcon(icon);

        versionLabel = new JLabel();
        versionLabel.setText("微信 v" + Launcher.APP_VERSION);
        versionLabel.setFont(FontUtil.getDefaultFont(20));
        versionLabel.setForeground(Colors.FONT_GRAY_DARKER);
    }

    private void initView()
    {
        this.setLayout(new GridBagLayout());

        JPanel avatarNamePanel = new JPanel();
        avatarNamePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));
        avatarNamePanel.add(imageLabel, BorderLayout.WEST);
        avatarNamePanel.add(versionLabel, BorderLayout.CENTER);

        add(avatarNamePanel, new GBC(0,0).setWeight(1,1).setAnchor(GBC.CENTER).setFill(GBC.BOTH).setInsets(50,0,0,0));
    }
}
