package cn.shu.wechat.service;

import cn.shu.wechat.face.IMsgHandlerFace;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 登陆服务接口
 * 
 * @author SXS
 * @date 创建时间：2017年5月13日 上午12:07:21
 * @version 1.1
 *
 */
public interface ILoginService {



	/**
	 * 获取UUID
	 * 
	 * @author SXS
	 * @date 2017年5月13日 上午12:21:40
	 * @return
	 */
	String getUuid();

	/**
	 * 下周登录二维码图片
	 * 
	 * @author SXS
	 * @date 2017年5月13日 上午12:13:51
	 * @param qrPath 二维码图片保存路径
	 * @return {@code true} 获取成功 {@code false} 获取失败
	 */
	boolean getQR(String qrPath);


	/**
	 * 登陆
	 *
	 * @author SXS
	 * @date 2017年5月13日 上午12:14:07
	 * @return {@code true} 获取成功 {@code false} 获取失败
	 */
	boolean login();

	/**
	 * web初始化
	 * 
	 * @author SXS
	 * @date 2017年5月13日 上午12:14:13
	 * @return
	 */
	boolean webWxInit();

	/**
	 * 微信状态通知
	 * 
	 * @author SXS
	 * @date 2017年5月13日 上午12:14:24
	 */
	void wxStatusNotify();

	/**
	 * 接收消息
	 * 
	 * @author SXS
	 * @date 2017年5月13日 上午12:14:37
	 */
	void startReceiving( );

	/**
	 * 获取微信联系人
	 * 
	 * @author SXS
	 * @date 2017年5月13日 下午2:26:18
	 */
	void webWxGetContact();

	/**
	 * 批量获取群成员信息
	 * 
	 * @date 2017年6月22日 下午11:24:35
	 */
	void WebWxBatchGetContact();

	/**
	 * 批量获取群成员详细信息
	 * 和上个方法的区别在于这个方法可以获取群成员的性别、省市等信息
	 * 一次只能获取50个，群成员是好友的可以不用获取
	 * @date 2017年6月22日 下午11:24:35
	 */
	JSONArray WebWxBatchGetContactDetail(JSONObject groupObject) ;

}
