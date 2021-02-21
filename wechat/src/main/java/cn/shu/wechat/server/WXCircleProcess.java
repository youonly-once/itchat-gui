package cn.shu.wechat.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import com.alibaba.fastjson.JSONException;
import lombok.extern.log4j.Log4j2;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import weixin.exception.WXException;
import weixin.utils.WXUntil;

/**
 * @author ShuXinSheng
 * @version 创建时间：2020年3月22日 上午11:13:14 类说明
 */
@Log4j2
public class WXCircleProcess {
	private static HashMap<String, String> tile2ID;

	/**
	 * 根据消息分词结果匹配文章
	 *
	 * @param result
	 *            分词结果集合
	 * @param userOpenid
	 *            用户Openid
	 * @return 暂未用
	 * @throws WXException
	 * @throws NullPointerException
	 * @throws JSONException
	 * @throws IOException
	 */
	static boolean matchMaterial(Result result, String userOpenid)
			throws JSONException, NullPointerException, WXException, IOException {

		if (tile2ID == null || tile2ID.isEmpty()) {
			tile2ID = WXUntil.getMaterialID2Title(WXUntil.getLocalServerAccessToken(), "news", "0", "20");
		}
		if (tile2ID == null || tile2ID.isEmpty()) {
			return false;
		}
		Map.Entry<String, String> maxEntry=null;
		if (tile2ID.size()>1) {

			int[] arr = new int[1];
			maxEntry = Collections.max(new ArrayList<>(tile2ID.entrySet()),
					new CircleSortComparator(result,arr));
			if (arr[0] != 1) {
				return false;
			}
		}else{

		}


		String[] strings = maxEntry.getValue().replace(",,", ", ,").split(",");
		// String[] strings = arrayList.get(0).getValue().replace(",,", ",
		// ,").split(",");
		/*
		 * HashMap<String, String> m = new HashMap<String, String>();
		 * m.put("title", strings[0]); m.put("thumb_url", strings[1]);
		 * m.put("digest", strings[2]); m.put("url", strings[3]);
		 */
		WXUntil.sendCustomMpnewsMsg(strings[4], userOpenid, WXUntil.getLocalServerAccessToken());
		return true;
	}

	/**
	 * 分词结果查找对应的TIPSTR
	 *
	 * @param result
	 *            分词结果
	 * @return
	 */
	static String matchTipStr(Result result) {
		/*
		 * HashMap<String, Integer> repaeatCount = new HashMap<>();
		 *
		 * for (Term term : result) { // 遍历文章标题 比对 //
		 * log.info("分词Item："+term.getName()); for (int i = 0; i <
		 * WXServletConfig.TIP_STR.length; i++) { // 标题包括了用户输入 if
		 * (WXServletConfig.TIP_STR[i].indexOf(term.getName()) >= 0) { // 匹配的次数
		 * if (repaeatCount.containsKey(WXServletConfig.TIP_STR[i])) {
		 * repaeatCount.put(WXServletConfig.TIP_STR[i],
		 * repaeatCount.get(WXServletConfig.TIP_STR[i]) + 1); } else {
		 * repaeatCount.put(WXServletConfig.TIP_STR[i], 1); } } }
		 *
		 * } // log.info("匹配Str："+repaeatCount.toString()); // 按重复次数排序 升序
		 *
		 * TreeSet <Map.Entry<String, Integer>> treeSet=new
		 * TreeSet<Map.Entry<String, Integer>>(new Comparator<Map.Entry<String,
		 * Integer>>() {
		 *
		 * @Override public int compare(Map.Entry<String, Integer> o1,
		 * Map.Entry<String, Integer> o2) { return o2.getValue()-o1.getValue();
		 * } }); treeSet.addAll(new ArrayList<>(repaeatCount.entrySet()));
		 *
		 * if (list == null || list.size() < 1) { return null; } // 以客服消息形式发送文章
		 * 发送最大次数的 int max = list.get(list.size() - 1).getValue(); String
		 * replyStr = ""; int i = 1; for (Entry<String, Integer> entry : list) {
		 * // log.info(entry.getValue()+"="+entry.getKey()); if
		 * (entry.getValue() == max) { replyStr += (i) + "、" + entry.getKey() +
		 * "\n"; i++; }
		 *
		 * } return replyStr;
		 */
		return "调试";
	}

