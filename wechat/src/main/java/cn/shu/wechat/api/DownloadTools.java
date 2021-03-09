package cn.shu.wechat.api;

import cn.shu.wechat.enums.WXReceiveMsgCodeEnum;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.enums.WXReceiveMsgCodeOfAppEnum;
import cn.shu.wechat.utils.Config;
import cn.shu.wechat.utils.MyHttpClient;
import lombok.extern.log4j.Log4j2;
import cn.shu.wechat.beans.msg.sync.AddMsgList;
import cn.shu.wechat.enums.URLEnum;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import utils.MD5Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * 下载工具类
 *
 * @author SXS
 * @version 1.1
 * @date 创建时间：2017年4月21日 下午11:18:46
 */
@Log4j2
public class DownloadTools {

    /**
     * 处理下载任务
     *
     * @param msg
     * @param msgTypeEnum
     * @param path
     * @return
     * @author SXS
     * @date 2017年4月21日 下午11:00:25
     */
    public static Object getDownloadFn(AddMsgList msg, WXReceiveMsgCodeEnum msgTypeEnum, String path) {
        Map<String, String> headerMap = new HashMap<String, String>();
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        String url = "";
        switch (msgTypeEnum) {
            case MSGTYPE_IMAGE:
            case MSGTYPE_EMOTICON:
                url = String.format(URLEnum.WEB_WX_GET_MSG_IMG.getUrl(), (String) Core.getLoginInfoMap().get("url"));
                break;
            case MSGTYPE_VOICE:
                url = String.format(URLEnum.WEB_WX_GET_VOICE.getUrl(), (String) Core.getLoginInfoMap().get("url"));
                break;
            case MSGTYPE_VIDEO:
                headerMap.put("Range", "bytes=0-");
                url = String.format(URLEnum.WEB_WX_GET_VIEDO.getUrl(), (String) Core.getLoginInfoMap().get("url"));
                break;
            case MSGTYPE_APP:
                headerMap.put("Range", "bytes=0-");
                url = String.format(URLEnum.WEB_WX_GET_MEDIA.getUrl(), (String) Core.getLoginInfoMap().get("fileUrl"));
                params.add(new BasicNameValuePair("sender", msg.getFromUserName()));
                params.add(new BasicNameValuePair("mediaid", msg.getMediaId()));
                params.add(new BasicNameValuePair("filename", msg.getFileName()));
                break;
        }
        params.add(new BasicNameValuePair("msgid", String.valueOf(msg.getNewMsgId())));
        params.add(new BasicNameValuePair("skey", (String) Core.getLoginInfoMap().get("skey")));
        HttpEntity entity = MyHttpClient.doGet(url, params, true, headerMap);
        OutputStream out = null;
        try {
            out = new FileOutputStream(path);
            byte[] bytes = EntityUtils.toByteArray(entity);
            out.write(bytes);
            out.flush();

        } catch (Exception e) {
            log.info(e.getMessage());
            return false;
        }finally {

                if (out != null) {
                    try {
                    out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

        }
        return null;
    }

    ;

    /**
     * 下载头像
     *
     * @param relativeUrl 头像地址
     * @param userName    用户名
     * @return 返回保存全路径
     */
    public static String downloadHeadImg(String relativeUrl, String userName) {


        //获取远端对象字节数组
        String url = String.format(URLEnum.WEB_WX_GET_HEAD_IMAGE.getUrl(), relativeUrl);
        HttpEntity entity = MyHttpClient.doGet(url, null, false, null);
        byte[] bytes ;
        try {
            bytes = EntityUtils.toByteArray(entity);
        } catch (IOException e) {
            e.printStackTrace();
            log.info(e.getMessage());
            return "";
        }
        //计算远端文件md5
        String md5Str = MD5Util.getMD5(bytes);


        //创建本地文件
        String remarkNameByUserName = WechatTools.getDisplayNameByUserName(userName);
        if (userName.startsWith("@@")) {
            remarkNameByUserName = WechatTools.getGroupDisplayNameByUserName(userName);
        }
        if (StringUtils.isEmpty(remarkNameByUserName)) {
            remarkNameByUserName = userName;
        }
        remarkNameByUserName = DownloadTools.replace(remarkNameByUserName);
        //创建目录
        String savePath = Config.PIC_DIR + "/headimg/"
                + File.separator + remarkNameByUserName
                + File.separator + md5Str + ".jpg";
        Path path = Paths.get(savePath);
        OutputStream out = null;
        try {
            if (!Files.exists(path.getParent())){
           Files.createDirectories(path.getParent());
        }
        File file = path.toFile();
        //头像文件已存在，md5及大小相等
        if (Files.exists(path) && Files.size(path) == bytes.length) {
            return path.toString();
        }


            Files.createFile(path);
            out = new FileOutputStream(file);
            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            log.info(e.getMessage());
        }finally {
            try {
                if (out!=null){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return path.toString();
    }

    /**
     * 不可建立文件夹的字符
     */
    public static String replace(String string) {
        string = string.replace("/", "").
                replace("|", "").
                replace("\\", "").
                replace("*", "").
                replace(":", "").
                replace("\"", "").
                replace("?", "").
                replace("<", "").
                replace("<", "").
                replace(" ", "").
                replace("\n", "").
                replace("\r", "").
                replace("\t", "").
                replace(">", "");
        return string;

    }

    /**
     * 下载消息记录中的图片、视频...
     * @param msg
     * @param msgTypeEnum
     * @return
     */
    public static String downloadFile(AddMsgList msg, WXReceiveMsgCodeEnum msgTypeEnum) {

        Callable<String> callable = new Callable<String>() {

            @Override
            public String call()  {
                //下载文件的后缀名
                String ext = null;
                switch (msgTypeEnum){
                    case MSGTYPE_EMOTICON:
                    case MSGTYPE_IMAGE:
                        ext = ".gif";
                        break;
                    case MSGTYPE_VOICE:
                        ext = ".mp3";
                        break;
                    case MSGTYPE_VIDEO:
                    case MSGTYPE_MICROVIDEO:
                        ext = ".mp4";
                        break;
                    case MSGTYPE_APP:
                        switch (WXReceiveMsgCodeOfAppEnum.getByCode(msg.getAppMsgType())){
                            case UNKNOWN:
                                break;
                            case FAVOURITE:
                                break;
                            case PROGRAM:
                                break;
                            case FILE:
                                ext = msg.getFileName().substring(msg.getFileName().lastIndexOf("."));
                                break;
                        }
                        break;
                }
                if (ext == null){
                    return null;
                }
                //发消息的用户或群名称
                String username = WechatTools.getDisplayNameByUserName(msg.getFromUserName());
                //群成员名称
                String groupUsername;
                if (msg.getMemberName() != null) {
                    groupUsername = WechatTools.getDisplayNameByUserName(msg.getMemberName()) + "-";
                } else {
                    groupUsername = "";
                }
                String fileName = groupUsername + "-"
                        + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + "-" + msg.getNewMsgId()
                        + ext;
                fileName = DownloadTools.replace(fileName);
                username =  DownloadTools.replace(username);
                // 保存语音的路径
                String path = Config.PIC_DIR+ File.separator + msgTypeEnum + File.separator + username + File.separator;
                boolean logDir = createLogDir(path);
                if (logDir) {
                    DownloadTools.getDownloadFn(msg, msgTypeEnum, path + fileName);
                } else {
                    return null;
                }
                return path + fileName;
            }
        };

        FutureTask<String> futureTask = new FutureTask<String>(callable);
        new Thread(futureTask, "download_media").start();
        try {
            return futureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }
    /*
     * 创建目录
     */
    public static boolean createLogDir(String dir) {
        File logFile = new File(dir);
        if (!logFile.exists()) {
            return logFile.mkdirs();
        }
        return true;
    }
}
