package cn.shu.wechat.swing.frames;

import cn.shu.wechat.api.DownloadTools;
import cn.shu.wechat.configuration.WechatConfiguration;
import cn.shu.wechat.constant.DownloadType;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.dto.response.wxinit.WxInitResponse;
import cn.shu.wechat.entity.LoginInfo;
import cn.shu.wechat.entity.RoomItem;
import cn.shu.wechat.exception.WebWXException;
import cn.shu.wechat.mapper.LoginInfoMapper;
import cn.shu.wechat.service.LoginService;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.listener.AbstractMouseListener;
import cn.shu.wechat.swing.panels.left.tabcontent.ContactsPanel;
import cn.shu.wechat.swing.panels.left.tabcontent.RoomsPanel;
import cn.shu.wechat.task.DownloadManager;
import cn.shu.wechat.task.DownloadTask;
import cn.shu.wechat.utils.*;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by 舒新胜 on 08/06/2017.
 */
@Log4j2
public final class LoginFrame extends JFrame {
    /**
     * 登陆服务实现类
     */
    private final LoginService loginService;

    /**
     * 微信配置类
     */
    private final WechatConfiguration wechatConfiguration;

    private final int WINDOW_WIDTH = 300;
    private final int WINDOW_HEIGHT = 450;
    private final Point origin = new Point();
    private final Color normalBg = Color.decode("#FAFAFA");
    private JLabel closeLabel;
    private JLabel codeLabel;
    private JLabel refreshCodeBt;
    private final Color hoverBg = Color.decode("#F0F0F0");
    private final Color pressedBg = Color.decode("#E0E0E0");
    private final Color textColor = Color.decode("#666666");
    private final Color borderColor = Color.decode("#D9D9D9");
    private JPanel contentPanel;
    private JLabel titleJLabel;
    private JLabel statusLabel;
    private MouseAdapter refreshCodeBtMouseAdapter;
    private AbstractMouseListener closeLabelMouseListener;
    private MouseMotionAdapter frameMouseMotionAdapter;
    private MouseAdapter frameMouseAdapter;
    private volatile boolean isRefreshCode = false;

    public LoginFrame() {
        super("微信-舒专用版");
        initComponents();
        initView();
        setLocationRelativeTo(null);
        setListeners();
//        if (OSUtil.getOsType() == OSUtil.Windows) {
//            registerHotKey();
//        }
        loginService = SpringContextHolder.getBean(LoginService.class);
        wechatConfiguration = SpringContextHolder.getBean(WechatConfiguration.class);
    }


    private void initComponents() {
        Dimension windowSize = new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
        setMinimumSize(windowSize);
        setMaximumSize(windowSize);

        contentPanel = new JPanel();
        contentPanel.setBorder(new LineBorder(Colors.LIGHT_GRAY));
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);

        titleJLabel = new JLabel(WechatConfiguration.getInstance().getLoginTitle());
        titleJLabel.setFont(FontUtil.getDefaultFont(14, Font.BOLD));
        if (OSUtil.getOsType() != OSUtil.Mac_OS) {
            setUndecorated(true);
        }

        codeLabel = new JLabel();
        codeLabel.setHorizontalAlignment(JLabel.CENTER);
        codeLabel.setIcon(IconUtil.getIcon(this, "/image/image_loading.gif"));
        codeLabel.setPreferredSize(new Dimension(250, 250));

        //重新扫描按钮
        refreshCodeBt = new JLabel("刷新");
        refreshCodeBt.setHorizontalAlignment(JLabel.CENTER);
        refreshCodeBt.setPreferredSize(new Dimension(150, 40));
        refreshCodeBt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshCodeBt.setFont(FontUtil.getDefaultFont());

        refreshCodeBt.setOpaque(true);
        refreshCodeBt.setBackground(normalBg);
        refreshCodeBt.setBorder(BorderFactory.createLineBorder(borderColor, 1, true));

        refreshCodeBt.setForeground(textColor);
        //refreshCodeBt.setFont(new Font("微软雅黑", Font.BOLD, 14));


