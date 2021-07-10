package cn.shu.wechat.swing.app;

import cn.shu.wechat.controller.LoginController;
import cn.shu.wechat.swing.db.service.TableService;
import cn.shu.wechat.swing.utils.DbUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

/**
 * Created by song on 17-5-28.
 */
@SpringBootApplication
public class App
{
    public static void main(String[] args)
    {


        ConfigurableApplicationContext run = SpringApplication.run(App.class, args);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Launcher launcher = new Launcher();
                launcher.launch();
            }
        });

        LoginController loginController = new LoginController();
        loginController.login(false);

    }

}
