package cn.shu.wechat.swing.frames;

import cn.shu.wechat.api.DownloadTools;
import cn.shu.wechat.api.WeChatTool;
import cn.shu.wechat.beans.pojo.Contacts;
import cn.shu.wechat.controller.LoginController;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.service.ILoginService;
import cn.shu.wechat.service.impl.LoginServiceImpl;
import cn.shu.wechat.swing.app.Launcher;
import cn.shu.wechat.swing.components.*;
import cn.shu.wechat.swing.db.model.CurrentUser;
import cn.shu.wechat.swing.db.service.CurrentUserService;
import cn.shu.wechat.swing.listener.AbstractMouseListener;
import cn.shu.wechat.swing.utils.*;
import cn.shu.wechat.timedtask.TimedTask;
import cn.shu.wechat.utils.*;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.SqlSession;
import org.json.JSONObject;
import cn.shu.wechat.swing.tasks.HttpPostTask;
import cn.shu.wechat.swing.tasks.HttpResponseListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by song on 08/06/2017.
 */
@Log4j2
@Component
public class LoginFrame extends JFrame
{
    /**
     * 登陆服务实现类
     */
    @Resource
    private ILoginService loginService ;

    /**
     * 登录重试次数
     */
    private int loginRetryCount = 10;
    private static final int windowWidth = 300;
    private static final int windowHeight = 400;

    private JPanel controlPanel;
    private JLabel closeLabel;
    private JPanel editPanel;
    private JPanel codePanel;
    private JLabel statusLabel;

    private static Point origin = new Point();

    private SqlSession sqlSession;
    private CurrentUserService currentUserService ;
    private String username;


    public LoginFrame()
    {
        super("微信-舒专用版");
        initService();
        initComponents();
        initView();
        centerScreen();
        setListeners();
    }



    private void initService()
    {
        sqlSession = DbUtils.getSqlSession();
        currentUserService = new CurrentUserService(sqlSession);
    }


