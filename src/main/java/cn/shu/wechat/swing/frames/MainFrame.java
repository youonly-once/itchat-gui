package cn.shu.wechat.swing.frames;


import cn.shu.wechat.api.WeChatTool;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.media.SoundPlayer;
import cn.shu.wechat.swing.panels.RightPanel;
import cn.shu.wechat.swing.panels.left.LeftPanel;
import cn.shu.wechat.utils.*;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by 舒新胜 on 17-5-28.
 */
@Log4j2
@Getter
public class MainFrame extends JFrame {
    public final static int DEFAULT_WIDTH = 900;
    public final static int DEFAULT_HEIGHT = 650;
    public final static int LEFT_PANEL_WIDTH = 300;
    public int currentWindowWidth = DEFAULT_WIDTH;
    public int currentWindowHeight = DEFAULT_HEIGHT;
    private static final long NOTIFY_INTERVAL_MS = 10_000; // 提示最小间隔：10秒
    private static volatile long lastNotifyTime = 0;
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


    @Getter
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
                log.error(e.getMessage(), e);
            }
        });

    }


    /**
     * 初始化系统托盘图标
     */
    private void initTray() throws AWTException {
        SystemTray systemTray = SystemTray.getSystemTray();

        if (OSUtil.getOsType() == OSUtil.Mac_OS) {
            normalTrayIcon = IconUtil.getBufferedImage(this, "/image/ic_launcher_dark.png", 40, 40);
        } else {
            normalTrayIcon = IconUtil.getBufferedImage(this, "/image/ic_launcher.png", 40, 40);
        }

        emptyTrayIcon = IconUtil.getBufferedImage(this, "/image/ic_launcher_empty.png", 40, 40);

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

        JPopupMenu menu = new JPopupMenu();

        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearClipboardCache();
                WeChatTool.webWXLogOut();
                System.exit(1);
            }
        });

        JMenuItem showItem = new JMenuItem("打开微信");
        showItem.addActionListener(
                e -> {
                    if (isLock)lock();
                    else unLock();
                });

        JMenuItem lockItem = new JMenuItem("锁屏");
        lockItem.addActionListener(
                e -> lock());
        menu.add(showItem);
        menu.add(lockItem);
        menu.add(exitItem);
        // 监听托盘图标鼠标事件
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) { // 右键弹出菜单
                    menu.setLocation(e.getX(), e.getY()-menu.getHeight()-10);
                    menu.setInvoker(MainFrame.this);
                    menu.setVisible(true);
                }
            }
        });
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
        ExecutorServiceUtil.getGlobalExecutorService().submit(() -> {
            trayFlashingThread = Thread.currentThread();
            trayFlashingThread.setName("TrayFlashingThread");
            while (true) {
                try {
                    if (!trayFlashing) {
                        LockSupport.park();
                        // 线程被唤醒后继续循环
                        continue;
                    }
                    // 在 EDT 安全更新托盘图标为“空”
                    SwingUtilities.invokeLater(() -> trayIcon.setImage(emptyTrayIcon));
                    SleepUtils.sleep(500);

                    // 在 EDT 安全更新托盘图标为“正常”
                    SwingUtilities.invokeLater(() -> trayIcon.setImage(normalTrayIcon));
                    SleepUtils.sleep(500);
                }catch (Exception e){
                    log.error(e.getMessage(),e);
                }
            }
        });
    }

    /**
     * 设置任务栏图标闪动
     */
    public void setTrayFlashing(boolean flashing) {
        if (flashing) {
            SoundPlayer.playMessageSound();
        }
        SwingUtilities.invokeLater(() -> {
            trayFlashing = flashing;

            if (trayFlashing) {
                //唤醒线程 闪烁
                if (trayFlashingThread != null) {
                    LockSupport.unpark(trayFlashingThread);
                }


                if (SystemTray.isSupported()) {
                    long now = System.currentTimeMillis();
                    if (now - lastNotifyTime >= NOTIFY_INTERVAL_MS) {
                        trayIcon.displayMessage("新消息", "您有一条新消息，请查收", TrayIcon.MessageType.INFO);
                        lastNotifyTime = now;
                    }
                }


            }
        });
    }


    private void initComponents() {
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        // 任务栏图标
        if (OSUtil.getOsType() != OSUtil.Mac_OS) {
            setIconImage(IconUtil.getBufferedImage(this, "/image/ic_launcher.png", 40, 40));
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
                log.error(e.getMessage(),e);
            }
        }

        setListeners();


        add(leftPanel, BorderLayout.WEST);
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

