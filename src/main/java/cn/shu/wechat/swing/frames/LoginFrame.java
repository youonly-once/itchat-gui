package cn.shu.wechat.swing.frames;

import cn.shu.wechat.api.DownloadTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.pojo.entity.Contacts;
import cn.shu.wechat.service.LoginService;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.entity.RoomItem;
import cn.shu.wechat.swing.listener.AbstractMouseListener;
import cn.shu.wechat.swing.panels.ContactsPanel;
import cn.shu.wechat.swing.panels.RoomsPanel;
import cn.shu.wechat.swing.utils.IconUtil;
import cn.shu.wechat.swing.utils.OSUtil;
import cn.shu.wechat.utils.Config;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import cn.shu.wechat.utils.HeadImageUtil;
import cn.shu.wechat.utils.SleepUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Created by 舒新胜 on 08/06/2017.
 */
@Log4j2
@Component
public class LoginFrame extends JFrame {
    /**
     * 登陆服务实现类
     */
    @Resource
    private LoginService loginService;


    private static final int WINDOW_WIDTH = 300;
    private static final int WINDOW_HEIGHT = 400;

    private JPanel controlPanel;
    private JLabel closeLabel;
    private JPanel editPanel;
    private JPanel codePanel;
    private JLabel codeLabel;
    private JLabel statusLabel;

    private static final Point origin = new Point();


    public LoginFrame() {
        super("微信-舒专用版");
        initComponents();
        initView();
        centerScreen();
        setListeners();
    }


