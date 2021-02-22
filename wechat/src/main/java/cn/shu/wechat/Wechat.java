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
	 * 消息处理类
	 */
	@Resource
	private IMsgHandlerFace msgHandler;

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

	public void start() {

		class MyRunnable implements Runnable{
			@Override
			public void run() {
				MsgCenter.handleMsg(msgHandler);
			}
		}

		log.info("+++++++++++++++++++开始消息处理+++++++++++++++++++++");
		for (int i = 0; i < 100; i++) {
			new Thread(new MyRunnable(),"HandleThread "+i).start();
		}

	}

}