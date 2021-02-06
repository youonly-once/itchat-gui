package cn.shu.weichat;

import cn.shu.weichat.core.MsgCenter;
import cn.shu.weichat.face.IMsgHandlerFace;
import lombok.extern.log4j.Log4j2;

import cn.shu.weichat.controller.LoginController;

@Log4j2
public class Wechat {
	private IMsgHandlerFace msgHandler;

	public Wechat(IMsgHandlerFace msgHandler, String qrPath) {
		System.setProperty("jsse.enableSNIExtension", "false"); // 防止SSL错误
		this.msgHandler = msgHandler;

		// 登陆
		LoginController login = new LoginController();
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
