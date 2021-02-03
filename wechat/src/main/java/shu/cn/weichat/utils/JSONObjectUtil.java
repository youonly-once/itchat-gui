package shu.cn.weichat.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.nlpcn.commons.lang.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @作者 舒新胜
 * @项目 weixin
 * @创建时间 2/3/2021 12:11 PM
 */
public class JSONObjectUtil {
    /**
     * 返回二个JSONObject的差异
     * @param oldO 旧
     * @param newO 新
     * @return difference
     */
    public static Map<String,Map<String,String>> getDifferenceMap(JSONObject oldO,JSONObject newO){
        Map<String,Map<String,String>>  difference= new HashMap<>();
        for (Map.Entry<String, Object> entry : oldO.entrySet()) {
            String newV = newO.getString(entry.getKey());
            String oldV = StringUtil.toString(entry.getValue());
            if (!oldV.equals(newV)) {
                HashMap<String, String> temp = new HashMap<>();
                temp.put(StringUtil.toString(oldV), newV);
                difference.put(StringUtil.toString(entry.getKey()),temp);
            }
        }
        return difference;
    }
}
