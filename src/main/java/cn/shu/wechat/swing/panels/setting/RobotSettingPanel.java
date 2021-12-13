package cn.shu.wechat.swing.panels.setting;

import cn.shu.wechat.configuration.WechatConfiguration;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.RCButton;
import cn.shu.wechat.swing.components.RCPasswordField;
import cn.shu.wechat.swing.components.VerticalFlowLayout;
import cn.shu.wechat.swing.frames.MainFrame;
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
 * Created by 舒新胜 on 13/12/2021.
 */
public class RobotSettingPanel extends JPanel {
    private static RobotSettingPanel context;
    private JTextField textField;
    private JTextField textFieldConfirm;
    private RCButton okButton;
    private JPanel contentPanel;
    private JLabel statusLabel;

    public RobotSettingPanel() {
        context = this;

        initComponents();
        initView();
        setListener();
        textField.requestFocus();
    }


    private void initComponents() {
        textField = new JTextField();
        textField.setText(WechatConfiguration.getInstance().getAutoChatPrefix());
        textField.setPreferredSize(new Dimension(200, 35));
        textField.setFont(FontUtil.getDefaultFont(14));
        textField.setForeground(Colors.FONT_BLACK);
        textField.setMargin(new Insets(0, 15, 0, 0));

        textFieldConfirm = new JTextField();
        textFieldConfirm.setPreferredSize(new Dimension(200, 35));
        textFieldConfirm.setFont(FontUtil.getDefaultFont(14));
        textFieldConfirm.setForeground(Colors.FONT_BLACK);
        textFieldConfirm.setMargin(new Insets(0, 15, 0, 0));
        textFieldConfirm.setText(WechatConfiguration.getInstance().getAutoChatSuffix());
        okButton = new RCButton("确认修改", Colors.MAIN_COLOR, Colors.MAIN_COLOR_DARKER, Colors.MAIN_COLOR_DARKER);
        okButton.setPreferredSize(new Dimension(100, 35));

        statusLabel = new JLabel();
        statusLabel.setForeground(Colors.FONT_GRAY_DARKER);
        //statusLabel.setVisible(false);

        contentPanel = new JPanel();
    }

    private void initView() {
        contentPanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 10, true, false));
        contentPanel.add(textField);
        contentPanel.add(textFieldConfirm);
        contentPanel.add(okButton);
        contentPanel.add(statusLabel);


        add(contentPanel);
    }

    private void setListener() {

        okButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                doResetPassword();

                super.mouseClicked(e);
            }
        });

        KeyListener keyListener = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doResetPassword();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        };
        textField.addKeyListener(keyListener);
        textFieldConfirm.addKeyListener(keyListener);
    }

    private void doResetPassword() {
        if (okButton.isEnabled()) {
            String password = textField.getText();
            String passwordConfirm = textFieldConfirm.getText();

            WechatConfiguration.getInstance().setAutoChatPrefix(password);
            WechatConfiguration.getInstance().setAutoChatSuffix(passwordConfirm);

           /* statusLabel.setVisible(false);
            okButton.setEnabled(false);
            okButton.setIcon(IconUtil.getIcon(this, "/image/sending.gif"));
            okButton.setText("修改中...");*/
            JOptionPane.showMessageDialog(MainFrame.getContext(), "修改前缀后缀", "修改前缀后缀", JOptionPane.INFORMATION_MESSAGE);

        }
    }


    public void restoreOKButton() {
        okButton.setText("确认修改");
        okButton.setIcon(null);
        okButton.setEnabled(true);
    }

    public void showSuccessMessage() {
        statusLabel.setText("密码修改成功，请重新登录");
        statusLabel.setIcon(IconUtil.getIcon(this, "/image/check.png"));
        statusLabel.setVisible(true);
    }

    public void showErrorMessage(String message) {
        statusLabel.setText(message);
        statusLabel.setIcon(new ImageIcon(IconUtil.getIcon(this, "/image/fail.png").getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH)));
        statusLabel.setVisible(true);
    }

    public static RobotSettingPanel getContext() {
        return context;
    }
}
