package cn.shu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;

/**
 * @author ShuXinSheng
 * @version 创建时间：2019年5月16日 下午1:02:30
 * 类说明
 */
@SpringBootApplication
@MapperScan("cn.shu.wechat.mapper")
public class Demo {

	@Autowired private IMsgHandlerFaceImpl iMsgHandlerFace;

	@PostConstruct
	public void init(){
		iMsgHandlerFace.init();
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ApplicationContext applicationContext = SpringApplication.run(Demo.class,args);
	/*	new IMsgHandlerFaceImpl();*/

	}


}
