package cn.shu.wechat.swing.frames;


import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.HintTextFieldUI;
import cn.shu.wechat.swing.panels.TitlePanel;
import cn.shu.wechat.swing.utils.FontUtil;
import cn.shu.wechat.swing.utils.IconUtil;
import cn.shu.wechat.swing.utils.OSUtil;
import lombok.Getter;
import org.jfree.chart.title.Title;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

/**
 * Created by 舒新胜 on 17-5-28.
 */
@Getter
public class LockFrame extends JFrame {
    public final static int DEFAULT_WIDTH = 900;
    public final static int DEFAULT_HEIGHT = 650;
    public final static int LEFT_PANEL_WIDTH = 300;
    public int currentWindowWidth = DEFAULT_WIDTH;
    public int currentWindowHeight = DEFAULT_HEIGHT;

    private static LockFrame context;


    private JPasswordField textField;
    private JButton button;
    private JPanel contentPanel;

    public LockFrame() {
        super("微信-舒专用版");
        context = this;
        initComponents();
        initView();
    }


    public static LockFrame getContext() {
        return context;
    }


    private void initComponents() {
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        // 任务栏图标
        if (OSUtil.getOsType() != OSUtil.Mac_OS) {
            setIconImage(IconUtil.getIcon(this, "/image/ic_launcher.png").getImage());
        }

        UIManager.put("Label.font", FontUtil.getDefaultFont());
        UIManager.put("Panel.font", FontUtil.getDefaultFont());
        UIManager.put("TextArea.font", FontUtil.getDefaultFont());

        UIManager.put("Panel.background", Colors.WINDOW_BACKGROUND);
        UIManager.put("CheckBox.background", Colors.WINDOW_BACKGROUND);

        // 创建输入框和按钮
        textField = new JPasswordField();
        button = new JButton("解锁");
        contentPanel = new JPanel();
    }

    private void initView() {
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setResizable(false);
        setMinimumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));


        if (OSUtil.getOsType() != OSUtil.Mac_OS) {
            // 隐藏标题栏
           // setUndecorated(true);

            String windows = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
            try {
                UIManager.setLookAndFeel(windows);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        textField.addActionListener(e->{
            if (Arrays.equals(textField.getPassword(), "950720".toCharArray())){
                MainFrame.getContext().unLock();
            }else{
                JOptionPane.showMessageDialog(LockFrame.getContext(),"密码错误","密码错误",JOptionPane.WARNING_MESSAGE);
            }
        });

        button.addActionListener(e -> {
            if (Arrays.equals(textField.getPassword(), "950720".toCharArray())){
                MainFrame.getContext().unLock();
            }else{
                JOptionPane.showMessageDialog(LockFrame.getContext(),"密码错误","密码错误",JOptionPane.WARNING_MESSAGE);
            }
        });
        setLayout(null);

        // 创建输入框和按钮
        textField.setBounds(300,200 , 300, 50);
        button.setBounds(350, 260, 200, 60);

        Font font = new Font(textField.getFont().getName(), Font.PLAIN, 20);
        textField.setFont(font);
        textField.setUI(new HintTextFieldUI("请输入密码"));
        button.setFont(font);
        button.setForeground(Colors.MAIN_COLOR_DARKER);
        // 将输入框和按钮添加到JFrame
        add(textField);
        add(button);
        setLocationRelativeTo(null);
    }


}