    private void initComponents()
    {
        Dimension windowSize = new Dimension(windowWidth, windowHeight);
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

    private void initView()
    {
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new LineBorder(Colors.LIGHT_GRAY));
        contentPanel.setLayout(new GridBagLayout());

        controlPanel.add(closeLabel);
        JPanel titleJPanel = new JPanel();
        JLabel titleJLabel = new JLabel("微信-舒专用版");
        titleJPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        titleJPanel.add(titleJLabel);
        if (OSUtil.getOsType() != OSUtil.Mac_OS)
        {
            setUndecorated(true);
            contentPanel.add(titleJPanel, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 1).setInsets(5, 0, 0, 0));
            contentPanel.add(controlPanel, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 1).setInsets(5, 0, 0, 0));
        }


        editPanel = new JPanel();
        codePanel = new JPanel();
        editPanel.add(codePanel);
        editPanel.add(statusLabel);


        add(contentPanel);
        contentPanel.add(editPanel, new GBC(0, 2).setFill(GBC.BOTH).setWeight(1, 10).setInsets(10, 10, 0, 10));
    }

    /**
     * 使窗口在屏幕中央显示
     */
    private void centerScreen()
    {
        Toolkit tk = Toolkit.getDefaultToolkit();
        this.setLocation((tk.getScreenSize().width - windowWidth) / 2,
                (tk.getScreenSize().height - windowHeight) / 2);
    }

    private void setListeners()
    {
        closeLabel.addMouseListener(new AbstractMouseListener()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                System.exit(1);
                super.mouseClicked(e);
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                closeLabel.setBackground(Colors.LIGHT_GRAY);
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                closeLabel.setBackground(Colors.WINDOW_BACKGROUND);
                super.mouseExited(e);
            }
        });

        if (OSUtil.getOsType() != OSUtil.Mac_OS)
        {
            addMouseListener(new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)
                {
                    // 当鼠标按下的时候获得窗口当前的位置
                    origin.x = e.getX();
                    origin.y = e.getY();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter()
            {
                @Override
                public void mouseDragged(MouseEvent e)
                {
                    // 当鼠标拖动时获取窗口当前位置
                    Point p = LoginFrame.this.getLocation();
                    // 设置窗口的位置
                    LoginFrame.this.setLocation(p.x + e.getX() - origin.x, p.y + e.getY()
                            - origin.y);
                }
            });
        }


    }

    private void doLogin()
    {
        this.dispose();

        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);


    }


    private void showMessage(String message)
    {
        if (!statusLabel.isVisible())
        {
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
        // 防止SSL错误
        System.setProperty("jsse.enableSNIExtension", "false");
        String qrPath = Config.QR_PATH;
        boolean mkdirs = new File(qrPath).getParentFile().mkdirs();
        // 登陆
        while (true) {
            Process process = null;
            for (int count = 0; count < loginRetryCount; count++) {
                log.info("获取UUID");
                while (true) {
                    log.info("1. 获取微信UUID");
                    String uuid = loginService.getUuid();
                    if (uuid != null) {
                        break;
                    }
                    log.warn("1.1. 获取微信UUID失败，两秒后重新获取");
                    SleepUtils.sleep(2000);
                }

                log.info("2. 获取登陆二维码图片");

                if (loginService.getQR(qrPath)) {
                    try {
                        JLabel label = new JLabel();
                        try {
                            label.setIcon(new ImageIcon(ImageIO.read(new File(qrPath)).getScaledInstance(250,250,Image.SCALE_SMOOTH)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        codePanel.add(label);
                        this.repaint();
                        this.revalidate();
                        // 使用图片查看器打开登陆二维码图片
                       // process = CommonTools.printQr(qrPath);
                    } catch (Exception e) {
                        log.info(e.getMessage());
                        log.info("请手动打开二维码图片进行扫码登录：" + qrPath);
                    }
                    break;
                } else if (count == loginRetryCount-1) {
                    log.error("2.2. 获取登陆二维码图片失败，系统退出");
                    System.exit(0);
                }
            }
            statusLabel.setText("请使用微信扫一扫以登录");
            log.info("3. 请扫描二维码图片，并在手机上确认");
            if (!Core.isAlive()) {
                try {
                    loginService.login();
                } catch (Exception e) {
                    log.error(e.getMessage());
                    return;
                }
                //TODO 登录成功，关闭打开的二维码图片，暂时没有成功
                CommonTools.closeQr(process);
                Core.setAlive(true);
                log.info(("4、登陆成功"));
                break;
            }
            log.info("4. 登陆超时，请重新扫描二维码图片");
        }
        statusLabel.setText("5、登陆成功，微信初始化");
        log.info("5. 登陆成功，微信初始化");
        if (!loginService.webWxInit()) {
            log.info("6. 微信初始化异常");
            System.exit(0);
        }
        statusLabel.setText("6. 开启微信状态通知");
        log.info("6. 开启微信状态通知");
        loginService.wxStatusNotify();

        statusLabel.setText("7. 清除。。。。");
        log.info("7. 清除。。。。");
        CommonTools.clearScreen();
        log.info(String.format("欢迎回来， %s", Core.getNickName()));

        statusLabel.setText("8. 获取联系人信息");
        log.info("8. 获取联系人信息");
        loginService.webWxGetContact();

        statusLabel.setText("9. 获取群好友及群好友列表");
        log.info("9. 获取群好友及群好友列表");
        loginService.WebWxBatchGetContact();



        statusLabel.setText("10. 缓存本次登陆好友相关消息");
        log.info("10. 缓存本次登陆好友相关消息");
        // 登陆成功后缓存本次登陆好友相关消息（NickName, UserName）
        //WeChatTool.setUserInfo();
        //删除无效头像
        // HeadImageUtil.deleteLoseEfficacyHeadImg(Config.PIC_DIR + "/headimg/");
        if (dHImg) {
            statusLabel.setText("11. 下载联系人头像");
            log.info("11. 下载联系人头像");
            for (Map.Entry<String, Contacts> entry : Core.getMemberMap().entrySet()) {
                ExecutorServiceUtil.getHeadImageDownloadExecutorService().execute(
                        () -> {
                            Core.getContactHeadImgPath().put(entry.getValue().getUsername(), DownloadTools.downloadHeadImg(entry.getValue().getHeadimgurl(), entry.getValue().getUsername()));
                            log.info("下载头像：({}):{}", entry.getValue().getNickname(), entry.getValue().getHeadimgurl());
                        });

            }
        }


        ExecutorServiceUtil.getHeadImageDownloadExecutorService().shutdown();
        ExecutorServiceUtil.getGlobalExecutorService().execute(() -> {
            try {
                //等待头像下载完成
                boolean b = ExecutorServiceUtil.getHeadImageDownloadExecutorService().awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            statusLabel.setText("头像下载完成");
            log.info("头像下载完成");

            statusLabel.setText("12 开始接收消息");
            log.info("12. 开始接收消息");
            loginService.startReceiving();
            doLogin();
        });


    }

}
