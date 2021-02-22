package utils;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

@Log4j2
public class SendSms {

	private static CloseableHttpClient MyHttpClient = HttpClients.createMinimal();
	private static List<String> msgContentList = new ArrayList<String>();
	private static String userName = "cqgt";
	private static String userpass = "Haier2019";
	private static boolean isSend=true;
	private static Timer timer=new Timer();
	/*
        public static void main(String []arg0) {
            SendSms.sendSms1to1("15723468981", "5555");
            //SendSms.sendSms1toN("15723468981", "5555");
        }*/
	// 单条内容单个被叫提交短信
	public synchronized static void sendSms1to1(String recvMsisdn, String smsText) {
		//半小时内不重复发送
		if (isSend) {
			log.info("发送中："+new Date());
			isSend=false;
			TimerTask timerTask=new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					isSend=true;
					this.cancel();
					log.info("任务取消："+new Date());
				}
			};

			timer.schedule(timerTask, 1000*60*30);
		}else{
			log.info("消息重复："+new Date());
			return;
		}
		String sendUrl = "http://sms.95ai.cn:1082/wgws/OrderServlet";
		smsText+="【海尔】";
		log.info("短信发送："+recvMsisdn+","+smsText);
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("apName", userName));
			params.add(new BasicNameValuePair("apPassword", userpass));
			params.add(new BasicNameValuePair("srcId", ""));
			params.add(new BasicNameValuePair("calledNumber", recvMsisdn));
			params.add(new BasicNameValuePair("content", smsText));

			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
			HttpPost postMethod = new HttpPost(sendUrl);
			postMethod.setEntity(entity); // 将参数填入POST Entity中
			HttpResponse response = MyHttpClient.execute(postMethod); // 执行POST方法
			int statuscode = response.getStatusLine().getStatusCode();
			String restr = EntityUtils.toString(response.getEntity(), "UTF-8").trim();
			log.info("短信发送结果："+restr);
			if (statuscode == 200) {
				SAXReader sr = new SAXReader();
/*				Document doc = sr.read(new StringReader(restr));
				List<Node> errlist = doc.getRootElement().selectNodes("//error");
				for (Node n : errlist) {
					Element en = (Element) n;
					if ("0".equals(en.getText())) {
						System.out.println("发送结果：" + docToString(doc));
					} else {
						System.out.println("发送失败！" + docToString(doc));
					}
				}*/
			} else {
				throw new Exception("返回HTTP状态码错误！httpStatus=" + statuscode + "; restr=" + restr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 单条内容多个被叫提交短信，请使用营销接口下单
	public static  void sendSms1toN(String recvMsisdn, String smsText) {

		String sendUrl = "http://sms.95ai.cn:1082/wgws/BatchSubmit";
		smsText+="【海尔】";
		log.info("短信发送："+recvMsisdn+","+smsText);
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("apName", userName));
			params.add(new BasicNameValuePair("apPassword", "#"+userpass));
			params.add(new BasicNameValuePair("srcId", ""));
			params.add(new BasicNameValuePair("ServiceId", ""));
			params.add(new BasicNameValuePair("calledNumber", recvMsisdn));
			params.add(new BasicNameValuePair("content", smsText));

			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
			HttpPost postMethod = new HttpPost(sendUrl);
			postMethod.setEntity(entity); // 将参数填入POST Entity中
			HttpResponse response = MyHttpClient.execute(postMethod); // 执行POST方法
			int statuscode = response.getStatusLine().getStatusCode();
			String restr = EntityUtils.toString(response.getEntity(), "UTF-8").trim();
			log.info("短信发送结果："+restr);
			if (statuscode == 200) {
				SAXReader sr = new SAXReader();
				Document doc = sr.read(new StringReader(restr));
	/*			List<Node> errlist = doc.getRootElement().selectNodes("//error");
				for (Node n : errlist) {
					Element en = (Element) n;
					if ("0".equals(en.getText())) {
						System.out.println("发送结果：" + docToString(doc));
					} else {
						System.out.println("发送失败！" + docToString(doc));
					}
				}*/
			} else {
				throw new Exception("返回HTTP状态码错误！httpStatus=" + statuscode + "; restr=" + restr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

/*	// 多内容多被叫
	private void sendSmsNtoN_XML() {

		String sendUrl = "http://sms.95ai.cn:1082/wgws/MultiTextSubmit";

		// 构造XML
		Element root = DocumentHelper.createElement("request");
		Element econ1 = root.addElement("content");
		econ1.addElement("smsText")
				.addText("【未讯】55555555555555555555555555555555555555555555555555555555555555555555555");
		econ1.addElement("msisdn").addText("17095960660");
		econ1.addElement("msisdn").addText("18559951104");
		Element econ2 = root.addElement("content");
		econ2.addElement("smsText")
				.addText("【未讯】888888888888888888888888888888888888888888888888888888888888888888888888");
		econ2.addElement("msisdn").addText("17095960660");
		econ2.addElement("msisdn").addText("18559951104");
//			Element econ3 = root.addElement("content");
//			econ3.addElement("smsText").addText("【未讯】6666666666666666666666666666666666666666666666666666666666666666666666666");
//			econ3.addElement("msisdn").addText("11000000030");
//			econ3.addElement("msisdn").addText("12000000030");

		// post请求并解析回执
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("apName", userName));
			params.add(new BasicNameValuePair("apPassword", userpass));
			params.add(new BasicNameValuePair("srcId", ""));
			params.add(new BasicNameValuePair("ServiceId", ""));
			params.add(new BasicNameValuePair("isZip", "true"));
			params.add(new BasicNameValuePair("content", root.asXML()));

			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
			HttpPost postMethod = new HttpPost(sendUrl);
			postMethod.setEntity(entity); // 将参数填入POST Entity中
			HttpResponse response = MyHttpClient.execute(postMethod); // 执行POST方法
			int statuscode = response.getStatusLine().getStatusCode();
			String restr = EntityUtils.toString(response.getEntity(), "UTF-8").trim();
			AtomicInteger succCnt = new AtomicInteger(0);
			AtomicInteger failCnt = new AtomicInteger(0);
			List<PmSendLog> sendList = new ArrayList<PmSendLog>();
			if (statuscode == 200) {
				SAXReader sr = new SAXReader();
				Document doc = sr.read(new StringReader(restr));

				System.out.println("发送结果：" + docToString(doc));

			} else {
				throw new Exception("返回HTTP状态码错误！httpStatus=" + statuscode + "; restr=" + restr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	private static String docToString(Document doc) {
		StringWriter sw;
		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("UTF-8");
			sw = new StringWriter();
			XMLWriter writer = new XMLWriter(sw, format);
			writer.write(doc.getRootElement());
			writer.flush();
			return sw.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
