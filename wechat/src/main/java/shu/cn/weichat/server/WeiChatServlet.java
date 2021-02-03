package shu.cn.weichat.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONException;
import lombok.extern.log4j.Log4j2;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import shu.cn.IMsgHandlerFaceImpl;
import utils.DateUtil;
import weixin.exception.WXException;
import weixin.utils.WXUntil;

/**
 * Servlet implementation class test http://cqgtweixin.natapp1.cc/weixin_server/
 */
@WebServlet("/")
@Log4j2
public class WeiChatServlet extends HttpServlet {

	private final static long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public WeiChatServlet() {
		super();
		log.info("构造Servlet");// 打印出来

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// 微信认证处理

		// 获取服务器发送过来的信息，因为不是参数，得用输入流读取
		new IMsgHandlerFaceImpl();
		new WXDoGetJIami().doGet(request, response);



	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// log.info("当前线程：" + Thread.currentThread().getName());
		log.info("\n\n\n##################" + DateUtil.getCurrDateAndTimeMil()
				+ "##############################################################################################################################");
		// TODO Auto-generated method stub
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		// log.info("签名："+request.getParameter("signature"));
		// 创建日志文件

		// 获取服务器发送过来的信息，因为不是参数，得用输入流读取
		PrintWriter out = response.getWriter();
		Map<String, String> map = null;
		try {
			map = xmlToMap(request);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			out.println("success");
			return;
		}
		// XML To Map error

		// log.info(map.toString());
		if (map == null) {
			log.info("error map is null");// 打印出来
			out.println("success");
			return;
		}

		try {
			doPostProcess(request, response, map);
		} catch (JSONException | NullPointerException | WXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String userOpenid = map.get("FromUserName");// 获取参数中的openid
			String myOpendid = map.get("ToUserName");// 获取参数中的openid
			out.println(WXUntil.replyTextMsg("系统错误、请稍后再试", userOpenid, myOpendid));
		}
	}

	/**
	 * xml 转MAP
	 *
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws DocumentException
	 */
	private Map<String, String> xmlToMap(HttpServletRequest request) throws IOException, DocumentException {

		Map<String, String> map = new HashMap<String, String>();

		// 从dom4j的jar包中，拿到SAXReader对象。

		SAXReader reader = new SAXReader();

		InputStream is = request.getInputStream();// 从request中，获取输入流
		/*
		 * BufferedReader in = new BufferedReader(new InputStreamReader(is,
		 * "UTF-8")); String line; String result = null; while ((line =
		 * in.readLine()) != null) { result += line; } log.info(result);
		 */
		Document doc = reader.read(is);// 从reader对象中,读取输入流
		Element root = doc.getRootElement();// 获取XML文档的根元素
		List<Element> list = root.elements();// 获得根元素下的所有子节点
		getXmlMap(map, list);
		if (map.get("MsgType").equals("event")
				&& (map.get("Event").equals("TEMPLATESENDJOBFINISH") || map.get("Event").equals("LOCATION"))) {
			return map;
		}
		// 保存访问记录
		// WXUserInfoProcess.saveUserInfo(map);
		log.info("received msg xml2Map:" + map.toString());
		return map;
	}

	private void getXmlMap(Map<String, String> map, List<Element> list) {
		for (Element e : list) {
			if (e.elements().isEmpty()) {
				map.put(e.getName(), e.getText());// 遍历list对象，并将结果保存到集合中
			} else {
				getXmlMap(map, e.elements());
			}
		}

	}
	/**
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws WXException
	 * @throws NullPointerException
	 * @throws JSONException
	 */
	private void doPostProcess(HttpServletRequest request, HttpServletResponse response,Map<String, String> map) throws IOException, JSONException, NullPointerException, WXException{

		PrintWriter out = response.getWriter();


		String userOpenid = map.get("FromUserName");// 获取参数中的openid
		String myOpendid = map.get("ToUserName");// 获取参数中的openid
		String msgType = map.get("MsgType");
		// 字段为空
		if (userOpenid == null || myOpendid == null || msgType == null) {
			log.info("error info is null");// 打印出来
			out.println("success");
			return;
		} else if (msgType.equals("text") || msgType.equals("voice")) { // 文本
			// 或语音
			// 消息
			String receContent = "";
			if (msgType.equals("text")) {

				receContent = map.get("Content");

			} else {// 语音消息，先转文字

				/*
				 * String mediaId=map.get("MediaId"); String
				 * format=map.get("format");
				 *
				 * String accessToken =
				 * wXUntil.getLocalServerAccessToken(false);
				 * receContent=wXUntil.voice2Text(mediaId,format,userOpenid,
				 * msgId, accessToken);
				 *
				 *
				 * receContent = map.get("Recognition");
				 */

			}

			WXMsgProcess.processTextMsg(map, out);

		} else if (msgType.equals("event")) {// 事件消息
			WXMsgProcess.processEventMsg(map, out);

		} else if (msgType.equals("image")) {// 图片消息
			WXMsgProcess.processImageMsg(map, out);

		} else {
			out.println("success");
		}
		out.close();
	}

}
