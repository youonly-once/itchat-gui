package cn.shu.wechat.swing.panels.setting;

import cn.shu.wechat.configuration.WechatConfiguration;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.RCButton;
import cn.shu.wechat.swing.components.VerticalFlowLayout;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.utils.AvatarUtil;
import cn.shu.wechat.swing.utils.FontUtil;
import cn.shu.wechat.swing.utils.IconUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 修改头像面板
 * <p>
 *
 * @author 舒新胜
 * @date 13/12/2021
 */
public class PrivacySettingPanel extends JPanel {
    private static PrivacySettingPanel context;
    private JCheckBox isFuzzUpAvatar;
    private RCButton okButton;
    private JPanel contentPanel;
    private JLabel statusLabel;

    public PrivacySettingPanel() {
        context = this;

        initComponents();
        initView();
        setListener();
        isFuzzUpAvatar.requestFocus();
    }


    private void initComponents() {
        isFuzzUpAvatar = new JCheckBox("临时全局模糊头像");
        isFuzzUpAvatar.setSelected(WechatConfiguration.getInstance().getFuzzUpAvatar());
        isFuzzUpAvatar.setPreferredSize(new Dimension(200, 35));
        isFuzzUpAvatar.setFont(FontUtil.getDefaultFont(14));
        isFuzzUpAvatar.setForeground(Colors.FONT_BLACK);
        isFuzzUpAvatar.setMargin(new Insets(0, 15, 0, 0));
        
        okButton = new RCButton("确认修改", Colors.MAIN_COLOR, Colors.MAIN_COLOR_DARKER, Colors.MAIN_COLOR_DARKER);
        okButton.setPreferredSize(new Dimension(100, 35));

        statusLabel = new JLabel();
        statusLabel.setForeground(Colors.FONT_GRAY_DARKER);
        //statusLabel.setVisible(false);

        contentPanel = new JPanel();
    }

    private void initView() {
        contentPanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 10, true, false));
        contentPanel.add(isFuzzUpAvatar);
        contentPanel.add(okButton);
        contentPanel.add(statusLabel);


        add(contentPanel);
    }

    private void setListener() {

        okButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fuzzUpAvatars();

                super.mouseClicked(e);
            }
        });

    }

    private void fuzzUpAvatars() {
        if (okButton.isEnabled()) {
            WechatConfiguration.getInstance().setFuzzUpAvatar(isFuzzUpAvatar.isSelected());
            AvatarUtil.invalidateAvatarCache();
            JOptionPane.showMessageDialog(MainFrame.getContext(), "修改成功", "修改前缀后缀", JOptionPane.INFORMATION_MESSAGE);
        }
    }



    public static PrivacySettingPanel getContext() {
        return context;
    }
}
