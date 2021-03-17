package cn.shu.wechat;


import cn.shu.wechat.utils.Config;
import lombok.extern.log4j.Log4j2;
import cn.shu.wechat.controller.LoginController;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Path;

@Log4j2
@Component
public class Wechat {


	/**
	 * 登录控制类
	 */
	@Resource
	private LoginController login;

	public void init(String qrPath) {
		// 防止SSL错误
		System.setProperty("jsse.enableSNIExtension", "false");
		// 登陆
		boolean mkdirs = new File(qrPath).getParentFile().mkdirs();

		login.login(qrPath);
	}

}
