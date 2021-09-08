package cn.shu.wechat.utils;

import cn.shu.wechat.beans.tuling.request.*;
import cn.shu.wechat.beans.tuling.response.TuLingResponseBean;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * @作者 舒新胜
 * @项目 weixin
 * @创建时间 2/2/2021 2:50 PM
 */
public class TuLingUtil {
    private static final String requestUrl = "http://openapi.tuling123.com/openapi/api/v2";

    /**
     * 调用图灵机器人
     *
     * @param msg 消息
     * @return 结果
     * @throws IOException
     * @throws NullPointerException
     * @throws JSONException
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
        // HttpEntity httpEntity = ;
        String result = EntityUtils.toString(HttpUtil.doPost(requestUrl, data));
        // String result = HttpUtil.sendPost(requestUrl, data);
        result = result.replaceAll("\"values\":\\{\"(url|image|video|new|voice)\":", "\"values\":{\"text\":");
        return JSON.parseObject(result, TuLingResponseBean.class);
    }


    /**
     * 调用青云客机器人
     *
     * @param msg 消息
     * @return 结果
     * @throws IOException
     * @throws NullPointerException
     * @throws JSONException
     */
    public static String robotMsgQYK(String msg) throws IOException, NullPointerException, JSONException {
        String url = "http://api.qingyunke.com/api.php?key=free&appid=0&msg=" + msg;
        String result = EntityUtils.toString(HttpUtil.doPost(requestUrl, ""));
        JSONObject jsonObject = JSON.parseObject(result);
        return jsonObject.getString("content");

    }

}
