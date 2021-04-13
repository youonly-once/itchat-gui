package cn.shu;

import cn.shu.wechat.Wechat;
import cn.shu.wechat.utils.Config;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

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
		Wechat wechat = applicationContext.getBean(Wechat.class);
		wechat.init(Config.QR_PATH);
	}


}
