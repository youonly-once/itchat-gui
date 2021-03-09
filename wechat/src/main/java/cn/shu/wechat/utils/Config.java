package cn.shu.wechat.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import lombok.extern.log4j.Log4j2;
import cn.shu.wechat.enums.OsNameEnum;

/**
 * 配置信息
 *
 * @author ShuXinSheng
 * @date 创建时间：2017年4月23日 下午2:26:21
 * @version 1.1
 *
 */
@Log4j2
public class Config {

	public static final String API_WXAPPID = "API_WXAPPID";

	public static final String PIC_DIR = "D://weixin";
	public static final String QR_PATH = Config.PIC_DIR + File.separator + "login";
	public static final String DEFAULT_QR = "QR.jpg";

	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.183 Safari/537.36";
	public static final ArrayList<String> API_SPECIAL_USER = new ArrayList<String>(Arrays.asList("filehelper", "weibo",
			"qqmail", "fmessage", "tmessage", "qmessage", "qqsync", "floatbottle", "lbsapp", "shakeapp", "medianote",
			"qqfriend", "readerapp", "blogapp", "facebookapp", "masssendapp", "meishiapp", "feedsapp", "voip",
			"blogappweixin", "brandsessionholder", "weixin", "weixinreminder", "officialaccounts", "wxitil",
			"notification_messages", "wxid_novlwrv3lqwv11", "gh_22b87fa7cb3c", "userexperience_alarm"));

	/**
	 * 获取文件目录
	 *
	 * @author ShuXinSheng
	 * @date 2017年4月8日 下午10:27:42
	 * @return
	 */
	public static String getLocalPath() {
		String localPath = null;
		try {
			localPath = new File("").getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return localPath;
	}

	/**
	 * 获取系统平台
	 *
	 * @author ShuXinSheng
	 * @date 2017年4月8日 下午10:27:53
	 */
	public static OsNameEnum getOsNameEnum() {
		String os = System.getProperty("os.name").toUpperCase();
		if (os.indexOf(OsNameEnum.DARWIN.toString()) >= 0) {
			return OsNameEnum.DARWIN;
		} else if (os.indexOf(OsNameEnum.WINDOWS.toString()) >= 0) {
			return OsNameEnum.WINDOWS;
		} else if (os.indexOf(OsNameEnum.LINUX.toString()) >= 0) {
			return OsNameEnum.LINUX;
		} else if (os.indexOf(OsNameEnum.MAC.toString()) >= 0) {
			return OsNameEnum.MAC;
		}
		return OsNameEnum.OTHER;
	}

}