        closeLabel = new JLabel();
        closeLabel.setIcon(IconUtil.getIcon(this, "/image/close.png"));
        closeLabel.setHorizontalAlignment(JLabel.RIGHT);
        closeLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));


        statusLabel = new JLabel();
        statusLabel.setForeground(Colors.FONT_GRAY);
        statusLabel.setText("正在加载二维码...");
        statusLabel.setVisible(true);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER); // 水平居中
        statusLabel.setVerticalAlignment(SwingConstants.CENTER);   // 垂直居中
    }

    private void initView() {


        int padding = 5;
        contentPanel.add(titleJLabel, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 10).setInsets(0, padding, 0, padding).setAnchor(GridBagConstraints.NORTH));
        contentPanel.add(closeLabel, new GBC(1, 0).setFill(GBC.BOTH).setWeight(1, 1).setInsets(0, padding, 0, padding));
        contentPanel.add(codeLabel, new GBC(0, 1).setFill(GBC.BOTH).setWeight(100, 100).setInsets(0, padding, 0, padding).setGridWidth(2));
        contentPanel.add(refreshCodeBt, new GBC(0, 2).setFill(GBC.BOTH).setWeight(10, 1).setInsets(0, 25, 30, 25).setGridWidth(2));
        contentPanel.add(statusLabel, new GBC(0, 3).setFill(GBC.BOTH).setWeight(1, 1).setInsets(0, padding, 30, padding).setGridWidth(2));

        this.add(contentPanel);
    }

    /**
     * 使窗口在屏幕中央显示
     */

    private void setListeners() {
        closeLabelMouseListener = new AbstractMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.exit(1);
                super.mouseClicked(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                closeLabel.setBackground(Colors.LIGHT_GRAY);
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeLabel.setBackground(Colors.WINDOW_BACKGROUND);
                super.mouseExited(e);
            }
        };
        closeLabel.addMouseListener(closeLabelMouseListener);

        if (OSUtil.getOsType() != OSUtil.Mac_OS) {
            frameMouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    // 当鼠标按下的时候获得窗口当前的位置
                    origin.x = e.getX();
                    origin.y = e.getY();
                }
            };
            addMouseListener(frameMouseAdapter);
            frameMouseMotionAdapter = new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    // 当鼠标拖动时获取窗口当前位置
                    Point p = LoginFrame.this.getLocation();
                    // 设置窗口的位置
                    LoginFrame.this.setLocation(p.x + e.getX() - origin.x, p.y + e.getY()
                            - origin.y);
                }
            };
            addMouseMotionListener(frameMouseMotionAdapter);
        }
        refreshCodeBtMouseAdapter = new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                refreshCodeBt.setBackground(hoverBg);
                refreshCodeBt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                refreshCodeBt.setBackground(normalBg);
                refreshCodeBt.setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                refreshCodeBt.setBackground(pressedBg);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                refreshCodeBt.setBackground(hoverBg); // 回到悬停色
                refreshCode();
                super.mouseReleased(e);
            }
        };
        refreshCodeBt.addMouseListener(refreshCodeBtMouseAdapter);


    }

    private void removeListeners() {
        if (refreshCodeBtMouseAdapter != null) {
            refreshCodeBt.removeMouseListener(refreshCodeBtMouseAdapter);
        }

        if (closeLabelMouseListener != null) {
            closeLabel.removeMouseListener(closeLabelMouseListener);
        }

        if (frameMouseMotionAdapter != null) {
            this.removeMouseMotionListener(frameMouseMotionAdapter);
        }

        if (frameMouseAdapter != null) {
            this.removeMouseListener(frameMouseAdapter);
        }
    }


    private void refreshCode() {
        if (isRefreshCode) return;
        isRefreshCode = true;
        SwingUtilities.invokeLater(() -> {
            showMessage("刷新二维码...");
        });
        ExecutorServiceUtil.getGlobalExecutorService().submit(() -> {
            try {
                getUUID();
                BufferedImage qr = loginService.getQR();
                SwingUtilities.invokeLater(() -> {
                    codeLabel.setIcon(new ImageIcon(IconUtil.getScaledImage(qr, 250, 250)));
                    showMessage("请扫描二维码以登录");
                });
            } catch (IOException | InterruptedException ex) {
                SwingUtilities.invokeLater(() -> {
                    showMessage(ex.getMessage());
                });
            } finally {
                isRefreshCode = false;
            }

        });
    }
    /**
     * 打开窗体
     */
    private void openMainFrame() {
        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        this.dispose();

    }

    /**
     * 显示消息
     */
    private void showMessage(String message) {
        if (!statusLabel.isVisible()) {
            statusLabel.setVisible(true);
        }

        statusLabel.setText(message);
        statusLabel.setToolTipText(message);
    }

    /**
     * 调用网页版微信登录
     *
     * @param dHImg 是否下载头像
     */
    public void login(boolean dHImg) {
        try { // 防止SSL错误
            System.setProperty("jsse.enableSNIExtension", "false");
            showMessage("获取UUID");
            getUUID();

            showMessage(" 获取登陆二维码图片");
            BufferedImage qr = loginService.getQR();
            codeLabel.setIcon(new ImageIcon(IconUtil.getScaledImage(qr, 250, 250)));

            showMessage("请使用微信扫一扫以登录");

            loginService.preLogin(new LoginService.LoginCallBack() {
                @Override
                public void CallBack(String msg) {
                    showMessage(msg);
                }


                @Override
                public void refreshCode() {
                    LoginFrame.this.refreshCode();
                }
                @Override
                public void avatar(String avatarBase64) {
                    if (avatarBase64 == null) {
                        return;
                    }
                    try {
                        byte[] decode = Base64.getDecoder().decode(avatarBase64);
                        ByteArrayInputStream bais = new ByteArrayInputStream(decode);
                        BufferedImage read = ImageIO.read(bais);
                        codeLabel.setIcon(new ImageIcon(read));

                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }

                }

            });
            refreshCodeBt.setVisible(false);


            showMessage("登陆成功，微信初始化...");
            //保存登录信息
            LoginInfo loginInfo = new LoginInfo();
            loginInfo.setUuid(Core.getUuid());
            loginInfo.setSkey(Core.getLoginResultData().getBaseRequest().getSKey());
            loginInfo.setWxsid(Core.getLoginResultData().getBaseRequest().getWxSid());
            loginInfo.setWxuin(Core.getLoginResultData().getBaseRequest().getWxUin());
            loginInfo.setPassTicket(Core.getLoginResultData().getPassTicket());
            loginInfo.setUin(Core.getLoginResultData().getBaseRequest().getWxUin());
            SpringContextHolder.getBean(LoginInfoMapper.class).insert(loginInfo);

            WxInitResponse wxInitResponse = loginService.webWxInit();

            wechatConfiguration.setBasePath(wechatConfiguration.getBasePath() + File.separator + MD5Util.MD5(Core.getNickName())+ File.separator);


            log.info("开启微信状态通知");
            loginService.wxStatusNotify();

            //打开窗体
            log.info("登录成功");
            openMainFrame();

            //初始化聊天列表
            Set<String> recentContacts = Core.getRecentContacts();
            SwingUtilities.invokeLater(() -> {
                List<RoomItem> roomItems = recentContacts.stream()
                        .map(userId -> new RoomItem(Core.getMemberMap().get(userId), "", 0, false))
                        .collect(Collectors.toList());
                RoomsPanel.getContext().addRoom(roomItems);
            });


            new SwingWorker<Object, Object>() {

                @Override
                protected Object doInBackground() throws Exception {
                    log.info("获取联系人信息");
                    DownloadTask<Void> objectDownloadTask = new DownloadTask<>();
                    objectDownloadTask.setTaskId("webWxGetContact");
                    objectDownloadTask.setType(DownloadType.GetContacts);
                    DownloadManager.submitAwait(objectDownloadTask);
                    return null;
                }

                @Override
                protected void done() {

                    SwingUtilities.invokeLater(() -> {
                        List<RoomItem> roomItems = Arrays.stream(wxInitResponse.getChatSet().split(","))
                                .filter(e -> !Core.getRecentContacts().contains(e))
                                .map(userId -> Core.getMemberMap().get(userId))
                                .filter(Objects::nonNull)
                                .map(e -> new RoomItem(e, "", 0, false))
                                .collect(Collectors.toList());
                        for (RoomItem roomItem : roomItems) {
                            Core.getRecentContacts().add(roomItem.getRoomId());
                        }
                        RoomsPanel.getContext().addRoom(roomItems);
                    });
                    ContactsPanel.getContext().notifyDataSetChanged();


                    ExecutorServiceUtil.getGlobalExecutorService().submit(() -> {

                        log.info("获取群好友及群好友列表");
                        DownloadTask<Void> objectDownloadTask = new DownloadTask<>();
                        objectDownloadTask.setTaskId("WebWxBatchGetContact");
                        objectDownloadTask.setType(DownloadType.GetBatchContacts);
                        DownloadManager.submitAwait(objectDownloadTask);

                        log.info(" 开始接收消息");
                        loginService.startReceiving();

                        Core.setCompare(true);
                        if (dHImg) {
                            downloadHeadImage();
                        }
                    });
                }
            }.execute();


        } catch (Exception e) {
            log.error(e.getMessage(),e);
            showMessage(e.getMessage());
        }
    }

    /**
     * 循环获取UUID
     */
    private void getUUID() {
        while (true) {
            log.info("1. 获取微信UUID");
            String uuid = null;
            try {
                uuid = loginService.getUuid();
            } catch (IOException | WebWXException | InterruptedException e) {
                log.error(e.getMessage());
            }
            if (uuid != null) {
                break;
            }
            log.warn("1.1. 获取微信UUID失败，两秒后重新获取");
            SleepUtils.sleep(2000);
        }
    }

    /**
     * 注册截图快捷键
     */
    private void registerHotKey() {

        int SCREEN_SHOT_CODE = 10001;
        try {
           JIntellitype.getInstance().registerHotKey(SCREEN_SHOT_CODE, JIntellitype.MOD_ALT, 'S');

            JIntellitype.getInstance().addHotKeyListener(new HotkeyListener() {
                @Override
                public void onHotKey(int markCode) {
                    if (markCode == SCREEN_SHOT_CODE) {
                        screenShot();
                    }
                }
            });
        } catch (Exception e) {
            log.warn("注册截屏快捷键失败：" + e.getMessage());
        }
    }

    /**
     * 截图
     */
    private void screenShot() {
        ScreenShotFrame ssw = new ScreenShotFrame();
        ssw.setVisible(true);
    }

    /**
     * 下载头像
     */
    private void downloadHeadImage() {
        ExecutorServiceUtil.getHeadImageDownloadExecutorService().execute(() -> AvatarUtil.deleteLoseEfficacyHeadImg(wechatConfiguration.getBasePath() + "/headimg/"));
        statusLabel.setText("11. 下载联系人头像");
        log.info("11. 下载联系人头像");
        long time = System.currentTimeMillis();
        Core.getMemberMap().forEach((key, value) -> {
            ExecutorServiceUtil.getHeadImageDownloadExecutorService().submit(() -> {
                try {
                    Core.getContactHeadImgPath().put(value.getUsername(), DownloadTools.downloadBigHeadImg(value.getHeadimgurl(), value.getUsername()));
                } catch (IOException | InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            });

            //log.info("下载头像：({}):{}", contacts.getValue().getNickname(), contacts.getValue().getHeadimgurl());
        });
        ExecutorServiceUtil.getHeadImageDownloadExecutorService().shutdown();
        try {
            boolean b = ExecutorServiceUtil.getHeadImageDownloadExecutorService().awaitTermination(5, TimeUnit.MINUTES);
            if (!b) {
                log.warn("线程池关闭失败！");
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        log.info("11. 下载联系人头像完成，耗时{}秒", (System.currentTimeMillis() - time) / 1000);
    }

    @Override
    public void dispose() {
        super.dispose();
        removeListeners();
    }
}
