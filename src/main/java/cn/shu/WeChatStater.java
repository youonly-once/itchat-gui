package cn.shu;

import cn.shu.wechat.swing.app.Launcher;
import cn.shu.wechat.swing.frames.LoginFrame;
import cn.shu.wechat.swing.frames.MainFrame;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.swing.*;
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
    public static void main(String[] args) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                context = new SpringApplicationBuilder(WeChatStater.class)
                        .headless(false)
                        .run();
                Launcher bean = context.getBean(Launcher.class);
                bean.launch();
                countDownLatch.countDown();
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
