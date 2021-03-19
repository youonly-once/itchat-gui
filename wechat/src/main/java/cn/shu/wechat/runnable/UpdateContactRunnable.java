package cn.shu.wechat.runnable;

import cn.shu.wechat.api.WeChatTool;
import cn.shu.wechat.utils.ChartUtil;
import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.service.ILoginService;
import cn.shu.wechat.utils.SleepUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import utils.DateUtil;

import javax.annotation.Resource;

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
@Component
public class UpdateContactRunnable implements Runnable {

	/**
	 * 登录服务
	 */
	@Resource
	private ILoginService loginService;


	@Override
	public void run() {
		if (Core.isAlive()) {
			loginService.webWxGetContact();

			loginService.WebWxBatchGetContact();

		}
	}

}
