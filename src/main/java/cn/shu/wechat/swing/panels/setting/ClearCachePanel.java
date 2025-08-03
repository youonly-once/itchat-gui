package cn.shu.wechat.swing.panels.setting;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCButton;
import cn.shu.wechat.swing.utils.AvatarUtil;
import cn.shu.wechat.swing.utils.ClipboardUtil;
import cn.shu.wechat.swing.utils.FileUtil;
import cn.shu.wechat.swing.utils.IconUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * Created by 舒新胜 on 10/07/2017.
 */
public class ClearCachePanel extends JPanel {
    private JLabel infoLabel;
    private RCButton clearButton;

    public ClearCachePanel() {
        initComponents();
        initView();
        setListeners();
    }

    private void setListeners() {
        clearButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (clearButton.isEnabled()) {
                    clearButton.setText("清除中...");
                    clearButton.setIcon(IconUtil.getIcon(this, "/image/loading_small.gif"));
                    clearButton.setEnabled(false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                deleteAllFiles(ClipboardUtil.CLIPBOARD_TEMP_DIR);
                                IconUtil.destroyMapCache();
                                AvatarUtil.invalidateAvatarCache();
                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(null, "(头像、Icon、剪贴板)缓存清理完成！", "提示", JOptionPane.INFORMATION_MESSAGE);
                                    infoLabel.setText("当前缓存占用磁盘空间：0 字节");
                                });

                            } catch (Exception e) {
                                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, e.getMessage(), "提示", JOptionPane.INFORMATION_MESSAGE));
                            }
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    clearButton.setText("清除.");
                                    clearButton.setEnabled(true);
                                    clearButton.setIcon(IconUtil.getIcon(this, "/image/check.png"));
                                }
                            });

                        }
                    }).start();
                }

                super.mouseClicked(e);
            }
        });
    }

    private void initComponents() {
        infoLabel = new JLabel("当前缓存占用磁盘空间：计算中...");
        clearButton = new RCButton("清除缓存", Colors.MAIN_COLOR, Colors.MAIN_COLOR_DARKER, Colors.MAIN_COLOR_DARKER);
        clearButton.setPreferredSize(new Dimension(150, 35));
        ToolTipManager.sharedInstance().setDismissDelay(10000);
        clearButton.setToolTipText("清除已缓存的聊天图片及文件，清除后下次使用相应的图片或文件时会重新从服务器获取。");

        calculateCacheSize();
    }

    private void calculateCacheSize() {
        infoLabel.setText( "当前剪贴板占用磁盘空间："+FileUtil.fileSizeString(FileUtil.getDirectorySize(new File(ClipboardUtil.CLIPBOARD_TEMP_DIR))));
    }



    private void deleteAllFiles(String fileCachePath) {
        File file = new File(fileCachePath);

        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteAllFiles(f.getAbsolutePath());
                    } else {
                        f.delete();
                    }
                }
            }
        }
    }

    private void initView() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(300, 150));
        panel.add(infoLabel, BorderLayout.NORTH);
        panel.add(clearButton, BorderLayout.CENTER);

        this.setLayout(new GridBagLayout());
        add(panel, new GBC(0, 0).setAnchor(GBC.NORTH).setFill(GBC.HORIZONTAL).setInsets(-1000, 0, 0, 0));
    }


}
