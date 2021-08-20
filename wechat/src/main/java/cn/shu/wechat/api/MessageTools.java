package cn.shu.wechat.api;


import cn.shu.wechat.beans.msg.send.*;
import cn.shu.wechat.beans.msg.sync.AddMsgList;
import cn.shu.wechat.beans.msg.sync.RecommendInfo;
import cn.shu.wechat.beans.pojo.Message;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.enums.*;
import cn.shu.wechat.exception.WebWXException;
import cn.shu.wechat.mapper.MessageMapper;
import cn.shu.wechat.swing.tasks.UploadTaskCallback;
import cn.shu.wechat.swing.utils.ImageUtil;
import cn.shu.wechat.swing.utils.MimeTypeUtil;
import cn.shu.wechat.utils.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import me.xuxiaoxiao.xtools.common.XTools;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

/**
 * 消息处理类
 *
 * @author SXS
 * @version 1.1
 * @date 创建时间：2017年4月23日 下午2:30:37
 */
@Log4j2
@Component

public class MessageTools {
    /**
     * 微信上传最大文件大小
     */
    public static final long maxFileSize = 1024 * 1024 * 20;
    /**
     * 本次登录以来上传文件的数量
     */
    private static int fileCount = 0;

    /**
     * 消息Mapper
     */
    private static MessageMapper messageMapper;

    /**
     * 根据指定类型发送消息
     *
     * @param messages    消息列表
     * @param callback 文件上传进度回调
     */
    public static WebWXSendMsgResponse sendMsgByUserId(List<Message> messages,UploadTaskCallback callback) {
        if (messages == null){
            return WebWXSendMsgResponse.error("is null");
        }
        WebWXSendMsgResponse sendMsgResponse = null;
        for (Message message : messages) {
            String toUserName = message.getToUsername();
            if (StringUtils.isEmpty(toUserName)) {
                log.error("消息接收者为空：{}", message);
                continue;
            }

            try {
                String content = XmlStreamUtil.formatXml(message.getContent());
                WXReceiveMsgCodeEnum byCode = WXReceiveMsgCodeEnum.getByCode(message.getMsgType());
                switch (byCode) {
                    case MSGTYPE_IMAGE:
                        sendMsgResponse = sendPicMsgByUserId(toUserName, message.getFilePath(), content, callback);
                        break;
                    case MSGTYPE_TEXT:
                        sendMsgResponse = sendTextMsgByUserId(content, toUserName);
                        break;
                    case MSGTYPE_VIDEO:
                        sendMsgResponse = sendVideoMsgByUserId(toUserName, message.getFilePath(), content, callback);
                        break;
                    case MSGTYPE_EMOTICON:
                        sendMsgResponse = sendEmotionMsgByUserId(toUserName, message.getFilePath(), content);
                        break;
                    case MSGTYPE_SHARECARD:
                        sendMsgResponse = sendCardMsgByUserId(toUserName, content);
                        break;
                    default:
                        //其他消息发送文件
                        sendMsgResponse = sendAppMsgByUserId(toUserName, message.getFilePath(), content, callback);
                }
                log.info(LogUtil.printToMeg(byCode.getDesc(), toUserName, StringUtils.isEmpty(message.getFilePath()) ? content : message.getFilePath()));
                if (sendMsgResponse == null) {
                    log.error("发送消息失败：{}", message);
                    return WebWXSendMsgResponse.error("null");
                } else if (sendMsgResponse.getBaseResponse().getRet() != 0) {
                    log.error("发送消息失败：{},{}", sendMsgResponse.getBaseResponse().getErrMsg(), message);
                    return sendMsgResponse;
                }
                //存储数据库
                 storeMsgToDB(message, sendMsgResponse, toUserName);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("发送消息失败：{}", e.getMessage());
                continue;
            }
        }
        return sendMsgResponse;
    }

    /**
     * 根据指定类型发送消息
     *
     * @param message    消息列表
     */
    public static WebWXSendMsgResponse sendMsgByUserId(Message message) {
        ArrayList<Message> messages = new ArrayList<>();
        messages.add(message);
        return sendMsgByUserId(messages);
    }


