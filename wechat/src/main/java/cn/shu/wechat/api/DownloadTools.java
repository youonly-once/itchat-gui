package cn.shu.wechat.api;

import cn.shu.wechat.beans.msg.sync.AddMsgList;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.enums.URLEnum;
import cn.shu.wechat.enums.WXReceiveMsgCodeEnum;
import cn.shu.wechat.enums.WXReceiveMsgCodeOfAppEnum;
import cn.shu.wechat.utils.Config;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import cn.shu.wechat.utils.MyHttpClient;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import cn.shu.utils.MD5Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

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
     * 文件下载状态
     * 文件路径为KEY、状态为VALUE
     * true： 下载成功
     * false：下载中
     */
    public final static Hashtable<String, Boolean> FILE_DOWNLOAD_STATUS = new Hashtable<>();


    /**
     * 处理下载任务
     *
     * @param msg  消息对象
     * @param path 保存路径
     * @return {@code true} 下载成功
     * {@code false} 下载失败
     * @author SXS
     * @date 2017年4月21日 下午11:00:25
     */
    public static boolean getDownloadFn(AddMsgList msg, String path) {
        Map<String, String> headerMap = new HashMap<String, String>();
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        WXReceiveMsgCodeEnum msgTypeEnum = WXReceiveMsgCodeEnum.getByCode(msg.getMsgType());
        String url = "";
        HttpEntity entity = null;
        switch (msgTypeEnum) {
            case MSGTYPE_IMAGE:
            case MSGTYPE_EMOTICON:
                url = String.format(URLEnum.WEB_WX_GET_MSG_IMG.getUrl(), (String) Core.getLoginInfoMap().get("url"));
                params.add(new BasicNameValuePair("msgid", String.valueOf(msg.getNewMsgId())));
                params.add(new BasicNameValuePair("skey", (String) Core.getLoginInfoMap().get("skey")));
                entity = MyHttpClient.doGet(url, params, true, headerMap);
                break;
            case MSGTYPE_VOICE:
                url = String.format(URLEnum.WEB_WX_GET_VOICE.getUrl(), (String) Core.getLoginInfoMap().get("url"));
                params.add(new BasicNameValuePair("msgid", String.valueOf(msg.getNewMsgId())));
                params.add(new BasicNameValuePair("skey", (String) Core.getLoginInfoMap().get("skey")));
                entity = MyHttpClient.doGet(url, params, true, headerMap);
                break;
            case MSGTYPE_VIDEO:
                headerMap.put("Range", "bytes=0-");
                url = String.format(URLEnum.WEB_WX_GET_VIEDO.getUrl(), (String) Core.getLoginInfoMap().get("url"));
                params.add(new BasicNameValuePair("msgid", String.valueOf(msg.getNewMsgId())));
                params.add(new BasicNameValuePair("skey", (String) Core.getLoginInfoMap().get("skey")));
                entity = MyHttpClient.doGet(url, params, true, headerMap);
                break;
            case MSGTYPE_APP:
                headerMap.put("Range", "bytes=0-");
                url = String.format(URLEnum.WEB_WX_GET_MEDIA.getUrl(), (String) Core.getLoginInfoMap().get("fileUrl"));
                params.add(new BasicNameValuePair("sender", msg.getFromUserName()));
                params.add(new BasicNameValuePair("mediaid", msg.getMediaId()));
                params.add(new BasicNameValuePair("filename", msg.getFileName()));
                params.add(new BasicNameValuePair("msgid", String.valueOf(msg.getNewMsgId())));
                params.add(new BasicNameValuePair("skey", (String) Core.getLoginInfoMap().get("skey")));
                entity = MyHttpClient.doGet(url, params, true, headerMap);
                break;
            case MSGTYPE_MAP:
                url = msg.getContent().substring(msg.getContent().indexOf(":<br/>") + ":<br/>".length());
                url = URLEnum.BASE_URL.getUrl() + url;
                entity = MyHttpClient.doGet(url, null, false, null);
                break;
            default:
                break;
        }

        boolean downloadStatus = false;
        OutputStream out = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    boolean mkdirs = parentFile.mkdirs();
                    if (!mkdirs) {
                        log.error("创建目录失败：{}", parentFile.getAbsolutePath());
                    }
                }
                boolean newFile = file.createNewFile();
                if (!newFile) {
                    log.error("创建文件失败：{}", path);
                }

            }
            if (entity == null) {
                log.error("下载失败：response entity is null.");
            }
            out = new FileOutputStream(file);
            byte[] bytes = EntityUtils.toByteArray(entity);
            out.write(bytes);
            out.flush();
            log.info("资源下载完成：{}", path);
            downloadStatus = true;
        } catch (Exception e) {
            log.info(e.getMessage());
            return false;
        } finally {
            DownloadTools.FILE_DOWNLOAD_STATUS.put(path, downloadStatus);
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return false;
    }


    /**
     * 下载头像
     *
     * @param relativeUrl 头像地址
     * @param userName    用户名
     * @return 下载成功头像保存路径
     * 下载失败 ""
     */
    public static String downloadHeadImg(String relativeUrl, String userName) {


        //获取远端对象字节数组
        String url = String.format(URLEnum.WEB_WX_GET_HEAD_IMAGE.getUrl(), relativeUrl);
        HttpEntity entity = MyHttpClient.doGet(url, null, false, null);
        byte[] bytes;
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
        String remarkNameByUserName = ContactsTools.getContactDisplayNameByUserName(userName);
        if (userName.startsWith("@@")) {
            remarkNameByUserName = ContactsTools.getContactDisplayNameByUserName(userName);
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
            if (!Files.exists(path.getParent())) {
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
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return path.toString();
    }

    /**
     * 替换字符串中不能用于创建文件或文件夹的字符
     *
     * @param string 字符串
     * @return 处理的字符串
     */
    public static String replace(String string) {
        if (string == null) {
            return null;
        }
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
     *
     * @param msg      消息对象
     * @param saveFile 保存路径
     */
    public static void downloadFile(AddMsgList msg, String saveFile) {
        final String path = saveFile;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                DownloadTools.getDownloadFn(msg, path);
            }
        };
        ExecutorServiceUtil.getGlobalExecutorService().execute(runnable);

    }

    /**
     * 获取消息资源文件保存路径
     *
     * @param msg 接收的消息对象
     * @return {@code String} 消息资源文件保存路径
     * {@code null} 获取失败或无需下载的资源
     */
    public static String getDownloadFilePath(AddMsgList msg) {
        //下载文件的后缀名
        WXReceiveMsgCodeEnum msgTypeEnum = WXReceiveMsgCodeEnum.getByCode(msg.getMsgType());
        String ext = null;
        switch (msgTypeEnum) {
            case MSGTYPE_MAP:
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
                switch (WXReceiveMsgCodeOfAppEnum.getByCode(msg.getAppMsgType())) {
                    case UNKNOWN:
                        break;
                    case FAVOURITE:
                        break;
                    case PROGRAM:
                        break;
                    case FILE:
                        int i = msg.getFileName().lastIndexOf(".");
                        if (i == -1) {
                            ext = "";
                        } else {
                            ext = msg.getFileName().substring(i);
                        }

                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        if (ext == null) {
            return null;
        }
        //发消息的用户或群名称
        String username = ContactsTools.getContactDisplayNameByUserName(msg.getFromUserName());
        //群成员名称
        String groupUsername = "";
        if (msg.isGroupMsg() && msg.getMemberName() != null) {
            groupUsername = ContactsTools.getContactDisplayNameByUserName(msg.getMemberName());
        }
        username = replace(username);

        String path = Config.PIC_DIR + File.separator + msgTypeEnum + File.separator + username + File.separator;
        String fileName = groupUsername + "-"
                + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + "-" + msg.getNewMsgId()
                + ext;
        fileName = replace(fileName);

        return path + fileName;
    }

    /**
     * @param dir 目录
     * @return {@code true} 创建成功
     * {@code false} 创建失败
     */
    public static boolean createLogDir(String dir) {
        File logFile = new File(dir);
        if (!logFile.exists()) {
            if (logFile.isFile()) {
                boolean mkdirs = logFile.getParentFile().mkdirs();
            }
            return logFile.mkdirs();
        }
        return true;
    }
}
