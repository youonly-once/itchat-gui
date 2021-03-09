package cn.shu.wechat;


import cn.shu.wechat.core.MsgCenter;
import cn.shu.wechat.face.IMsgHandlerFace;
import lombok.extern.log4j.Log4j2;

import cn.shu.wechat.controller.LoginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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
		System.setProperty("jsse.enableSNIExtension", "false"); // 防止SSL错误
		// 登陆
		login.login(qrPath);
	}

}
