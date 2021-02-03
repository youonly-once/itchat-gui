package shu.cn.weichat.thread;

import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shu.cn.weichat.api.WechatTools;
import shu.cn.weichat.core.Core;
import shu.cn.weichat.service.ILoginService;
import shu.cn.weichat.service.impl.LoginServiceImpl;
import shu.cn.weichat.utils.SleepUtils;

/**
 * 检查微信在线状态
 * <p>
 * 如何来感知微信状态？
 * 微信会有心跳包，LoginServiceImpl.syncCheck()正常在线情况下返回的消息中retcode报文应该为"0"，心跳间隔一般在25秒，
 * 那么可以通过最后收到正常报文的时间来作为判断是否在线的依据。若报文间隔大于60秒，则认为已掉线。
 * </p>
 * 
 * @author SXS
 * @date 创建时间：2017年5月17日 下午10:53:15
 * @version 1.1
 *
 */
@Log4j2
public class CheckLoginStatusThread implements Runnable {
	private Core core = Core.getInstance();
	private final ILoginService loginService = LoginServiceImpl.loginService;
	@Override
	public void run() {
		while (core.isAlive()) {
			long t1 = System.currentTimeMillis(); // 秒为单位
			if (t1 - core.getLastNormalRetcodeTime() > 60 * 1000) { // 超过60秒，判为离线
				//core.setAlive(false);
				//TODO 心跳检测不准确
				log.info("微信已离线");
			}
			SleepUtils.sleep(60*10 * 1000); // 休眠10秒
		}
	}

}
