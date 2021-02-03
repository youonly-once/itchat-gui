package shu.cn.weichat.server;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qq.weixin.mp.aes.AesException;
import com.qq.weixin.mp.aes.SHA1;

import lombok.extern.log4j.Log4j2;
import weixin.utils.WXConfig;

/**
 * @author ShuXinSheng
 * @version 创建时间：2020年3月22日 上午10:55:21
 * 类说明
 */
@Log4j2
public class WXDoGetJIami {

	WXDoGetJIami(){}

	void doGet(HttpServletRequest request,HttpServletResponse response) throws IOException{
		// 微信加密签名
		String signature = request.getParameter("signature");
		// 随机字符串
		String echostr = request.getParameter("echostr");
		// 时间戳
		String timestamp = request.getParameter("timestamp");
		// 随机数
		String nonce = request.getParameter("nonce");
		if (signature == null || echostr == null || timestamp == null || nonce == null) {
			return;
		}
		// SHA1加密
		String jiami = "";
		try {
			jiami = SHA1.getSHA1(WXConfig.TOKEN, timestamp, nonce, "");// 这里是对三个参数进行加密
			// 确认请求来至微信
			if (jiami.equals(signature)){
				response.getWriter().print(echostr);
				response.getWriter().close();
			}
		} catch (AesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
