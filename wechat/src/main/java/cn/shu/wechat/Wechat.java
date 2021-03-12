package cn.shu.wechat;


import lombok.extern.log4j.Log4j2;
import cn.shu.wechat.controller.LoginController;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

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
		login.login(qrPath);
	}

}
