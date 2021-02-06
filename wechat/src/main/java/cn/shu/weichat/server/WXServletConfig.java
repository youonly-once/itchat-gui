package cn.shu.weichat.server;

import lombok.extern.log4j.Log4j2;

import java.io.File;

/**
 * @author ShuXinSheng
 * @version 创建时间：2020年3月22日 上午11:08:45
 * 类说明
 */
@Log4j2
public class WXServletConfig {
	public final static String IMAGE_DIR= "D:"+File.separator+"weixin_image";
	public final static String LOG_DIR= "D:\\log_weixin";
	public final static String ALLTIP_STR =  "你可以发送以下文字："
			+ "\n  1、开启/关闭工程不良推送,密码"
			+ "\n  2、开启/关闭小时产量推送,密码"
			+ "\n  3、开启停机时推送,姓名,密码"
			+ "\n  4、关闭停机时推送,密码"
			+ "\n\t\t\t  例：开启工程不良推送,mima"
			+ "\n\t\t\t  例：开启停机时推送,张三,mima"
			+ "\n  5、查询条码扫描记录/测试数据,密码"
			+ "\n\t\t\t  例：CE0J扫描记录,mima"
			+ "\n\t\t\t  例：CE0J四码合一,mima"
			+ "\n\t\t\t  例：CE0J安检仪,mima"
			+ "\n\t\t\t  例： 发送二维码或条码图片查询扫描记录"
			+ "\n  6、查询停机时,姓名,日期,密码"
			+ "\n\t\t\t  例：查询停机时,舒新胜,10-28~10-29,mima"
			+ "\n\t\t\t  例：查询停机时,舒新胜,当周/日/月,mima"
			+ "\n  7、生成二维码(条码号+二维码/条形码)"
			+ "\n\t\t\t  例：CE0J二维码"
			+ "\n  8、获取报表网址"
			+ "\n\t\t\t  例：停机时查询/小时产量报表/日产量报表/工程不良明细/工程不良周报/首页,mima"
			+ "\n  9、电话变更通知,姓名，旧电话，新电话，密码"
			+ "\n\t\t\t  例：电话变更通知,舒新胜,110,120,mima"
			+ "\n  10、发送关键词获取办公教程"
			+ "\n\t\t\t  例：高清视频会议怎么用？"
			+"\n  备注：密码请咨询管理员";
	public final static String[] TIP_STR =
			{"\n开启/关闭工程不良推送,密码"
					+ "\n\t\t\t例：开启工程不良推送,mima"
					, "\n开启/关闭小时产量推送,密码"
					+ "\n\t\t\t例：开启小时产量推送,mima"
					, "\n开启停机时推送,姓名,密码"
					+ "\n\t\t\t例：开启停机时推送,张三,mima"
					, "\n关闭停机时推送,密码"
					+ "\n\t\t\t例：关闭停机时推送 ,mima"
					, "\n查询条码扫描记录/测试数据,密码"
					+ "\n\t\t\t例：CE0J扫描记录,mima"
					+ "\n\t\t\t例：CE0J四码合一,mima"
					+ "\n\t\t\t例：CE0J安检仪,mima"
					+ "\n\t\t\t例： 发送二维码或条码图片查询扫描记录"
					, "\n查询停机时,姓名,日期,密码"
					+ "\n\t\t\t例：查询停机时,舒新胜,10-28~10-29,mima"
					+ "\n\t\t\t例：查询停机时,舒新胜,当周/日/月,mima"
					, "\n生成二维码(条码号+二维码/条形码)"
					+ "\n\t\t\t例：CE0J二维码"
					, "\n获取报表网址"
					+ "\n\t\t\t例：停机时查询/小时产量报表/日产量报表/工程不良明细/工程不良周报/首页,mima"
					, "\n电话变更通知,姓名，旧电话，新电话，密码"
					+ "\n\t\t\t例：电话变更通知,舒新胜,110,120,mima"
					,"\n备注：密码请咨询管理员"};

}
