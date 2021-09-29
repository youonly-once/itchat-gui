package cn.shu.wechat.utils;

import cn.shu.wechat.enums.OsNameEnum;
import cn.shu.wechat.pojo.dto.msg.sync.AddMsgList;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 常用工具类
 *
 * @author SXS
 * @version 1.1
 * @date 创建时间：2017年4月8日 下午10:59:55
 */
@Log4j2
public class CommonTools {

    public static Process printQr(String qrPath) {
        Process exec = null;
        switch (Config.getOsNameEnum()) {
            case WINDOWS:
                if (Config.getOsNameEnum().equals(OsNameEnum.WINDOWS)) {
                    Runtime runtime = Runtime.getRuntime();
                    try {
                        exec = runtime.exec("cmd /c start " + qrPath);
                        /** 当路径中有空格时，则应该程序和参数分开写*/
		/*			String[] paramArr = new String[2];
					paramArr[0] = "C:\\Program Files (x86)\\Microsoft Office\\root\\Office16\\WINWORD.EXE";
					paramArr[1] = "E:\\wmx\\Map_in-depth.docx";
					Runtime runtime = Runtime.getRuntime();
					Process process = runtime.exec(paramArr);*/
                        /*	exec = Runtime.getRuntime().exec("rundll32 c:\\Windows\\System32\\shimgvw.dll,ImageView_Fullscreen "+qrPath);
                         */
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case MAC:
                if (Config.getOsNameEnum().equals(OsNameEnum.MAC)) {
                    Runtime runtime = Runtime.getRuntime();
                    try {
                        exec = runtime.exec("open " + qrPath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            default:
                break;
        }
        return exec;
    }

    /**
     * 关闭弹出的二维码图片
     *
     * @param exec
     */
    public static void closeQr(Process exec) {
        if (exec != null) {
            if (exec.isAlive()) {
                exec.destroyForcibly();
            }

        }
    }

    public static boolean clearScreen() {
        switch (Config.getOsNameEnum()) {
            case WINDOWS:
                if (Config.getOsNameEnum().equals(OsNameEnum.WINDOWS)) {
                    Runtime runtime = Runtime.getRuntime();
                    try {
                        runtime.exec("cmd /c " + "cls");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            default:
                break;
        }
        return true;
    }

    /**
     * 正则表达式处理工具
     *
     * @return
     * @author SXS
     * @date 2017年4月9日 上午12:27:10
     */
    public static Matcher getMatcher(String regEx, String text) {
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(text);
        return matcher;
    }

    /**
     * xml解析器
     *
     * @param text
     * @return
     * @author SXS
     * @date 2017年4月9日 下午6:24:25
     */
    public static Document xmlParser(String text) {
        Document doc = null;
        StringReader sr = new StringReader(text);
        InputSource is = new InputSource(sr);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    public static JSONObject structFriendInfo(JSONObject userObj) {
        Map<String, Object> friendInfoTemplate = new HashMap<String, Object>();
        friendInfoTemplate.put("UserName", "");
        friendInfoTemplate.put("City", "");
        friendInfoTemplate.put("DisplayName", "");
        friendInfoTemplate.put("PYQuanPin", "");
        friendInfoTemplate.put("RemarkPYInitial", "");
        friendInfoTemplate.put("Province", "");
        friendInfoTemplate.put("KeyWord", "");
        friendInfoTemplate.put("RemarkName", "");
        friendInfoTemplate.put("PYInitial", "");
        friendInfoTemplate.put("EncryChatRoomId", "");
        friendInfoTemplate.put("Alias", "");
        friendInfoTemplate.put("Signature", "");
        friendInfoTemplate.put("NickName", "");
        friendInfoTemplate.put("RemarkPYQuanPin", "");
        friendInfoTemplate.put("HeadImgUrl", "");

        friendInfoTemplate.put("UniFriend", 0);
        friendInfoTemplate.put("Sex", 0);
        friendInfoTemplate.put("AppAccountFlag", 0);
        friendInfoTemplate.put("VerifyFlag", 0);
        friendInfoTemplate.put("ChatRoomId", 0);
        friendInfoTemplate.put("HideInputBarFlag", 0);
        friendInfoTemplate.put("AttrStatus", 0);
        friendInfoTemplate.put("SnsFlag", 0);
        friendInfoTemplate.put("MemberCount", 0);
        friendInfoTemplate.put("OwnerUin", 0);
        friendInfoTemplate.put("ContactFlag", 0);
        friendInfoTemplate.put("Uin", 0);
        friendInfoTemplate.put("StarFriend", 0);
        friendInfoTemplate.put("Statues", 0);

        friendInfoTemplate.put("MemberList", new ArrayList<Object>());

        JSONObject r = new JSONObject();
        Set<String> keySet = friendInfoTemplate.keySet();
        for (String key : keySet) {
            if (userObj.containsKey(key)) {
                r.put(key, userObj.get(key));
            } else {
                r.put(key, friendInfoTemplate.get(key));
            }
        }

        return r;
    }

    public static String getSynckey(JSONObject obj) {
        JSONArray obj2 = obj.getJSONArray("List");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < obj2.size(); i++) {
            JSONObject obj3 = (JSONObject) JSON.toJSON(obj2.get(i));
            sb.append(obj3.get("Val") + "|");
        }
        return sb.substring(0, sb.length() - 1); // 656159784|656159911|656159873|1491905341

    }

    public static JSONObject searchDictList(List<JSONObject> list, String key, String value) {
        JSONObject r = null;
        for (JSONObject i : list) {
            if (i.getString(key).equals(value)) {
                r = i;
                break;
            }
        }
        return r;
    }



    /**
     * 消息格式化
     *
     * @author SXS
     * @date 2017年4月23日 下午4:19:08
     */
    public static void emojiFormatter(AddMsgList msg) {

        msg.setContent(StringEscapeUtils.unescapeXml(msg.getContent()));
        msg.setContent(emojiFormatter(msg.getContent()));

    }

    /**
     * 消息格式化
     *
     * @author SXS
     * @date 2017年4月23日 下午4:19:08
     */
    public static String emojiFormatter(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return msg;
        }
        msg = msg.replace("<br/>", "\n");
        Matcher matcher = getMatcher("<span class=\"emoji emoji(.{1,10})\"></span>", msg);
        StringBuilder sb = new StringBuilder();
        int lastStart = 0;
        while (matcher.find()) {
            String str = matcher.group(1);
            if (str.length() == 6) {

            } else if (str.length() == 10) {

            } else {
                String tmp = msg.substring(lastStart, matcher.start());
                sb.append(tmp).append("&#x").append(str).append(";");
                lastStart = matcher.end();
            }
        }
        if (lastStart < msg.length()) {
            sb.append(msg.substring(lastStart));
        }
        if (sb.length() != 0) {
            msg = EmojiParser.parseToUnicode(sb.toString());
        } else {
            msg = EmojiParser.parseToUnicode(msg);
        }
        return msg;
        // TODO 与emoji表情有部分兼容问题，目前暂未处理解码处理 d.put(k,
        // StringEscapeUtils.unescapeHtml4(d.getString(k)));

    }
}
