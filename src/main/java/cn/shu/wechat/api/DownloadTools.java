package cn.shu.wechat.api;

import cn.shu.wechat.configuration.WechatConfiguration;
import cn.shu.wechat.constant.WxRespConstant;
import cn.shu.wechat.constant.WxURLEnum;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.dto.request.msg.url.WXMsgUrl;
import cn.shu.wechat.dto.response.sync.AddMsgList;
import cn.shu.wechat.utils.DateUtils;
import cn.shu.wechat.utils.HttpUtil;
import cn.shu.wechat.utils.MD5Util;
import cn.shu.wechat.utils.SpringContextHolder;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import static cn.shu.wechat.utils.HttpUtil.getProgressBytesBodyHandler;

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
     * 配置文件
     */
    private final static WechatConfiguration WECHAT_CONFIGURATION = SpringContextHolder.getBean(WechatConfiguration.class);



    /**
     * 处理下载任务
     *
     * @param msg 消息对象
     * @author SXS
     * @date 2017年4月21日 下午11:00:25
     */
    public static void getDownloadFn(AddMsgList msg, BlockingQueue<Long> process) throws IOException, InterruptedException {
        Map<String, String> headerMap = new HashMap<String, String>();
        Map<String,String> params = new HashMap<>();
        WxRespConstant.WXReceiveMsgCodeEnum msgTypeEnum = WxRespConstant.WXReceiveMsgCodeEnum.getByCode(msg.getMsgType());

        String url;
        HttpResponse.BodyHandler<Void> voidBodyHandler = getProgressBytesBodyHandler(process, msg.getFilePath());
        switch (msgTypeEnum) {
            case MSGTYPE_IMAGE:
                url = String.format(WxURLEnum.WEB_WX_GET_MSG_IMG.getUrl(), (String) Core.getLoginResultData().getUrl());
                 downloadEntityByMsgID(
                        url,String.valueOf(msg.getNewMsgId())
                        ,null,headerMap, voidBodyHandler);
                break;
            case MSGTYPE_EMOTICON:
                url = String.format(WxURLEnum.WEB_WX_GET_MSG_IMG.getUrl(), (String)Core.getLoginResultData().getUrl());
                 downloadEntityByMsgID(url,String.valueOf(msg.getNewMsgId()), WXMsgUrl.BIG_TYPE,headerMap, voidBodyHandler);
                break;
            case MSGTYPE_VOICE:
                url = String.format(WxURLEnum.WEB_WX_GET_VOICE.getUrl(), (String)Core.getLoginResultData().getUrl());
                 downloadEntityByMsgID(
                        url,String.valueOf(msg.getNewMsgId())
                        ,null,headerMap, voidBodyHandler);
                break;
            case MSGTYPE_VIDEO:
                headerMap.put("Range", "bytes=0-");
                url = String.format(WxURLEnum.WEB_WX_GET_VIEDO.getUrl(), (String)Core.getLoginResultData().getUrl());
                downloadEntityByMsgID(
                        url,String.valueOf(msg.getNewMsgId())
                        ,null,headerMap, voidBodyHandler);
                break;
            case MSGTYPE_APP:
               // headerMap.put("Range", "bytes=0-");
                url = String.format(WxURLEnum.WEB_WX_GET_MEDIA.getUrl(), (String)  Core.getLoginResultData().getFileUrl());
                params.put("sender", msg.getFromUserName());
                params.put("mediaid", msg.getMediaId());
                params.put("filename", msg.getFileName());
               // params.add(new BasicNameValuePair("msgid", String.valueOf(msg.getNewMsgId())));
               // params.add(new BasicNameValuePair("skey", (String)  Core.getLoginResultData().getBaseRequest().getSKey()));
                HttpUtil.doGet(url, params, headerMap,true, voidBodyHandler);
                break;
            case MSGTYPE_MAP:
                url = msg.getContent().substring(msg.getContent().indexOf(":<br/>") + ":<br/>".length());
                url = WxURLEnum.BASE_URL.getUrl() + url;
                HttpUtil.doGet(url, null, null,false, voidBodyHandler);
                break;
            default:
                break;
        }
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
    public static void downloadFileByMsgId(String msgId, String path, BlockingQueue<Long> progress) throws Exception {
        String url = String.format(WxURLEnum.WEB_WX_GET_MSG_IMG.getUrl(), Core.getLoginResultData().getUrl());
        HttpResponse.BodyHandler<Void> voidBodyHandler = getProgressBytesBodyHandler(progress, path);
        downloadEntityByMsgID(url, msgId, WXMsgUrl.SLAVE_TYPE, null, voidBodyHandler);

    }

    /**
     * entity 2 file
     *
     * @param entity
     * @param path
     */
//    private static void entity2File(HttpEntity entity, String path, BlockingQueue<Long> progress) throws Exception {
//        if (entity == null) {
//            throw new Exception("response entity is null：" + path);
//        }
//
//        File file = new File(path);
//        if (!file.exists()) {
//            File parentFile = file.getParentFile();
//            if (!parentFile.exists()) {
//                if (!parentFile.mkdirs()) {
//                    log.warn("创建目录失败：{}", parentFile.getAbsolutePath());
//                    }
//                }
//                if (!file.createNewFile()) {
//                    log.warn("创建文件失败：{}", path);
//                }
//
//            }
//            try (InputStream in = entity.getContent();
//                 OutputStream out = Files.newOutputStream(file.toPath())) {
//
//                int size = 1024 * 1024;
//                byte[] data = new byte[size];
//                int readLen = 0;
//                long readed = 0;
//                while ((readLen = in.read(data, 0, size)) > 0) {
//                    out.write(data, 0, readLen);
//                    readed = readed + readLen;
//                    progress.offer(readed);
//                }
//                out.flush();
//                progress.offer(-100L);
//            }
//
//    }
    /**
     * entity to image
     * @param entity entity
     * @return
     */
    private static BufferedImage entity2Image(byte[] entity) throws IOException {
        if (entity == null || entity.length == 0) {
            throw new IOException("entity is null or empty");
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(entity)) {
            return ImageIO.read(bais);
        }
    }
    /**
     * entity to ImageIcon
     * @param bytes entity
     * @return
     */
    private static ImageIcon entity2ImageIcon(byte[] bytes){
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return new ImageIcon(bytes);
    }
    /**
     * 下载微信大头像
     *
     * @param relativeUrl 微信头像地址
     * @param userName    用户名
     * @return 下载成功头像保存路径
     * 下载失败 ""
     */
    public static String downloadBigHeadImg(String relativeUrl, String userName) throws IOException, InterruptedException {
        //获取远端对象字节数组
        String url = String.format(WxURLEnum.WEB_WX_GET_HEAD_IMAGE_BIG.getUrl(), relativeUrl);
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
    private static String downloadHeadImgThum(String relativeUrl, String userName) throws IOException, InterruptedException {

        //获取远端对象字节数组
        String url = String.format(WxURLEnum.WEB_WX_GET_HEAD_IMAGE_THUM.getUrl(), relativeUrl);
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
    private static String downloadHeadImg(String url, String userName) throws IOException, InterruptedException {
        byte[] bytes = HttpUtil.doGet(url, null, null, false, HttpResponse.BodyHandlers.ofByteArray());
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


        Path saveDir = Paths.get(WECHAT_CONFIGURATION.getBasePath(), "headimg", remarkNameByUserName);
        Path savePath = saveDir.resolve(md5Str + ".jpg");

        try {
            if (Files.notExists(saveDir)) {
                Files.createDirectories(saveDir);
            }
            //头像文件已存在，md5及大小相等
            if (bytes != null && Files.exists(savePath) && Files.size(savePath) == bytes.length) {
                return savePath.toString();
            }

            // 写文件，使用try-with-resources保证关闭
            try (OutputStream out = Files.newOutputStream(savePath)) {
                if (bytes != null) {
                    out.write(bytes);
                }
                out.flush();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return savePath.toString();
    }

    /**
     * 下载头像缩略图
     *
     * @param relativeUrl 微信头像地址
     * @return Image对象
     */
    public static Image downloadHeadImgByRelativeUrl(String relativeUrl) throws IOException, InterruptedException {
        String url = String.format(WxURLEnum.WEB_WX_GET_HEAD_IMAGE_THUM.getUrl(), relativeUrl);
        return downloadImgByAbsoluteUrl(url);

    }

    /**
     * 下载图片
     *
     * @param url 图片地址
     * @return Image对象
     */
    private static BufferedImage downloadImgByAbsoluteUrl(String url) throws IOException, InterruptedException {
        byte[] bytes = HttpUtil.doGet(url, null, null, true, HttpResponse.BodyHandlers.ofByteArray());

        return entity2Image(bytes);
    }


    /**
     * 下载图片根据消息id
     * @param msgId 消息ID
     * @param type 类型
     * @return Image
     */
    public static BufferedImage downloadImgByMsgID(String msgId,String type) throws IOException, InterruptedException {
        return entity2Image(downloadImgEntityByMsgID(msgId, type, HttpResponse.BodyHandlers.ofByteArray()));
    }


    /**
     * 下载图片根据消息id
     * @param msgId 消息ID
     * @param type 类型
     * @return Image
     */
    public static<R> R  downloadImgEntityByMsgID(String msgId, String type,HttpResponse.BodyHandler<R> bodyHandler) throws IOException, InterruptedException {
        String url = String.format(WxURLEnum.WEB_WX_GET_MSG_IMG.getUrl(), (String)Core.getLoginResultData().getUrl());
        return downloadEntityByMsgID(
                url, String.valueOf(msgId), type
                , null, bodyHandler);
    }
    /**
     * 下载资源根据消息id
     *
     * @param msgId 消息ID
     * @param type  类型
     * @return Image
     */
    private static<R> R downloadEntityByMsgID(String url, String msgId, String type, Map<String, String> headerMap, HttpResponse.BodyHandler<R> bodyHandler) throws IOException, InterruptedException {

        Map<String,String> params = new HashMap<>();
        params.put("msgid", String.valueOf(msgId));
        if (StringUtils.isNotEmpty(type)){
            params.put("type", type);
        }
        params.put("skey", Core.getLoginResultData().getBaseRequest().getSKey());
        return HttpUtil.doGet(url, params, headerMap, bodyHandler);
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
                + File.separator + DateUtils.format(System.currentTimeMillis(), "yyyy-MM-dd")
                + File.separator;

        fileName = fileName.substring(0,fileName.length()-ext.length())+ "-"
                + DateUtils.format(System.currentTimeMillis(), "yyyy-MM-dd-HH-mm-ss")
                + ext;


        return path + fileName;
    }


    /**
     * 根据UserName下载头像
     * @param username 用户名
     * @return 头像
     */
    public static Image downloadHeadImgByUserName(String username) throws IOException, InterruptedException {
        //获取远端对象字节数组
        String url = String.format(WxURLEnum.WEB_WX_GET_HEAD_IMAGE.getUrl(),
                Core.getLoginResultData().getUrl(),System.currentTimeMillis(),username);
        return downloadImgByAbsoluteUrl(url);
    }
}
