//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.shu.wechat.api;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.shu.wechat.core.Core;
import cn.shu.wechat.enums.StorageLoginInfoEnum;
import cn.shu.wechat.enums.URLEnum;
import cn.shu.wechat.utils.MyHttpClient;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * 微信工具类
 * @author SXS
 */
@Log4j2
public final class WeChatTool {
    private static final char[] HEX = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 获取文件类型，该文件类型是网页版微信上传文件时定义的类型
     * @param file 文件对象
     * @return 文件类型
     */
    public static String getFileType(File file) {
        String var1 = getFileSuffix(file);
        byte var2 = -1;
        switch(var1.hashCode()) {
            case 97669:
                if (var1.equals("bmp")) {
                    var2 = 0;
                }
                break;
            case 105441:
                if (var1.equals("jpg")) {
                    var2 = 3;
                }
                break;
            case 108273:
                if (var1.equals("mp4")) {
                    var2 = 4;
                }
                break;
            case 111145:
                if (var1.equals("png")) {
                    var2 = 1;
                }
                break;
            case 3268712:
                if (var1.equals("jpeg")) {
                    var2 = 2;
                }
                break;
            default:
                break;
        }

        switch(var2) {
            case 0:
            case 1:
            case 2:
            case 3:
                return "pic";
            case 4:
                return "video";
            default:
                return "doc";
        }
    }

    /**
     * 获取文件后缀 gif、jpg...
     * @param file 文件对象
     * @return 文件后缀
     */
    public static String getFileSuffix(File file) {
        try {
            FileInputStream is = new FileInputStream(file);
            Throwable var2 = null;

            String var7;
            try {
                byte[] b = new byte[3];
                is.read(b, 0, b.length);
                String fileCode = bytesToHex(b);
                byte var6 = -1;
                switch(fileCode.hashCode()) {
                    case -1277558572:
                        if (fileCode.equals("ffd8ff")) {
                            var6 = 0;
                        }
                        break;
                    case 1541115082:
                        if (fileCode.equals("474946")) {
                            var6 = 2;
                        }
                        break;
                    case 1657499917:
                        if (fileCode.equals("89504e")) {
                            var6 = 1;
                        }
                        break;
                    default:
                        break;
                }

                switch(var6) {
                    case 0:
                        var7 = "jpg";
                        return var7;
                    case 1:
                        var7 = "png";
                        return var7;
                    case 2:
                        var7 = "gif";
                        return var7;
                    default:
                        if (fileCode.startsWith("424d")) {
                            var7 = "bmp";
                            return var7;
                        }

                        if (file.getName().lastIndexOf(46) <= 0) {
                            var7 = "";
                            return var7;
                        }

                        var7 = file.getName().substring(file.getName().lastIndexOf(46) + 1);
                }
            } catch (Throwable var22) {
                var2 = var22;
                throw var22;
            } finally {
                if (is != null) {
                    if (var2 != null) {
                        try {
                            is.close();
                        } catch (Throwable var21) {
                            var2.addSuppressed(var21);
                        }
                    } else {
                        is.close();
                    }
                }

            }

            return var7;
        } catch (IOException var24) {
            var24.printStackTrace();
            return "";
        }
    }

    /**
     * 字节转16进制
     * @param bytes 字节数组
     * @return 16进制
     */
    private static String bytesToHex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];

        for(int i = 0; i < bytes.length; ++i) {
            byte b = bytes[i];
            chars[i << 1] = HEX[b >>> 4 & 15];
            chars[(i << 1) + 1] = HEX[b & 15];
        }

        return new String(chars);
    }

    /**
     * 退出微信
     *
     * @author SXS
     * @date 2017年5月18日 下午11:56:54
     */
    public static void webWXLogOut() {
        String url = String.format(URLEnum.WEB_WX_LOGOUT.getUrl(),
                Core.getLoginInfoMap().get(StorageLoginInfoEnum.url.getKey()));
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("redirect", "1"));
        params.add(new BasicNameValuePair("type", "1"));
        params.add(
                new BasicNameValuePair("skey", (String) Core.getLoginInfoMap().get(StorageLoginInfoEnum.skey.getKey())));
        try {
            HttpEntity entity = MyHttpClient.doGet(url, params, false, null);
            String text = EntityUtils.toString(entity, Consts.UTF_8);
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
    }

    /**
     * 保存用户信息到核心类
     */
/*    public static void setUserInfo() {
        for (Map.Entry<String, JSONObject> jsonObjectEntry : Core.getContactMap().entrySet()) {
            Core.getUserInfoMap().put(jsonObjectEntry.getValue().getString("NickName"), jsonObjectEntry.getValue());
            Core.getUserInfoMap().put(jsonObjectEntry.getValue().getString("UserName"), jsonObjectEntry.getValue());
        }

    }*/
    /**
     *
     * 根据用户昵称设置备注名称
     *
     * @date 2017年5月27日 上午12:21:40
     * @param nickName 联系人昵称
     * @param remName 联系人备注
     */
  /*  public static void remarkNameByNickName(String nickName, String remName) {
        String url = String.format(URLEnum.WEB_WX_REMARKNAME.getUrl(), Core.getLoginInfoMap().get("url"),
                Core.getLoginInfoMap().get(StorageLoginInfoEnum.pass_ticket.getKey()));
        Map<String, Object> msgMap = new HashMap<String, Object>();
        Map<String, Object> msgMap_BaseRequest = new HashMap<String, Object>();
        msgMap.put("CmdId", 2);
        msgMap.put("RemarkName", remName);
        msgMap.put("UserName", Core.getUserInfoMap().get(nickName).get("UserName"));
        msgMap_BaseRequest.put("Uin", Core.getLoginInfoMap().get(StorageLoginInfoEnum.wxuin.getKey()));
        msgMap_BaseRequest.put("Sid", Core.getLoginInfoMap().get(StorageLoginInfoEnum.wxsid.getKey()));
        msgMap_BaseRequest.put("Skey", Core.getLoginInfoMap().get(StorageLoginInfoEnum.skey.getKey()));
        msgMap_BaseRequest.put("DeviceID", Core.getLoginInfoMap().get(StorageLoginInfoEnum.deviceid.getKey()));
        msgMap.put("BaseRequest", msgMap_BaseRequest);
        try {
            String paramStr = JSON.toJSONString(msgMap);
            HttpEntity entity =  MyHttpClient.doPost(url, paramStr);
            String result = EntityUtils.toString(entity, Consts.UTF_8);
            log.info("修改备注" + remName);
        } catch (Exception e) {
            log.error("remarkNameByUserName", e);
        }
    }*/

    /**
     * 获取微信在线状态
     *
     * @date 2017年6月16日 上午12:47:46
     * @return {@code true} 在线  {@code false} 离线
     */
    public static boolean getWechatStatus() {
        return Core.isAlive();
    }
}