    private void initComponents() {
        Dimension windowSize = new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
        setMinimumSize(windowSize);
        setMaximumSize(windowSize);


        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        //controlPanel.setBounds(0,5, windowWidth, 30);

        closeLabel = new JLabel();
        closeLabel.setIcon(IconUtil.getIcon(this, "/image/close.png"));
        closeLabel.setHorizontalAlignment(JLabel.CENTER);
        //closeLabel.setPreferredSize(new Dimension(30,30));
        closeLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));


        statusLabel = new JLabel();
        statusLabel.setForeground(Colors.FONT_GRAY);
        statusLabel.setText("正在加载二维码...");
        statusLabel.setVisible(true);
    }

    private void initView() {
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new LineBorder(Colors.LIGHT_GRAY));
        contentPanel.setLayout(new GridBagLayout());

        controlPanel.add(closeLabel);
        JPanel titleJPanel = new JPanel();
        JLabel titleJLabel = new JLabel("微信-舒专用版");
        titleJPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        titleJPanel.add(titleJLabel);
        if (OSUtil.getOsType() != OSUtil.Mac_OS) {
            setUndecorated(true);
            contentPanel.add(titleJPanel, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 1).setInsets(5, 0, 0, 0));
            contentPanel.add(controlPanel, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 1).setInsets(5, 0, 0, 0));
        }


        editPanel = new JPanel();
        codePanel = new JPanel();
        codeLabel = new JLabel();
        ImageIcon icon = IconUtil.getIcon(this, "/image/image_loading.gif");
        codeLabel.setHorizontalAlignment(JLabel.CENTER);
        codeLabel.setIcon(icon);
        codePanel.add(codeLabel);
        editPanel.add(codePanel);
        editPanel.add(statusLabel);


        add(contentPanel);
        contentPanel.add(editPanel, new GBC(0, 2).setFill(GBC.BOTH).setWeight(1, 10).setInsets(10, 10, 0, 10));
    }

    /**
     * 使窗口在屏幕中央显示
     */
    private void centerScreen() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        this.setLocation((tk.getScreenSize().width - WINDOW_WIDTH) / 2,
                (tk.getScreenSize().height - WINDOW_HEIGHT) / 2);
    }

    private void setListeners() {
        closeLabel.addMouseListener(new AbstractMouseListener() {
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
        });

        if (OSUtil.getOsType() != OSUtil.Mac_OS) {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    // 当鼠标按下的时候获得窗口当前的位置
                    origin.x = e.getX();
                    origin.y = e.getY();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    // 当鼠标拖动时获取窗口当前位置
                    Point p = LoginFrame.this.getLocation();
                    // 设置窗口的位置
                    LoginFrame.this.setLocation(p.x + e.getX() - origin.x, p.y + e.getY()
                            - origin.y);
                }
            });
        }


    }

    /**
     * 打开窗体
     */
    private void openFrame() {
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
            codeLabel.setIcon(new ImageIcon(qr.getScaledInstance(250, 250, Image.SCALE_SMOOTH)));

            showMessage("请使用微信扫一扫以登录");
            loginService.login();
            Core.setAlive(true);

            showMessage("登陆成功，微信初始化...");
            if (!loginService.webWxInit()) {
                showMessage(" 微信初始化异常");
                System.exit(0);
            }
            //打开窗体
            openFrame();

            //初始化聊天列表
            Set<String> recentContacts = Core.getRecentContacts();
            SwingUtilities.invokeLater(() -> {
                ArrayList<RoomItem> rooms = new ArrayList<>();
                for (String userId : recentContacts) {
                    rooms.add(new RoomItem(Core.getMemberMap().get(userId), "", 0));
                }
                RoomsPanel.getContext().addRoom(rooms);
            });


            new SwingWorker<Object, Object>() {

                @Override
                protected Object doInBackground() throws Exception {
                    log.info("获取联系人信息");
                    loginService.webWxGetContact();
                    return null;
                }

                @Override
                protected void done() {
                    log.info("开启微信状态通知");
                    loginService.wxStatusNotify();
                    log.info(" 开始接收消息");
                    loginService.startReceiving();
                    ContactsPanel.getContext().notifyDataSetChanged();

                    ExecutorServiceUtil.getGlobalExecutorService().submit(() -> {
                        log.info("9. 获取群好友及群好友列表");
                        loginService.WebWxBatchGetContact();
                        if (dHImg) {
                            downloadHeadImage();
                        }
                    });
                }
            }.execute();


        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    /**
     * 循环获取UUID
     */
    private void getUUID() {
        while (true) {
            log.info("1. 获取微信UUID");
            String uuid = loginService.getUuid();
            if (uuid != null) {
                break;
            }
            log.warn("1.1. 获取微信UUID失败，两秒后重新获取");
            SleepUtils.sleep(2000);
        }
    }

    /**
     * 下载头像
     */
    private void downloadHeadImage() {
        ExecutorServiceUtil.getHeadImageDownloadExecutorService().execute(() -> HeadImageUtil.deleteLoseEfficacyHeadImg(Config.PIC_DIR + "/headimg/"));
        statusLabel.setText("11. 下载联系人头像");
        log.info("11. 下载联系人头像");
        for (Map.Entry<String, Contacts> entry : Core.getMemberMap().entrySet()) {
            Core.getContactHeadImgPath().put(entry.getValue().getUsername(), DownloadTools.downloadBigHeadImg(entry.getValue().getHeadimgurl(), entry.getValue().getUsername()));
            log.info("下载头像：({}):{}", entry.getValue().getNickname(), entry.getValue().getHeadimgurl());
        /*    ExecutorServiceUtil.getHeadImageDownloadExecutorService().execute(

                    () -> {

                        Core.getContactHeadImgPath().put(entry.getValue().getUsername(), DownloadTools.downloadHeadImgBig(entry.getValue().getHeadimgurl(), entry.getValue().getUsername()));
                        log.info("下载头像：({}):{}", entry.getValue().getNickname(), entry.getValue().getHeadimgurl());
                    });

            ExecutorServiceUtil.getGlobalExecutorService().submit(new Runnable() {
                @Override
                public void run() {
                    AvatarUtil.putUserAvatarCache(entry.getValue().getUsername(), DownloadTools.downloadImage(entry.getValue().getHeadimgurl()));
                }
            });*/

        }
    }

}
