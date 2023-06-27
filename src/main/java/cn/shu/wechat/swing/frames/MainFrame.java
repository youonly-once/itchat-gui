package cn.shu.wechat.swing.frames;


import cn.shu.wechat.api.WeChatTool;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.panels.RightPanel;
import cn.shu.wechat.swing.panels.left.LeftPanel;
import cn.shu.wechat.swing.utils.ClipboardUtil;
import cn.shu.wechat.swing.utils.FontUtil;
import cn.shu.wechat.swing.utils.IconUtil;
import cn.shu.wechat.swing.utils.OSUtil;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import cn.shu.wechat.utils.SleepUtils;
import lombok.Getter;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by 舒新胜 on 17-5-28.
 */
@Getter
public class MainFrame extends JFrame {
    public final static int DEFAULT_WIDTH = 900;
    public final static int DEFAULT_HEIGHT = 650;
    public final static int LEFT_PANEL_WIDTH = 300;
    public int currentWindowWidth = DEFAULT_WIDTH;
    public int currentWindowHeight = DEFAULT_HEIGHT;
    /**
     * 主窗口左面板
     */
    private LeftPanel leftPanel;

    /**
     * 是否锁屏
     */
    private boolean isLock;
    /**
     * 右面版
     */
    private RightPanel rightPanel;

    private LockFrame lockFrame;


    private static MainFrame context;

    /**
     * 正常时的任务栏图标
     */
    private Image normalTrayIcon;

    /**
     * 闪动时的任务栏图标
     */
    private Image emptyTrayIcon;

    /**
     * 任务栏图例
     */
    private TrayIcon trayIcon;

    /**
     * 任务栏图标是否闪动
     */
    private volatile boolean trayFlashing = false;

    /**
     * 任务栏图标闪烁线程
     */
    private Thread trayFlashingThread;


    public MainFrame() {
        super("微信-舒专用版");
        context = this;
        initComponents();
        initView();
        initResource();
        initTrayFlashingThread();
    }

    private void initResource() {
        ExecutorServiceUtil.getGlobalExecutorService().submit(() -> {
            try {
                initTray();
            } catch (AWTException e) {
                e.printStackTrace();
            }
            initMessageSound();
        });

    }

    /**
     * 消息到来的时候提示音
     */
    private void initMessageSound() {


    }

    /**
     * 播放消息提示间
     */
    public void playMessageSound() {

        try {

            // 创建音频输入流
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                    Objects.requireNonNull(MainFrame.class.getResourceAsStream("/wav/msg.wav")));

            // 获取音频格式
            AudioFormat audioFormat = audioInputStream.getFormat();

            // 创建数据行信息对象
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

            // 打开数据行
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceDataLine.open(audioFormat);

            // 启动数据行
            sourceDataLine.start();

            // 缓冲区大小
            int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];

            int bytesRead = 0;

            // 从音频输入流读取数据到缓冲区，并写入数据行进行播放
            while ((bytesRead = audioInputStream.read(buffer)) != -1) {
                sourceDataLine.write(buffer, 0, bytesRead);
            }

            // 停止数据行
            sourceDataLine.drain();
            sourceDataLine.close();
            audioInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 初始化系统托盘图标
     */
    private void initTray() throws AWTException {
        SystemTray systemTray = SystemTray.getSystemTray();

        if (OSUtil.getOsType() == OSUtil.Mac_OS) {
            normalTrayIcon = IconUtil.getIcon(this, "/image/ic_launcher_dark.png", 20, 20).getImage();
        } else {
            normalTrayIcon = IconUtil.getIcon(this, "/image/ic_launcher.png", 20, 20).getImage();
        }

        emptyTrayIcon = IconUtil.getIcon(this, "/image/ic_launcher_empty.png", 20, 20).getImage();

        trayIcon = new TrayIcon(normalTrayIcon, Core.getNickName());
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) {
                    super.mouseClicked(e);
                    return;
                }
                // 显示主窗口
                if (isLock){
                   lock();
                }else{
                    setVisible(true);
                    setState(0);
                    // 任务栏图标停止闪动
                    if (trayFlashing) {
                        trayFlashing = false;
                        trayIcon.setImage(normalTrayIcon);
                    }
                }


                super.mouseClicked(e);
            }
        });

        PopupMenu menu = new PopupMenu();

        MenuItem exitItem = new MenuItem("退出");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearClipboardCache();
                System.exit(1);
                WeChatTool.webWXLogOut();

            }
        });

        MenuItem showItem = new MenuItem("打开微信");
        showItem.addActionListener(
                e -> {
                    if (isLock)lock();
                    else unLock();
                });

        MenuItem lockItem = new MenuItem("锁屏");
        lockItem.addActionListener(
                e -> lock());
        menu.add(showItem);
        menu.add(lockItem);
        menu.add(exitItem);
        trayIcon.setPopupMenu(menu);
        systemTray.add(trayIcon);

    }

    /**
     * 显示通知
     *
     * @param caption     说明文字
     * @param text        提醒消息
     * @param messageType 消息类型
     */
    private void displayMessage(String caption, String text, TrayIcon.MessageType messageType) {
        trayIcon.displayMessage(caption, text, messageType);
    }

    /**
     * 清除剪切板缓存文件
     */
    private void clearClipboardCache() {
        ClipboardUtil.clearCache();
    }


    /**
     * 初始化任务栏图标闪烁 线程
     */
    private void initTrayFlashingThread() {


        ExecutorServiceUtil.getGlobalExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                trayFlashingThread = Thread.currentThread();
                trayFlashingThread.setName("TrayFlashingThread");
                while (true) {
                    if (!trayFlashing) {
                        LockSupport.park();
                    }
                    trayIcon.setImage(emptyTrayIcon);
                    SleepUtils.sleep(500);

                    trayIcon.setImage(normalTrayIcon);
                    SleepUtils.sleep(500);
                }
            }
        });
    }

    /**
     * 设置任务栏图标闪动
     */
    public synchronized void setTrayFlashing(boolean flashing) {
        trayFlashing = flashing;
        if (flashing) {
            LockSupport.unpark(trayFlashingThread);
        }

    }

    public boolean isTrayFlashing() {
        return trayFlashing;
    }


    public static MainFrame getContext() {
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


        leftPanel = new LeftPanel();
        leftPanel.setPreferredSize(new Dimension(LEFT_PANEL_WIDTH, currentWindowHeight));
        rightPanel = new RightPanel();
    }

    private void initView() {
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setMinimumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));


        if (OSUtil.getOsType() != OSUtil.Mac_OS) {
            // 隐藏标题栏
            setUndecorated(true);

            String windows = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
            try {
                UIManager.setLookAndFeel(windows);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        setListeners();


        add(leftPanel, BorderLayout.WEST);
        //add(rightPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }


    private void setListeners() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                currentWindowWidth = (int) e.getComponent().getBounds().getWidth();
                currentWindowHeight = (int) e.getComponent().getBounds().getHeight();
            }
        });
    }

    @Override
    public void dispose() {

        // 移除托盘图标
        SystemTray.getSystemTray().remove(trayIcon);
        super.dispose();
        WeChatTool.webWXLogOut();
    }


    public void lock(){
        this.isLock = true;

        if (lockFrame == null){
            lockFrame = new LockFrame();
        }
        lockFrame.setVisible(true);

        this.setVisible(false);
    }

    public void unLock(){
        this.isLock = false;

        this.setVisible(true);
        if (lockFrame != null){
            lockFrame.dispose();
            lockFrame =null;
        }
    }
}

