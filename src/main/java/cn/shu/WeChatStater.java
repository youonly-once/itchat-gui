package cn.shu;

import cn.shu.wechat.configuration.WechatConfiguration;
import cn.shu.wechat.swing.components.VerticalFlowLayout;
import cn.shu.wechat.swing.frames.LoginFrame;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.utils.FontUtil;
import cn.shu.wechat.swing.utils.IconUtil;
import cn.shu.wechat.utils.SpringContextHolder;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.CountDownLatch;

/**
 * @author ShuXinSheng
 * @version 创建时间：2019年5月16日 下午1:02:30
 * 类说明
 */
@SpringBootApplication
@MapperScan("cn.shu.wechat.mapper")
@EnableScheduling
@EnableAsync

public class WeChatStater {
    private static ConfigurableApplicationContext context;
    private static final CountDownLatch countDownLatch = new CountDownLatch(1);
    public static void main(String[] args) {
        boot();

        new SwingWorker<Object,Object>() {

            @Override
            protected Object doInBackground() throws Exception {
                context = new SpringApplicationBuilder(WeChatStater.class)
                        .headless(false)
                        .run();
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                countDownLatch.countDown();
                loginFrame.setVisible(true);
                loginFrame.login(true);
                return null;
            }
        }.execute();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动画面
     */
    private static void boot(){
        JWindow jWindow = new JWindow();
        jWindow.setLocationRelativeTo(null);
        jWindow.setSize(400,300);
        JLabel label = new JLabel();
        label.setIcon(IconUtil.getIcon(jWindow,"/image/ic_launcher.png",128,128));
        label.setHorizontalAlignment(JLabel.CENTER);
        JLabel labelText = new JLabel("微信启动中...");
        labelText.setHorizontalAlignment(JLabel.CENTER);
        labelText.setFont(FontUtil.getDefaultFont(16));

        JPanel jPanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.MIDDLE));
        jPanel.add(label);
        jPanel.add(labelText);
        jPanel.setOpaque(false);

        JPanel contentPanel = new JPanel();
        contentPanel.add(jPanel);
        contentPanel.setOpaque(false);

        jWindow.add(contentPanel);
        jWindow.setAlwaysOnTop(true);
        jWindow.setBackground(new Color(0,0,0,0));
        jWindow.setVisible(true);
        jWindow.pack();



        new Thread(() -> {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            jWindow.dispose();
        }).start();
    }

    /**
     * 通过文件锁来判断程序是否正在运行
     *
     * @return 如果正在运行返回true，否则返回false
     */
    private boolean isApplicationRunning() {
        WechatConfiguration wechatConfiguration = SpringContextHolder.getBean(WechatConfiguration.class);
        boolean rv = false;
        try {
            String path = wechatConfiguration.getBasePath() + System.getProperty("file.separator") + "appLock";
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File lockFile = new File(path + System.getProperty("file.separator") + "appLaunch.lock");
            if (!lockFile.exists()) {
                lockFile.createNewFile();
            }

            //程序名称
            RandomAccessFile fis = new RandomAccessFile(lockFile.getAbsolutePath(), "rw");
            FileChannel fileChannel = fis.getChannel();
            FileLock fileLock = fileChannel.tryLock();
            if (fileLock == null) {
                System.out.println("程序已在运行.");
                rv = true;
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return rv;
    }

    public static void restartApplication() {
        try {
            String javaCommand = System.getProperty("java.home") + "/bin/java";
            String className = WeChatStater.class.getName();
            String classPath = System.getProperty("java.class.path");
            ProcessBuilder processBuilder = new ProcessBuilder(javaCommand, "-cp", classPath, className);
            processBuilder.start();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
