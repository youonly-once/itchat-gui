package cn.shu.wechat.api;

import cn.shu.wechat.configuration.WechatConfiguration;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.enums.URLEnum;
import cn.shu.wechat.enums.WXReceiveMsgCodeEnum;
import cn.shu.wechat.pojo.dto.msg.sync.AddMsgList;
import cn.shu.wechat.pojo.dto.msg.url.WXMsgUrl;
import cn.shu.wechat.utils.HttpUtil;
import cn.shu.wechat.utils.MD5Util;
import cn.shu.wechat.utils.SleepUtils;
import cn.shu.wechat.utils.SpringContextHolder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

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
     * null 下载失败 或者未下载
     */
    public final static ConcurrentHashMap<String, Boolean> FILE_DOWNLOAD_STATUS = new ConcurrentHashMap<>();

    /**
     * 文件下载进度
     */
    public final static HashMap<String, LinkedBlockingDeque<Long>> FILE_DOWNLOAD_PROCESS = new HashMap<>();

    private final static WechatConfiguration WECHAT_CONFIGURATION = SpringContextHolder.getBean(WechatConfiguration.class);


    /**
     * 处理下载任务
     *
     * @param msg 消息对象
     * @author SXS
     * @date 2017年4月21日 下午11:00:25
     */
    public static void getDownloadFn(AddMsgList msg) {
        Map<String, String> headerMap = new HashMap<String, String>();
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

        WXReceiveMsgCodeEnum msgTypeEnum = WXReceiveMsgCodeEnum.getByCode(msg.getMsgType());
        String url = "";
        HttpEntity entity = null;
        switch (msgTypeEnum) {
            case MSGTYPE_IMAGE:
                url = String.format(URLEnum.WEB_WX_GET_MSG_IMG.getUrl(), (String) Core.getLoginInfoMap().get("url"));
                entity = downloadEntityByMsgID(
                        url,String.valueOf(msg.getNewMsgId())
                        ,null,headerMap,true);
                break;
            case MSGTYPE_EMOTICON:
                url = String.format(URLEnum.WEB_WX_GET_MSG_IMG.getUrl(), (String) Core.getLoginInfoMap().get("url"));
                entity = downloadEntityByMsgID(url,String.valueOf(msg.getNewMsgId()), WXMsgUrl.BIG_TYPE,headerMap,true);
                break;
            case MSGTYPE_VOICE:
                url = String.format(URLEnum.WEB_WX_GET_VOICE.getUrl(), (String) Core.getLoginInfoMap().get("url"));
                entity = downloadEntityByMsgID(
                        url,String.valueOf(msg.getNewMsgId())
                        ,null,headerMap,true);
                break;
            case MSGTYPE_VIDEO:
                headerMap.put("Range", "bytes=0-");
                url = String.format(URLEnum.WEB_WX_GET_VIEDO.getUrl(), (String) Core.getLoginInfoMap().get("url"));
                entity = downloadEntityByMsgID(
                        url,String.valueOf(msg.getNewMsgId())
                        ,null,headerMap,true);
                break;
            case MSGTYPE_APP:
               // headerMap.put("Range", "bytes=0-");
                url = String.format(URLEnum.WEB_WX_GET_MEDIA.getUrl(), (String) Core.getLoginInfoMap().get("fileUrl"));
                params.add(new BasicNameValuePair("sender", msg.getFromUserName()));
                params.add(new BasicNameValuePair("mediaid", msg.getMediaId()));
                params.add(new BasicNameValuePair("filename", msg.getFileName()));
               // params.add(new BasicNameValuePair("msgid", String.valueOf(msg.getNewMsgId())));
               // params.add(new BasicNameValuePair("skey", (String) Core.getLoginInfoMap().get("skey")));
                entity = HttpUtil.doGet(url, params, true, headerMap);
                break;
            case MSGTYPE_MAP:
                url = msg.getContent().substring(msg.getContent().indexOf(":<br/>") + ":<br/>".length());
                url = URLEnum.BASE_URL.getUrl() + url;
                entity = HttpUtil.doGet(url, null, false, null);
                break;
            default:
                break;
        }
        entity2File(entity, msg.getFilePath());
    }

    /**
     * 下载缩略图
     *
     * @param msgId 消息id
     * @param path  保存路径
     * @return {@code true} 下载成功
     * {@code false} 下载失败
     * @author SXS
     * @date 2017年4月21日 下午11:00:25
     */
    public static void downloadFileByMsgId(long msgId, String path) {
        Map<String, String> headerMap = new HashMap<String, String>();
        String url = "";
        HttpEntity entity = null;
        url = String.format(URLEnum.WEB_WX_GET_MSG_IMG.getUrl(), (String) Core.getLoginInfoMap().get("url"));
        entity = downloadEntityByMsgID(
                url, String.valueOf(msgId)
                , WXMsgUrl.SLAVE_TYPE, headerMap, true);
        if (entity == null) {
            DownloadTools.FILE_DOWNLOAD_STATUS.remove(path);
            log.error("下载失败：response entity is null.");
        }
        entity2File(entity, path);

    }

    /**
     * entity 2 file
     *
     * @param entity
     * @param path
     */
    private static void entity2File(HttpEntity entity, String path) {
        if (entity == null) {
            DownloadTools.FILE_DOWNLOAD_STATUS.remove(path);
            DownloadTools.FILE_DOWNLOAD_PROCESS.remove(path);
            log.error("下载失败：response entity is null.");
            return;
        }
        if (!DownloadTools.FILE_DOWNLOAD_STATUS.containsKey(path)) {
            DownloadTools.FILE_DOWNLOAD_STATUS.put(path, false);
        }

        if (!DownloadTools.FILE_DOWNLOAD_PROCESS.containsKey(path)) {
            DownloadTools.FILE_DOWNLOAD_PROCESS.put(path,new LinkedBlockingDeque<Long>());
        }
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
            out = new FileOutputStream(file);
            int size = 1024 * 1024;
            byte[] data = new byte[size];
            int readLen =0;
            long readed = 0;
            while ((readLen = entity.getContent().read(data, 0, size))>0){
                out.write(data,0,readLen);
                readed = readed +readLen;
                DownloadTools.FILE_DOWNLOAD_PROCESS.get(path).offer(readed);
                System.out.println(readed);
            }
            DownloadTools.FILE_DOWNLOAD_PROCESS.get(path).offer(-100L);
            out.flush();
            log.info("资源下载完成：{}", path);
            DownloadTools.FILE_DOWNLOAD_STATUS.put(path, true);
        } catch (Exception e) {
            DownloadTools.FILE_DOWNLOAD_PROCESS.remove(path);
            DownloadTools.FILE_DOWNLOAD_STATUS.remove(path);
            log.info(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                entity.getContent().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
    /**
     * entity to image
     * @param entity entity
     * @return
     */
    private static BufferedImage entity2Image(HttpEntity entity){
        InputStream content = null;
        try {
            content = entity.getContent();
            if (content == null){
                return null;
            }
            BufferedImage image = ImageIO.read(content);
            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (content!=null){
                try {
                    content.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    /**
     * entity to ImageIcon
     * @param entity entity
     * @return
     */
    private static ImageIcon entity2ImageIcon(HttpEntity entity){
        InputStream content = null;
        try {
            content = entity.getContent();
            if (content == null){
                return null;
            }
            byte[] bytes = EntityUtils.toByteArray(entity);
           return new ImageIcon(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (content!=null){
                try {
                    content.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    /**
     * 下载微信大头像
     *
     * @param relativeUrl 微信头像地址
     * @param userName    用户名
     * @return 下载成功头像保存路径
     * 下载失败 ""
     */
    public static String downloadBigHeadImg(String relativeUrl, String userName) {
        //获取远端对象字节数组
        String url = String.format(URLEnum.WEB_WX_GET_HEAD_IMAGE_BIG.getUrl(), relativeUrl);
        return downloadHeadImg(url, userName);

    }

    /**
     * 下载头像
     *
     * @param relativeUrl 微信头像地址
     * @param userName    用户名
     * @return 下载成功头像保存路径
     * 下载失败 ""
     */
    public static String downloadHeadImgThum(String relativeUrl, String userName) {

        //获取远端对象字节数组
        String url = String.format(URLEnum.WEB_WX_GET_HEAD_IMAGE_THUM.getUrl(), relativeUrl);
        return downloadHeadImg(url,userName);

    }
    /**
     * 下载头像
     *
     * @param url 头像地址全路径
     * @param userName    用户名
     * @return 下载成功头像保存路径
     * 下载失败 ""
     */
    public static String downloadHeadImg(String url, String userName) {
        HttpEntity entity = HttpUtil.doGet(url, null, false, null);
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
        String savePath = WECHAT_CONFIGURATION.getBasePath() + "/headimg/"
                + File.separator + remarkNameByUserName
                + File.separator + md5Str + ".jpg";
        Path path = Paths.get(savePath);
        OutputStream out = null;
        boolean downloadStatus = false;
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
            downloadStatus = true;
        } catch (IOException e) {
            log.info(e.getMessage());
        } finally {
            DownloadTools.FILE_DOWNLOAD_STATUS.put(path.toString(), downloadStatus);
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
     * 下载头像缩略图
     *
     * @param relativeUrl 微信头像地址
     * @return Image对象
     */
    public static Image downloadHeadImgByRelativeUrl(String relativeUrl) {
        String url = String.format(URLEnum.WEB_WX_GET_HEAD_IMAGE_THUM.getUrl(), relativeUrl);
        return downloadImgByAbsoluteUrl(url);

    }

    /**
     * 下载图片
     *
     * @param url 图片地址
     * @return Image对象
     */
    public static BufferedImage downloadImgByAbsoluteUrl(String url) {
        HttpEntity entity = HttpUtil.doGet(url, null, true, null);
        return entity2Image(entity);
    }


    /**
     * 下载图片根据消息id
     * @param msgId 消息ID
     * @param type 类型
     * @return Image
     */
    public static BufferedImage downloadImgByMsgID(String msgId,String type){
        return entity2Image(downloadImgEntityByMsgID(msgId, type));
    }

    /**
     * 下载图片根据消息id
     * @param msgId 消息ID
     * @param type 类型
     * @return Image
     */
    public static ImageIcon downloadImgIconByMsgID(String msgId, String type){

        return entity2ImageIcon(downloadImgEntityByMsgID(msgId, type));
    }
    /**
     * 下载图片根据消息id
     * @param msgId 消息ID
     * @param type 类型
     * @return Image
     */
    public static byte[] downloadImgByteByMsgID(String msgId, String type){
        HttpEntity httpEntity = downloadImgEntityByMsgID(msgId, type);
        try {
            byte[] bytes = EntityUtils.toByteArray(httpEntity);
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 下载图片根据消息id
     * @param msgId 消息ID
     * @param type 类型
     * @return Image
     */
    private static HttpEntity downloadImgEntityByMsgID(String msgId, String type){
        String url = String.format(URLEnum.WEB_WX_GET_MSG_IMG.getUrl(), (String) Core.getLoginInfoMap().get("url"));
        HttpEntity entity = downloadEntityByMsgID(
                url, String.valueOf(msgId), type
                , null, true);
        return entity;
    }
    /**
     * 下载资源根据消息id
     * @param msgId 消息ID
     * @param type 类型
     * @return Image
     */
    public static HttpEntity downloadEntityByMsgID(String url,String msgId,String type,Map<String, String> headerMap,boolean redirect){

        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("msgid", String.valueOf(msgId)));
        if (StringUtils.isNotEmpty(type)){
            params.add(new BasicNameValuePair("type", type));
        }
        params.add(new BasicNameValuePair("skey", (String) Core.getLoginInfoMap().get("skey")));
        HttpEntity entity = HttpUtil.doGet(url, params, redirect, headerMap);
        return entity;
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
     * 获取缩略图文件保存文章
     *
     * @param msg 接收的消息对象
     * @return {@code String} 消息资源文件保存路径
     * {@code null} 获取失败或无需下载的资源
     * @return 路径
     */
    public static String getDownloadThumImgPath(AddMsgList msg, String fileName, String ext) {

        String downloadFilePath = getDownloadFilePath(msg, fileName, ext);
        downloadFilePath = downloadFilePath + "_slave.gif";

        return downloadFilePath;
    }

    /**
     * 获取消息资源文件保存路径
     *
     * @param msg 接收的消息对象
     * @return {@code String} 消息资源文件保存路径
     * {@code null} 获取失败或无需下载的资源
     * @return 路径
     */
    public static String getDownloadFilePath(AddMsgList msg, String fileName, String ext) {
        //发消息的用户或群名称
        String username = ContactsTools.getContactDisplayNameByUserName(msg.getFromUserName());
        username = replace(username);

        //群成员名称
        String groupUsername = "";
        if (msg.isGroupMsg() && msg.getMemberName() != null) {
            groupUsername = ContactsTools.getMemberDisplayNameOfGroup(msg.getFromUserName(),msg.getMemberName());
        }
        groupUsername = groupUsername == null ? "" : replace(groupUsername);
        //basePath/消息类型/用户或群名/群成员名称/日期/文件名-日期.类型
        String path = WECHAT_CONFIGURATION.getBasePath() + File.separator + msg.getType()
                + File.separator + username
                + File.separator + groupUsername
                + File.separator + DateUtils.formatDate(new Date(), "yyyy-MM-dd")
                + File.separator;

        fileName = fileName.substring(0,fileName.length()-ext.length())+ "-"
                + DateUtils.formatDate(new Date(), "yyyy-MM-dd-HH-mm-ss")
                + ext;


        return path + fileName;
    }

    /**
     * 等待下载完成
     * @param path 文件路径
     */
    public static void awaitDownload(String path){
        Boolean aBoolean = DownloadTools.FILE_DOWNLOAD_STATUS.get(path);
        while (aBoolean != null && !aBoolean) {
            SleepUtils.sleep(100);
            aBoolean = DownloadTools.FILE_DOWNLOAD_STATUS.get(path);
        }
        DownloadTools.FILE_DOWNLOAD_STATUS.remove(path);
    }
}
