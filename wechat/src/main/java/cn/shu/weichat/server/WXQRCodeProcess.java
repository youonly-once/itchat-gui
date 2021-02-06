package cn.shu.weichat.server;

import java.io.File;
import java.io.FileNotFoundException;

import cn.shu.weichat.utils.CodeParse;
import lombok.extern.log4j.Log4j2;

/**
 * @author ShuXinSheng
 * @version 创建时间：2020年3月22日 上午11:11:30
 * 类说明
 */
@Log4j2
public class WXQRCodeProcess {
	static String createCodeImage(String code,int type) throws FileNotFoundException{

		String path = WXServletConfig.IMAGE_DIR ;
		String filePath=path+File.separator + code.hashCode()+ type + ".png";
		log.info("二维码图片的路径:"+filePath);
		File file = new File(filePath);
		if (!file.exists()) {
			if(!createCodePath(path)){
				throw new FileNotFoundException("创建二维码保存路径失败");
			}

			// 一维码
			if (type == 1) {
				CodeParse.encode(code, 500, 200, filePath);

				// 二维码
			} else if (type == 2) {
				CodeParse.enQrcode(code, 500, 500, filePath);

			}
		}
		return filePath;
	}
	private static boolean createCodePath(String pathStr){
		File path=new File(pathStr);
		if (!path.exists()) {
			return path.mkdirs();
		}
		return true;
	}
}
