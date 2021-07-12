package cn.shu;

import cn.shu.wechat.controller.LoginController;
import cn.shu.wechat.swing.app.App;
import cn.shu.wechat.swing.app.Launcher;
import cn.shu.wechat.utils.SleepUtils;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.swing.*;

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

        new SwingWorker(){

            @Override
            protected Object doInBackground() throws Exception {
                ConfigurableApplicationContext context = new SpringApplicationBuilder(WeChatStater.class)
                        .headless(false)
                        .run(args);
                Launcher bean = context.getBean(Launcher.class);
                bean.launch();
                return null;
            }
        }.execute();
 /*       SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Launcher launcher = new Launcher();
                launcher.launch();
            }
        });*/
        SleepUtils.sleep(Long.MAX_VALUE);
    }




}
