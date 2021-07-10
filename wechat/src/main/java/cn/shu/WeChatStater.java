package cn.shu;

import cn.shu.wechat.controller.LoginController;
import cn.shu.wechat.swing.app.App;
import cn.shu.wechat.swing.app.Launcher;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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

public class WeChatStater {

    public static void main(String[] args) {
        Launcher launcher = new Launcher();
        launcher.launch();

    }




}
