package weixin.exception;

import lombok.extern.log4j.Log4j2;

/**
 * @author ShuXinSheng
 * @version 创建时间：2020年3月30日 上午10:09:50
 * 类说明
 */
@Log4j2
public class WXTokenExpireException extends Exception {
	public WXTokenExpireException() {
		// TODO Auto-generated constructor stub
		super("access_token过期");
	}
}
