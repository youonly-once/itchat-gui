package cn.shu;

import cn.shu.wechat.controller.LoginController;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

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
		ApplicationContext applicationContext = SpringApplication.run(WeChatStater.class,args);
		LoginController loginController = applicationContext.getBean(LoginController.class);
		loginController.login(true);
	}


}
