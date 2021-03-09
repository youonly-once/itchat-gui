package utils;

import bean.tuling.request.*;
import bean.tuling.response.TuLingResponseBean;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @作者 舒新胜
 * @项目 weixin
 * @创建时间 2/2/2021 2:50 PM
 */
public class TuLingUtil {
    private static final String requestUrl = "http://openapi.tuling123.com/openapi/api/v2";
    /**
     * 调用图灵机器人
     */
    public static TuLingResponseBean robotMsgTuling(String msg) throws IOException, NullPointerException, JSONException {


        TuLingRequestBean tuLingRequestBean = TuLingRequestBean.builder()
                .perception(
                        Perception.builder()
                                .inputImage(InputImage.builder()
                                        .url("http://www.16sucai.com/uploadfile/2013/0630/20130630095526852.jpg").build())
                                .inputText(InputText.builder()
                                        .text(msg)
                                        .build())
                                .selfInfo(SelfInfo.builder()
                                        .location(Location.builder()
                                                .city("重庆")
                                                .province("重庆")
                                                .street("港城南路").build()
                                        ).build())
                                .build()
                )
                .reqType(0)
                .userInfo(UserInfo.builder()
                        .apiKey("f6446c50c3a24c0c85fded541c8613a7")
                        .userId("324129").build()).build();
        String data = JSON.toJSONString(tuLingRequestBean);
        String result = HttpUtil.sendPost(requestUrl, data);
        result = result.replaceAll("\"values\":\\{\"(url|image|video|new|voice)\":", "\"values\":{\"text\":");
        return JSON.parseObject(result, TuLingResponseBean.class);
       /* JSONObject jsonObject = JSON.parseObject(result);
        JSONArray jsonArray = JSONArray.parseArray(jsonObject.get("results").toString());
        String code = JSON.parseObject(jsonObject.get("intent").toString()).get("code").toString();
        if (code.equals("4003")) {// 请求次数超限制!
            return robotMsgQYK(msg);
        }
        StringBuilder resultStr = new StringBuilder();
        for (Object str : jsonArray) {
            Map<String, Object> map = utils.JSONUtils.parseJSON2Map(str.toString());
            String type = map.get("resultType").toString();
            JSONObject jsonObject2 = JSON.parseObject(map.get("values").toString());
            resultStr.append(jsonObject2.get(type) + "\n");
        }
        return resultStr.toString();*/
    }

    /**
     * 调用青云客机器人
     */
    public static String robotMsgQYK(String msg) throws IOException, NullPointerException, JSONException {
        String url = "http://api.qingyunke.com/api.php?key=free&appid=0&msg=" + msg;

        String result = HttpUtil.sendGet(url, "");
        // log.info("测试"+msg+result);
        JSONObject jsonObject = JSON.parseObject(result);
        /*
         * JSONArray jsonArray = (JSONArray)
         * JSONArray.fromObject(jsonObject.get("results")); String resultStr =
         * ""; for (Object str : jsonArray) { Map<String, Object> map =
         * utils.JSONUtils.parseJSON2Map(str.toString()); String type =
         * map.get("resultType").toString(); net.sf.json.JSONObject jsonObject2
         * = net.sf.json.JSONObject.fromObject(map.get("values")); resultStr +=
         * jsonObject2.get(type) + "\n"; } log.info(result);
         */
        // log.info(jsonObject.getString("content"));
        return jsonObject.getString("content");

    }

    public static void main(String[] args) throws IOException {
        System.out.println(TuLingUtil.robotMsgTuling("你好"));
    }
}
