package cn.shu;

import cn.shu.wechat.swing.app.Launcher;
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

    public static void main(String[] args) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                ConfigurableApplicationContext context = new SpringApplicationBuilder(WeChatStater.class)
                        .headless(false)
                        .run(args);
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


}
