package cn.shu.wechat.utils;

import cn.shu.wechat.entity.Contacts;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author user
 * @作者 舒新胜
 * @项目 weixin
 * @创建时间 2/3/2021 12:11 PM
 */
public class JSONObjectUtil {
    private static Pattern pattern = Pattern.compile(".+\\?seq=(\\d+).+");
    /**
     * 返回二个JSONObject的差异
     *
     * @param oldO 旧
     * @param newO 新
     * @return difference
     */
    public static Map<String, Map<String, String>> getDifferenceMap(JSONObject oldO, JSONObject newO) {
        Map<String, Map<String, String>> difference = new HashMap<>(1);
        for (Map.Entry<String, Object> entry : oldO.entrySet()) {
            String newV = newO.getString(entry.getKey());
            String oldV = entry.getValue() == null ? "" : entry.getValue().toString();
            //是否相同
            boolean equals = oldV.equals(newV);
            if ("HeadImgUrl".equals(entry.getKey()) || "headimgurl".equals(entry.getKey())) {
                Matcher matcherNew = pattern.matcher(newV);
                Matcher matcherOld = pattern.matcher(oldV);
                if (matcherNew.find() && matcherOld.find()) {
                    //头像相同
                    String groupNew = matcherNew.group(1);
                    String groupOld = matcherOld.group(1);
                    equals = groupNew.equals(groupOld);
                }
            }
            if ("memberlist".equals(entry.getKey()) || "MemberList".equals(entry.getKey())) {
                equals = true;
            }
            if ("remarkpyinitial".equals(entry.getKey())) {
                equals = true;
            }
            if ("remarkpyquanpin".equals(entry.getKey())) {
                equals = true;
            }
            if (!equals) {
                HashMap<String, String> temp = new HashMap<>(1);
                temp.put(CommonTools.emojiFormatter(oldV), CommonTools.emojiFormatter(newV));
                difference.put(entry.getKey(), temp);
            }
        }
        return difference;
    }

    /**
     * 返回二个JSONObject的差异
     *
     * @param oldO 旧
     * @param newO 新
     * @return difference
     */
    public static Map<String, Map<String, String>> getDifferenceMap(Contacts oldO, Contacts newO) {
        JSONObject old = JSONObject.parseObject(JSON.toJSONString(oldO));
        JSONObject new_ = JSONObject.parseObject(JSON.toJSONString(newO));
        return getDifferenceMap(old, new_);

    }
}
