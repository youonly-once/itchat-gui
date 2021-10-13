package cn.shu;

import cn.shu.wechat.swing.app.Launcher;
import cn.shu.wechat.swing.components.VerticalFlowLayout;
import cn.shu.wechat.swing.frames.LoginFrame;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.utils.FontUtil;
import cn.shu.wechat.swing.utils.IconUtil;
import cn.shu.wechat.swing.utils.WindowUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;
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
                Launcher bean = context.getBean(Launcher.class);
                countDownLatch.countDown();
                bean.launch();
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
        jWindow.setLocationRelativeTo(null);


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                jWindow.dispose();
            }
        }).start();
    }
    /**
     * 关闭
     */
    public static void close(){
       try {
           LoginFrame bean = context.getBean(LoginFrame.class);
           bean.dispose();
           MainFrame.getContext().dispose();
       }catch (Exception e){
           e.printStackTrace();
       }
        context.close();
    }

    /**
     * restart
     */
    public static void restart(){
        //TODO 有问题
        close();
        main(new String[1]);
    }
}
