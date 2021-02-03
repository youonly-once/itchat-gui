package com.qq.weixin.mp.aes;

import java.security.MessageDigest;
import java.util.Arrays;

import lombok.extern.log4j.Log4j2;
import utils.Log;

/**
 * @author ShuXinSheng
 * @version 创建时间：2019年2月16日 下午11:32:39
 * 类说明
 */
@Log4j2
public class SHA1 {
	/**
	 * 鐢⊿HA1绠楁硶鐢熸垚瀹夊叏绛惧悕
	 * @param token 绁ㄦ嵁
	 * @param timestamp 鏃堕棿鎴�
	 * @param nonce 闅忔満瀛楃涓�
	 * @param encrypt 瀵嗘枃
	 * @return 瀹夊叏绛惧悕
	 * @throws AesException
	 */
	public static String getSHA1(String token, String timestamp, String nonce, String encrypt) throws AesException
	{
		try {
			String[] array = new String[] { token, timestamp, nonce, encrypt };
			StringBuffer sb = new StringBuffer();
			// 瀛楃涓叉帓搴�
			Arrays.sort(array);
			for (int i = 0; i < 4; i++) {
				sb.append(array[i]);
			}
			String str = sb.toString();
			// SHA1绛惧悕鐢熸垚
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(str.getBytes());
			byte[] digest = md.digest();

			StringBuffer hexstr = new StringBuffer();
			String shaHex = "";
			for (int i = 0; i < digest.length; i++) {
				shaHex = Integer.toHexString(digest[i] & 0xFF);
				if (shaHex.length() < 2) {
					hexstr.append(0);
				}
				hexstr.append(shaHex);
			}
			return hexstr.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new AesException(AesException.ComputeSignatureError);
		}
	}
	public static String getSHA1ForJSSDK(String ticket, String timestamp, String noncestr, String url)
			throws AesException {
		ticket = "jsapi_ticket=" + ticket;
		noncestr = "noncestr=" + noncestr;
		timestamp = "timestamp=" + timestamp;
		try {
			String[] array = new String[] { ticket, timestamp, noncestr, url };
			StringBuffer sb = new StringBuffer();
			// 字符串排序
			Arrays.sort(array);
			for (int i = 0; i < 3; i++) {
				sb.append(array[i] + "&");
			}
			sb.append(array[3]);
			log.info("JSSDK singler str:" + sb);

			String str = sb.toString();
			// SHA1签名生成
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(str.getBytes());
			byte[] digest = md.digest();

			StringBuffer hexstr = new StringBuffer();
			String shaHex = "";
			for (int i = 0; i < digest.length; i++) {
				shaHex = Integer.toHexString(digest[i] & 0xFF);
				if (shaHex.length() < 2) {
					hexstr.append(0);
				}
				hexstr.append(shaHex);
			}
			return hexstr.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new AesException(AesException.ComputeSignatureError);
		}
	}
}