    /**
     * 根据指定类型发送消息
     * @param message 消息列表
     * @param callback 发送进度回调
     * @return
     */
    public static WebWXSendMsgResponse sendMsgByUserId(Message message, UploadTaskCallback callback) {
        ArrayList<Message> messages = new ArrayList<>();
        messages.add(message);
        return sendMsgByUserId(messages, callback);
    }

    /**
     * 保存发送的消息到数据库
     *
     * @param toUserName      消息接收者
     * @param results         发送的消息
     * @param sendMsgResponse 发送成功响应信息
     */
    private static List<Message> storeMsgToDB(List<Message> results, WebWXSendMsgResponse sendMsgResponse, String toUserName) {
        for (Message message : results) {
            boolean isToSelf = toUserName.endsWith(Core.getUserName());
            message.setIsSend(true);
            message.setProgress(100);
            message.setMsgJson(JSON.toJSONString(message));
            message.setResponse(JSON.toJSONString(sendMsgResponse));
            if (message.getMsgType() == null) {
                message.setAppMsgType(0);
            }
            message.setCreateTime(DateUtils.getCurrDateString(DateUtils.YYYY_MM_DD_HH_MM_SS));
            message.setFromNickname(Core.getNickName());
            message.setFromRemarkname(Core.getNickName());
            message.setFromUsername(Core.getUserName());
            message.setToNickname(isToSelf ? Core.getNickName() : ContactsTools.getContactNickNameByUserName(toUserName));
            message.setToRemarkname(isToSelf ? Core.getNickName() : ContactsTools.getContactRemarkNameByUserName(toUserName));
            message.setId(message.getId() == null ? UUID.randomUUID().toString().replace("-", "") : message.getId());
            message.setMsgDesc(message.getMsgType() + "");
            if (message.getPlaintext() == null) {
                message.setPlaintext(message.getContent());
            }
            message.setTimestamp(System.currentTimeMillis());
        }
        try {
            int insert = messageMapper.batchInsert(results);
            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    /**
     * 保存发送的消息到数据库
     *
     * @param toUserName      消息接收者
     * @param message         发送的消息
     * @param sendMsgResponse 发送成功响应信息
     */
    private static List<Message> storeMsgToDB(Message message, WebWXSendMsgResponse sendMsgResponse, String toUserName) {
        ArrayList<Message> messages = new ArrayList<>();
        messages.add(message);
        return storeMsgToDB(messages,sendMsgResponse,toUserName);
    }
    /**
     * 根据指定类型发送消息
     *
     * @param messages   消息列表
     */
    public static WebWXSendMsgResponse sendMsgByUserId(List<Message> messages) {
        return sendMsgByUserId(messages,null);
    }

    /**
     * @param filePath     文件路径
     * @param fromUserName 该消息发送者
     * @param toUserName   消息接收者
     * @param callback 上传进度回调
     * @return {@link WebWXSendMsgResponse}
     */
    private static WebWXUploadMediaResponse webWxUploadMedia(String filePath, String fromUserName, String toUserName, UploadTaskCallback callback) throws WebWXException, IOException {

        //一次上传的文件最大1M
        long singleFileMaxSize = 1048576L;
        File file = new File(filePath);
        //等待另一线程的下载该资源完成
        DownloadTools.awaitDownload(filePath);
        if (!file.exists()) {
            throw new WebWXException("待上传文件不存在：" + filePath);
        }
        long fileSize = file.length();
        if (fileSize <= 0) {
            throw new WebWXException("文件大小为：" + fileSize + "," + filePath);
        }
        String fileType = WeChatTool.getFileType(file);

        //大于20M不能发送需要压缩
        if (fileSize > maxFileSize) {

            switch (fileType) {
                case "video":
                    //视频超过1M，则压缩到1M
                    int bitRate = 800000;
                    String name = file.getName();
                    while (fileSize > maxFileSize) {
                        file = MediaUtil.compressionVideo(file, "/compression/" + name + ".mp4", bitRate);
                        fileSize = file.length();
                        bitRate = (bitRate / 2);
                    }
                    break;
                case "pic":
                    //图片超过1M，则压缩到1M
                    file = MediaUtil.compressImage(file, maxFileSize);
                    break;
                default:
                    //其它文件压缩成zip
                    break;

            }
        }
        if (file.length() > maxFileSize) {
            throw new WebWXException("不能上传大于20M的文件：" + filePath);
        }
        int fileId = fileCount++;
        String fileMime = MimeTypeUtil.getMimeByPath(file.getAbsolutePath());
        if (fileMime ==null){
            fileMime ="application";
        }
        String lastModifyFileDate = new SimpleDateFormat("yyyy MM dd HH:mm:ss").format(file.lastModified());
        String passTicket = (String) Core.getLoginInfoMap().get("pass_ticket");
        if (StringUtils.isEmpty(passTicket)) {
            passTicket = "undefined";
        }
        String clientMediaId = System.currentTimeMillis() + String.valueOf(new Random().nextLong()).substring(0, 4);
        String webWXDataTicket = MyHttpClient.getCookie("webwx_data_ticket");
        Map<String, Object> paramMap = Core.getParamMap();
        paramMap.put("UploadType", 2);
        paramMap.put("BaseRequest", Core.getParamMap().get("BaseRequest"));
        paramMap.put("ClientMediaId", clientMediaId);
        paramMap.put("TotalLen", file.length());
        paramMap.put("StartPos", 0);
        paramMap.put("DataLen", file.length());
        paramMap.put("MediaType", 4);
        paramMap.put("FromUserName", fromUserName);
        paramMap.put("ToUserName", toUserName);
        paramMap.put("FileMd5", XTools.md5(file));
        String result = null;
        String url = String.format(URLEnum.WEB_WX_UPLOAD_MEDIA.getUrl(), Core.getLoginInfoMap().get(StorageLoginInfoEnum.fileUrl.getKey()));
        if (file.length() <= singleFileMaxSize) {
            //小于1M发送方式
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addTextBody("id", "WU_FILE_0", ContentType.TEXT_PLAIN);
            builder.addTextBody("name", filePath, ContentType.TEXT_PLAIN);
            builder.addTextBody("type", fileMime, ContentType.TEXT_PLAIN);
            builder.addTextBody("lastModifieDate", lastModifyFileDate, ContentType.TEXT_PLAIN);
            builder.addTextBody("size", String.valueOf(fileSize), ContentType.TEXT_PLAIN);
            builder.addTextBody("mediatype", fileType, ContentType.TEXT_PLAIN);
            builder.addTextBody("uploadmediarequest", JSON.toJSONString(paramMap), ContentType.TEXT_PLAIN);
            builder.addTextBody("webwx_data_ticket", webWXDataTicket, ContentType.TEXT_PLAIN);
            builder.addTextBody("pass_ticket", passTicket, ContentType.TEXT_PLAIN);
            builder.addBinaryBody("filename", file, ContentType.create(fileMime), filePath);
            HttpEntity reqEntity = builder.build();
            HttpEntity resultEntity = MyHttpClient.doPostFile(url, reqEntity);
            result = EntityUtils.toString(resultEntity, Consts.UTF_8);
            if (callback != null) {
                callback.onTaskSuccess(99, 100);
            }

        } else {
            //大于1M发送方式
            //最后一个分片上传后返回msgid
            ArrayList<String> partFilePathList = FileSplitAndMergeUtil.splitFile1(file.getAbsolutePath());
            for (int i = 0; i < partFilePathList.size(); i++) {
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                builder.addTextBody("id", String.format("WU_FILE_%d", fileId), ContentType.TEXT_PLAIN);
                builder.addTextBody("name", filePath, ContentType.TEXT_PLAIN);
                builder.addTextBody("type", fileMime, ContentType.TEXT_PLAIN);
                builder.addTextBody("lastModifieDate", lastModifyFileDate, ContentType.TEXT_PLAIN);
                builder.addTextBody("size", String.valueOf(fileSize), ContentType.TEXT_PLAIN);
                builder.addTextBody("mediatype", fileType, ContentType.TEXT_PLAIN);
                builder.addTextBody("uploadmediarequest", JSON.toJSONString(paramMap), ContentType.TEXT_PLAIN);
                builder.addTextBody("webwx_data_ticket", webWXDataTicket, ContentType.TEXT_PLAIN);
                builder.addTextBody("pass_ticket", passTicket, ContentType.TEXT_PLAIN);
                builder.addTextBody("chunks", partFilePathList.size() + "", ContentType.TEXT_PLAIN);
                builder.addTextBody("chunk", i + "", ContentType.TEXT_PLAIN);
                builder.addBinaryBody("filename", new File(partFilePathList.get(i)), ContentType.create(fileMime), filePath);
                HttpEntity reqEntity = builder.build();
                HttpEntity resultEntity = MyHttpClient.doPostFile(url, reqEntity);
                if (i == partFilePathList.size() - 1) {
                    result = EntityUtils.toString(resultEntity, Consts.UTF_8);

                } else {
                    //不关闭下次执行会卡住
                    resultEntity.getContent().close();
                }
                if (callback != null) {
                    callback.onTaskSuccess(i + 1, partFilePathList.size() + 1);
                }
            }
            //删除分片文件
            FileSplitAndMergeUtil.deletePartFile(partFilePathList);
        }

        WebWXUploadMediaResponse webWXUploadMediaResponse = JSON.parseObject(result, WebWXUploadMediaResponse.class);
        if (webWXUploadMediaResponse == null
                || StringUtils.isEmpty(webWXUploadMediaResponse.getMediaId())) {
            throw new WebWXException("上传文件返回MediaId为空");
        }
        return webWXUploadMediaResponse;
    }

    /**
     * 根据UserName发送文本消息
     *
     * @param toUserName 消息接收者UserName
     * @param content    消息内容，content可能包含资源文件的id等信息，可直接使用
     * @return {@link WebWXSendMsgResponse}
     * @author SXS
     * @date 2017年5月4日 下午11:17:38
     */
    private static WebWXSendMsgResponse sendTextMsgByUserId(String content, String toUserName) throws IOException {
        String url = String.format(URLEnum.WEB_WX_SEND_MSG.getUrl(), Core.getLoginInfoMap().get(StorageLoginInfoEnum.url.getKey()));
        WebWXSendMsgRequest msgRequest = new WebWXSendMsgRequest();
        WebWXSendingMsg textMsg = new WebWXSendingTextMsg();
        textMsg.Content = content;
        textMsg.ToUserName = toUserName;
        msgRequest.Msg = textMsg;
        return sendMsg(msgRequest, url);
    }

    /**
     * 根据UserName发送名片消息
     *
     * @param toUserName 消息接收者UserName
     * @param content    消息内容，content可能包含资源文件的id等信息，可直接使用
     * @return {@link WebWXSendMsgResponse}
     * @author SXS
     * @date 2017年5月4日 下午11:17:38
     */
    private static WebWXSendMsgResponse sendCardMsgByUserId(String toUserName, String content) throws IOException {
        String url = String.format(URLEnum.WEB_WX_SEND_MSG.getUrl(), Core.getLoginInfoMap().get(StorageLoginInfoEnum.url.getKey()));
        WebWXSendMsgRequest msgRequest = new WebWXSendMsgRequest();
        WebWXSendingCardMsg textMsg = new WebWXSendingCardMsg();
        textMsg.Content = content;
        textMsg.ToUserName = toUserName;
        msgRequest.Msg = textMsg;
        return sendMsg(msgRequest, url);
    }

    @Resource
    public void setMessageMapper(MessageMapper messageMapper) {
        MessageTools.messageMapper = messageMapper;
    }


    /**
     * 根据用户id发送图片消息
     *
     * @param userId   消息接收者UserName
     * @param filePath 待上传文件路径 content为空时使用上传
     * @param content  消息内容，content可能包含资源文件的id等信息，可直接使用
     * @return {@link WebWXSendMsgResponse}
     * @author SXS
     * @date 2017年5月7日 下午10:34:24
     */
    private static WebWXSendMsgResponse sendPicMsgByUserId(String userId, String filePath, String content,UploadTaskCallback callback) throws WebWXException, IOException {
        String mediaId = "";
        if (StringUtils.isEmpty(content) || !content.startsWith("@")) {
            WebWXUploadMediaResponse resp = webWxUploadMedia(filePath, Core.getUserName(), userId, callback);
            mediaId = resp.getMediaId();
            content = "";
        }
        String url = String.format(URLEnum.WEB_WX_SEND_PIC_MSG.getUrl(), Core.getLoginInfoMap().get(StorageLoginInfoEnum.url.getKey()),
                Core.getLoginInfoMap().get("pass_ticket"));

        WebWXSendMsgRequest msgRequest = new WebWXSendMsgRequest();
        WebWXSendingPicMsg textMsg = new WebWXSendingPicMsg();
        textMsg.MediaId = mediaId;
        textMsg.ToUserName = userId;
        textMsg.Content = content;
        msgRequest.Msg = textMsg;
        WebWXSendMsgResponse webWXSendMsgResponse = sendMsg(msgRequest, url);
        if (callback!=null){
            callback.onTaskSuccess(100,100);
        }

        return webWXSendMsgResponse;

    }

    /**
     * 根据用户id发送表情消息
     * <p>
     * content里面有表情md5可直接发送
     * 没有则通过filepath上传使用mediaid发送
     *
     * @param userId   消息接收者UserName
     * @param filePath 待上传文件路径 content为空时使用上传
     * @param content  消息内容，content可能包含资源文件的id等信息，可直接使用
     * @return {@link WebWXSendMsgResponse}
     * @author SXS
     * @date 2017年5月7日 下午10:34:24
     */
    private static WebWXSendMsgResponse sendEmotionMsgByUserId(String userId, String filePath, String content) throws WebWXException, IOException {

        String url = String.format(URLEnum.WEB_WX_SEND_EMOTION_MSG.getUrl(), Core.getLoginInfoMap().get(StorageLoginInfoEnum.url.getKey()));

        WebWXSendMsgRequest msgRequest = new WebWXSendMsgRequest();
        WebWXSendingEmotionMsg textMsg = new WebWXSendingEmotionMsg();
        String md5 = null;
        try {
            if (StringUtils.isNotEmpty(content)) {
                Map<String, Object> stringObjectMap = XmlStreamUtil.toMap(content);
                md5 = stringObjectMap.get("msg.emoji.attr.md5").toString();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        if (md5 == null) {
            WebWXUploadMediaResponse resp = webWxUploadMedia(filePath, Core.getUserName(), userId, null);
            textMsg.MediaId = resp.getMediaId();
            textMsg.EmojiFlag = 2;
        } else {
            textMsg.EMoticonMd5 = md5;
        }
        textMsg.ToUserName = userId;
        msgRequest.Scene = 2;
        msgRequest.Msg = textMsg;
        return sendMsg(msgRequest, url);

    }

    /**
     * 根据用户id发送撤回消息
     *
     * @param userId      消息接收者
     * @param clientMsgId 发送消息返回的 LocalID {@link WebWXSendMsgResponse}
     * @param svrMsgId    发送消息返回的 MsgId {@link WebWXSendMsgResponse}
     * @return {@code true} 发送成功 {@code false} 发送失败
     * @author SXS
     * @date 201714年5月7日 下午10:34:24
     */
    public static boolean sendRevokeMsgByUserId(String userId, String clientMsgId, String svrMsgId) {

        String url = String.format(URLEnum.WEB_WX_REVOKE_MSG.getUrl()
                , URLEnum.BASE_URL.getUrl());

        WebWXSendingRevokeMsg webWXSendingRevokeMsg = new WebWXSendingRevokeMsg();
        webWXSendingRevokeMsg.ClientMsgId = clientMsgId;
        webWXSendingRevokeMsg.SvrMsgId = svrMsgId;
        webWXSendingRevokeMsg.ToUserName = userId;
        String paramStr = JSON.toJSONString(webWXSendingRevokeMsg);
        HttpEntity entity = MyHttpClient.doPost(url, paramStr);
        if (entity != null) {
            try {
                String result = EntityUtils.toString(entity, Consts.UTF_8);
                return JSON.parseObject(result).getJSONObject("BaseResponse").getInteger("Ret") == 0;
            } catch (Exception e) {
                log.error("webWxSendMsgImg 错误： {}", e.getMessage());
            }
        }
        return false;
    }

    /**
     * 根据用户id发送视频消息
     *
     * @param userId   消息接收者UserName
     * @param filePath 待上传文件路径 content为空时使用上传
     * @param content  消息内容，content可能包含资源文件的id等信息，可直接使用
     * @param callback
     * @return {@link WebWXSendMsgResponse}
     * @author SXS
     * @date 201714年5月7日 下午10:34:24
     */
    private static WebWXSendMsgResponse sendVideoMsgByUserId(String userId, String filePath, String content, UploadTaskCallback callback) throws WebWXException, IOException {
        String mediaId = "";
        if (StringUtils.isEmpty(content) || !content.startsWith("@")) {
            WebWXUploadMediaResponse resp = webWxUploadMedia(filePath, Core.getUserName(), userId, callback);
            mediaId = resp.getMediaId();
            content = "";
        }
        String url = String.format(URLEnum.WEB_WX_SEND_VIDEO_MSG.getUrl(), Core.getLoginInfoMap().get(StorageLoginInfoEnum.url.getKey()),
                Core.getLoginInfoMap().get(StorageLoginInfoEnum.pass_ticket.getKey()));
        WebWXSendMsgRequest msgRequest = new WebWXSendMsgRequest();
        WebWXSendingVideoMsg textMsg = new WebWXSendingVideoMsg();
        textMsg.MediaId = mediaId;
        textMsg.ToUserName = userId;
        textMsg.Content = content;
        msgRequest.Msg = textMsg;
        WebWXSendMsgResponse webWXSendMsgResponse = sendMsg(msgRequest, url);
        if (callback!=null){
            callback.onTaskSuccess(100,100);
        }
        return webWXSendMsgResponse;


    }


    /**
     * 发送APP消息
     *
     * @param userId   消息接收者UserName
     * @param filePath 待上传文件路径 content为空时使用上传
     * @param content  消息内容，content可能包含资源文件的id等信息，可直接使用
     * @param callback 发送成功回调
     * @return {@link WebWXSendMsgResponse}
     * @author SXS
     * @date 2017年5月10日 上午12:21:28
     */
    private static WebWXSendMsgResponse sendAppMsgByUserId(String userId, String filePath, String content, UploadTaskCallback callback) throws IOException, WebWXException {
        String url = String.format(URLEnum.WEB_WX_SEND_APP_MSG.getUrl(), Core.getLoginInfoMap().get(StorageLoginInfoEnum.url.getKey()),
                Core.getLoginInfoMap().get("pass_ticket"));

        if (StringUtils.isEmpty(content)) {
            String title = new File(filePath).getName();
            String fileext = title.split("\\.")[1];
            if (fileext == null) {
                fileext = "";
            }
            WebWXUploadMediaResponse webWXUploadMediaResponse = webWxUploadMedia(filePath, Core.getUserName(), userId, callback);
            long totallen = webWXUploadMediaResponse.getStartPos();
            String attachid = webWXUploadMediaResponse.getMediaId();
            content = "<appmsg appid='wxeb7ec651dd0aefa9' sdkver=''>" +
                    "<title>" + title + "</title><des></des><action></action><type>6</type><content></content><url></url><lowurl></lowurl>"
                    + "<appattach><totallen>" + totallen + "</totallen>" +
                    "<attachid>" + attachid + "</attachid>" +
                    "<fileext>" + fileext + "</fileext>" +
                    "</appattach><extinfo></extinfo></appmsg>";
        } else {
            Map<String, Object> stringObjectMap = XmlStreamUtil.toMap(content);
            Object attachid_ = stringObjectMap.get("msg.appmsg.appattach.attachid");
            Object totallen = stringObjectMap.get("msg.appmsg.appattach.totallen");
            Object fileext = stringObjectMap.get("msg.appmsg.appattach.fileext");
            Object title = stringObjectMap.get("msg.appmsg.title");
            content = "<appmsg appid='wxeb7ec651dd0aefa9' sdkver=''>" +
                    "<title>" + title + "</title><des></des><action></action><type>6</type><content></content><url></url><lowurl></lowurl>"
                    + "<appattach><totallen>" + totallen + "</totallen>" +
                    "<attachid>" + attachid_ + "</attachid>" +
                    "<fileext>" + fileext + "</fileext>" +
                    "</appattach><extinfo></extinfo></appmsg>";

        }
        WebWXSendMsgRequest msgRequest = new WebWXSendMsgRequest();
        WebWXSendingAppMsg textMsg = new WebWXSendingAppMsg();
        textMsg.ToUserName = userId;
        textMsg.Content = content;
        msgRequest.Msg = textMsg;
        WebWXSendMsgResponse webWXSendMsgResponse = sendMsg(msgRequest, url);
        if (callback!=null){
            callback.onTaskSuccess(100,100);
        }
        return webWXSendMsgResponse;
    }
    /**
     * 发送APP消息
     *
     * @param userId   消息接收者UserName
     * @param webWXUploadMediaResponse 上传文件信息
     * @return {@link WebWXSendMsgResponse}
     * @author SXS
     * @date 2017年5月10日 上午12:21:28
     */
    private static WebWXSendMsgResponse sendAppMsgByUserId(String userId, WebWXUploadMediaResponse webWXUploadMediaResponse,String filePath) throws IOException, WebWXException {
        String url = String.format(URLEnum.WEB_WX_SEND_APP_MSG.getUrl(), Core.getLoginInfoMap().get(StorageLoginInfoEnum.url.getKey()),
                Core.getLoginInfoMap().get("pass_ticket"));
            String title = new File(filePath).getName();
            String fileext = title.split("\\.")[1];
            if (fileext == null) {
                fileext = "";
            }
            long totallen = webWXUploadMediaResponse.getStartPos();
            String attachid = webWXUploadMediaResponse.getMediaId();
            String   content = "<appmsg appid='wxeb7ec651dd0aefa9' sdkver=''>" +
                    "<title>" + title + "</title><des></des><action></action><type>6</type><content></content><url></url><lowurl></lowurl>"
                    + "<appattach><totallen>" + totallen + "</totallen>" +
                    "<attachid>" + attachid + "</attachid>" +
                    "<fileext>" + fileext + "</fileext>" +
                    "</appattach><extinfo></extinfo></appmsg>";
        WebWXSendMsgRequest msgRequest = new WebWXSendMsgRequest();
        WebWXSendingAppMsg textMsg = new WebWXSendingAppMsg();
        textMsg.ToUserName = userId;
        textMsg.Content = content;
        msgRequest.Msg = textMsg;
        return sendMsg(msgRequest, url);
    }

    /**
     * 发送状态通知
     *
     * @param toUserName   消息接收者UserName
     * @return {@link WebWXSendMsgResponse}
     * @author SXS
     * @date 2017年5月10日 上午12:21:28
     */
    public static WebWXSendMsgResponse sendStatusNotify(String toUserName) {
        String url = String.format(URLEnum.WEB_WX_SEND_NOTIFY_MSG.getUrl(), Core.getLoginInfoMap().get(StorageLoginInfoEnum.url.getKey()));
        WebWXSendingNotifyMsg webWXSendingNotifyMsg = new WebWXSendingNotifyMsg();
        webWXSendingNotifyMsg.Code = 1;
        webWXSendingNotifyMsg.FromUserName = Core.getUserName();
        webWXSendingNotifyMsg.ToUserName =toUserName;
        String paramStr = JSON.toJSONString(webWXSendingNotifyMsg);
        HttpEntity entity = MyHttpClient.doPost(url, paramStr);
        if (entity != null) {
            try {
                String result = EntityUtils.toString(entity, Consts.UTF_8);
                return JSON.parseObject(result,WebWXSendMsgResponse.class);
            } catch (Exception e) {
                log.error("webWxSendMsgImg 错误： {}", e.getMessage());
            }
        }
        return null;
    }
    /**
     * 被动添加好友
     *
     * @param msg    消息实体
     * @param accept {@code true} 接受 {@code false} 拒绝
     * @date 2017年6月29日 下午10:08:43
     */
    public static void addFriend(AddMsgList msg, boolean accept) {
        if (!accept) {
            // 不添加
            return;
        }
        // 接受好友请求
        int status = VerifyFriendEnum.ACCEPT.getCode();
        RecommendInfo recommendInfo = msg.getRecommendInfo();
        String userName = recommendInfo.getUserName();
        String ticket = recommendInfo.getTicket();
        // 更新好友列表
        // TODO 此处需要更新好友列表
        // Core.getContactList().add(msg.getJSONObject("RecommendInfo"));

        String url = String.format(URLEnum.WEB_WX_VERIFYUSER.getUrl(), Core.getLoginInfoMap().get(StorageLoginInfoEnum.url.getKey()),
                String.valueOf(System.currentTimeMillis() / 3158L), Core.getLoginInfoMap().get(StorageLoginInfoEnum.pass_ticket.getKey()));

        List<Map<String, Object>> verifyUserList = new ArrayList<Map<String, Object>>();
        Map<String, Object> verifyUser = new HashMap<String, Object>();
        verifyUser.put("Value", userName);
        verifyUser.put("VerifyUserTicket", ticket);
        verifyUserList.add(verifyUser);

        List<Integer> sceneList = new ArrayList<Integer>();
        sceneList.add(33);

        JSONObject body = new JSONObject();
        body.put("BaseRequest", Core.getParamMap().get("BaseRequest"));
        body.put("Opcode", status);
        body.put("VerifyUserListSize", 1);
        body.put("VerifyUserList", verifyUserList);
        body.put("VerifyContent", "");
        body.put("SceneListCount", 1);
        body.put("SceneList", sceneList);
        body.put("skey", Core.getLoginInfoMap().get(StorageLoginInfoEnum.skey.getKey()));

        String result = null;
        try {
            String paramStr = JSON.toJSONString(body);
            HttpEntity entity = MyHttpClient.doPost(url, paramStr);
            result = EntityUtils.toString(entity, Consts.UTF_8);
            log.info("自动添加好友：" + result);
        } catch (Exception e) {
            log.error("webWxSendMsg", e);
        }

        if (StringUtils.isBlank(result)) {
            log.error("被动添加好友失败");
        }

        log.debug(result);

    }

    /**
     * 发送消息
     *
     * @param webWXSendMsgRequest 请求体
     * @param url                 请求地址
     * @return {@link WebWXSendMsgResponse}
     * @throws IOException IOException
     */
    private static WebWXSendMsgResponse sendMsg(WebWXSendMsgRequest webWXSendMsgRequest, String url) throws IOException {
        String paramStr = JSON.toJSONString(webWXSendMsgRequest);
        HttpEntity entity = MyHttpClient.doPost(url, paramStr);
        String s = EntityUtils.toString(entity, Consts.UTF_8);
        return JSON.parseObject(s, WebWXSendMsgResponse.class);
    }


    public static Message toPicMessage(String filePath, String toUserName) {

        if (filePath != null) {
            Dimension imageSize = ImageUtil.getImageSize(filePath);
            return Message.builder().msgType(WXSendMsgCodeEnum.PIC.getCode())
                    .filePath(filePath)
                    .imgWidth(imageSize.width)
                    .imgHeight(imageSize.height)
                    .toUsername(toUserName).build();
        }
        return null;
    }

}