	private String plustekPush(String receiveContent, String userOpenid)
			throws JSONException, NullPointerException, WXException, IOException {
		log.info("精益知识推送");
		// 精益知识推送，内容，备注，密码
		StringTokenizer stringTokenizer = new StringTokenizer(receiveContent, ",");
		if (stringTokenizer.countTokens() < 5) {
			return "格式不正确！";
		}

		stringTokenizer.nextToken();
		String content = stringTokenizer.nextToken();
		String remark = stringTokenizer.nextToken();
		String url = stringTokenizer.nextToken();
		String password = stringTokenizer.nextToken();

		if (!"liyuanqin".equals(password)) {

			return "密码不正确！";
		}
		if (content.isEmpty() || remark.isEmpty()) {
			return "推送内容不能为空0！";
		}

		/*
		 * new Thread(new Runnable() {
		 *
		 * @Override public void run() {
		 */

		String title = "精益知识推送";

		String time = "";
		// 获得我的昵称
		List<Object> list = new ArrayList<>();
		list.add(userOpenid);
		String access_token = WXUntil.getLocalServerAccessToken();
		HashMap<String, Object> userRemark = WXUntil.getUserNameAndIdMap(list, "namevalue",
				WXUntil.getLocalServerAccessToken());
		if (userRemark == null || userRemark.get(userOpenid) == null) {
			time = "通过微信公众号命令发送";
		} else {
			time = "<" + userRemark.get(userOpenid).toString() + "> 通过微信公众号群发";
		}

		// 获取所有粉丝信息

		access_token = WXUntil.getLocalServerAccessToken();

		// 获得分组下所有用户的ID
		List<Object> allUserIdArrray = WXUntil.getAllUserOpenId("", access_token);

		// List<Object>
		// allUserIdArrray=WXUntilgetGroupUserOpenId(WXUntilgetGroupId("测试",
		// access_token), access_token);

		if (allUserIdArrray == null) {
			return content = "\n\t发送失败:获取人员信息失败\n";
		}
		access_token = WXUntil.getLocalServerAccessToken();
		log.info(allUserIdArrray.toString() + ":" + allUserIdArrray.size());
		if (url.equals("home")) {
			url = "https://mp.weixin.qq.com/mp/homepage?__biz=MzU3MDY5MTEwNw==&hid=2&sn=cd2e7b3d4c1d1a6edc50906e3fff7dd5";
		}
		for (int i = 0, n = allUserIdArrray.size(); i < n; i++) {
			String teString = WXUntil.getTemplateData(allUserIdArrray.get(i).toString(),
					WXUntil.createDefDataMap(title, content, time, remark, url));
			WXUntil.sendTemplateMsg(teString, WXUntil.getLocalServerAccessToken());
		}

		// }
		/* }).start(); */
		// TODO Auto-generated method stub
		return "发送完成。";
	}

	private static class CircleSortComparator implements Comparator<Entry<String, String>> {
		private Result result;
		private int[] state;

		private CircleSortComparator(Result result, int[] state) {
			// TODO Auto-generated constructor stub
			this.result = result;
			this.state = state;
		}

		@Override
		public int compare(Entry<String, String> o1, Entry<String, String> o2) {
			// TODO Auto-generated method stub
			int count1 = 0;
			int count2 = 0;

			for (Term term : result) {

				if (o1.getKey().indexOf(term.getName().trim()) >= 0) {
					count1++;
				}
				if (o2.getKey().indexOf(term.getName().trim()) >= 0) {
					count2++;
				}
			}
			int max = Math.max(count1, count2);
			if (max > 0) {
				state[0] = 1;
			}
			// log.info(Integer.toString(state[0]));
			// 注意与sort区别
			// count2-count1在sort中为降序，在max中会返回最小值
			return count1 - count2;
		}

	}

}
