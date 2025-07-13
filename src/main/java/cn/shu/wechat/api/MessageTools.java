package cn.shu.wechat.api;


import cn.shu.wechat.constant.WxReqParamsConstant;
import cn.shu.wechat.constant.WxRespConstant;
import cn.shu.wechat.constant.WxURLEnum;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.dto.request.msg.send.*;
import cn.shu.wechat.dto.response.msg.send.WebWXSendMsgResponse;
import cn.shu.wechat.dto.response.msg.send.WebWXUploadMediaResponse;
import cn.shu.wechat.entity.Message;
import cn.shu.wechat.exception.WebWXException;
import cn.shu.wechat.mapper.MessageMapper;
import cn.shu.wechat.swing.tasks.UploadTaskCallback;
import cn.shu.wechat.swing.utils.ImageUtil;
import cn.shu.wechat.swing.utils.MimeTypeUtil;
import cn.shu.wechat.swing.utils.MultipartBodyPublisher;
import cn.shu.wechat.task.DownloadManager;
import cn.shu.wechat.utils.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
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
     * 消息Mapper
     */
    private static MessageMapper messageMapper;

    /**
     * 根据指定类型发送消息
     *
     * @param messages 消息列表
     * @param callback 文件上传进度回调
     */
    public static WebWXSendMsgResponse sendMsgByUserId(List<Message> messages, UploadTaskCallback callback) {
        if (messages == null) {
            return WebWXSendMsgResponse.error("messages is null");
        }
        WebWXSendMsgResponse sendMsgResponse = null;
        for (Message message : messages) {
            try {

                String toUserName = message.getToUsername();
                if (StringUtils.isEmpty(toUserName)) {
                    log.error("消息接收者为空：{}", message);
                    return WebWXSendMsgResponse.error("toUserName is null");

                }

                String content = XmlStreamUtil.formatXml(message.getContent());
                WxRespConstant.WXReceiveMsgCodeEnum byCode = WxRespConstant.WXReceiveMsgCodeEnum.getByCode(message.getMsgType());
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
                    case MSGTYPE_MAP:
                        sendMsgResponse = sendMapMsgByUserId(toUserName, content);
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
                return WebWXSendMsgResponse.error(e.getMessage());

            }
        }
        return sendMsgResponse;
    }

    /**
     * 根据指定类型发送消息
     *
     * @param message 消息列表
     */
    public static WebWXSendMsgResponse sendMsgByUserId(Message message) {
        ArrayList<Message> messages = new ArrayList<>();
        messages.add(message);
        return sendMsgByUserId(messages);
    }


    /**
     * 根据指定类型发送消息
     *
     * @param message  消息列表
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
            message.setMsgId(sendMsgResponse.getMsgID());
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
            message.setMessageTime(LocalDateTime.now());
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
        return storeMsgToDB(messages, sendMsgResponse, toUserName);
    }

    /**
     * 根据指定类型发送消息
     *
     * @param messages 消息列表
     */
    public static WebWXSendMsgResponse sendMsgByUserId(List<Message> messages) {
        return sendMsgByUserId(messages, null);
    }

    public static String generateClientMsgId() {
        return System.currentTimeMillis() + String.valueOf(new Random().nextLong()).substring(0, 4);
    }
    /**
     * @param filePath     文件路径
     * @param fromUserName 该消息发送者
     * @param toUserName   消息接收者
     * @param callback     上传进度回调
     * @return {@link WebWXSendMsgResponse}
     */
    private static WebWXUploadMediaResponse webWxUploadMedia(String filePath, String fromUserName, String toUserName, UploadTaskCallback callback) throws WebWXException, IOException, InterruptedException {

        //一次上传的文件最大1M
        long singleFileMaxSize = 1024 * 1024;


        //如果是上传之前下载或正在下载的资源，则等待下载完成
        if (DownloadManager.containsTask(filePath)) {
            DownloadManager.awaitDownload(filePath, 10 * 60 * 1000L);
        }
        File file = new File(filePath);
        if (!file.exists()) {
            throw new WebWXException("待上传文件不存在：" + filePath);
        }

        String fileType = WeChatTool.getFileType(file);

        String fileMime = MimeTypeUtil.getMimeByPath(file.getAbsolutePath());
        if (fileMime == null) {
            fileMime = "application";
        }
        long fileSize = file.length();
        Map<String, Object> paramMap = new HashMap<>();

        paramMap.put("UploadType", 2);
        paramMap.put("BaseRequest", Core.getLoginResultData().getBaseRequest());
        paramMap.put("ClientMediaId", generateClientMsgId());
        paramMap.put("TotalLen", fileSize);
        paramMap.put("StartPos", 0);
        paramMap.put("DataLen", fileSize);
        paramMap.put("MediaType", 4);
        paramMap.put("FromUserName", fromUserName);
        paramMap.put("ToUserName", toUserName);

        String url = String.format(WxURLEnum.WEB_WX_UPLOAD_MEDIA.getUrl(), Core.getLoginResultData().getFileUrl());
        WebWXUploadMediaResponse webWXUploadMediaResponse = new WebWXUploadMediaResponse();
        if (file.length() <= singleFileMaxSize) {
            //小于1M发送方式
            MultipartBodyPublisher multipart = new MultipartBodyPublisher()
                    .addText("name", filePath)
                    .addText("type", fileMime)
                    .addText("lastModifieDate", LocalDateTime.now().toString())
                    .addText("size", String.valueOf(fileSize))
                    .addText("mediatype", fileType) // 你自己的类型
                    .addText("uploadmediarequest", JSON.toJSONString(paramMap))
                    .addText("webwx_data_ticket", HttpUtil.getCookie("webwx_data_ticket"))
                    .addText("pass_ticket", Core.getLoginResultData().getPassTicket())
                    .addText("id", "WU_FILE_0")
                    .addFile("filename", Path.of(filePath), fileMime, filePath);
            webWXUploadMediaResponse = HttpUtil.doPostFile(url, multipart, HttpUtil.getJsonEntityBodyHandler(WebWXUploadMediaResponse.class));
            if (callback != null) {
                callback.onTaskSuccess(99, 100);
            }
        } else {
            //大于1M发送方式
            //最后一个分片上传后返回Msgid
            //检查服务器是否存在该文件
            String md5 = null;
            try {
                md5 = MD5Util.fastMD5(file);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            WebWXUploadMediaResponse checkResponse = MessageTools.webWXCheckUpload(md5, filePath, fileSize, fromUserName, toUserName);
            if (checkResponse == null || !checkResponse.isSuccess()) {
                throw new WebWXException("checkResponse is null");
            }
            //微信服务器无该文件
            if (StringUtils.isEmpty(checkResponse.getMediaId())) {
                paramMap.put("AESKey", checkResponse.getAESKey());
                paramMap.put("Signature", checkResponse.getSignature());
                paramMap.put("FileMd5", md5);
                int chunkSize = 512 * 1024;//官方512kb
                int totalChunks = (int) Math.ceil((double) fileSize / chunkSize);
                int retry = 0;
                try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {

                    byte[] buffer = new byte[chunkSize];
                    for (int i = 0; i < totalChunks; i++) {
                        int readLen;
                        raf.seek((long) i * chunkSize);
                        readLen = raf.read(buffer);
                        byte[] actualBytes = (readLen == buffer.length) ? buffer : Arrays.copyOf(buffer, readLen);
                        if (readLen == 0) {
                            throw new WebWXException("第 " + i + " 片读取失败，长度为 0");
                        }
                        MultipartBodyPublisher multipart = new MultipartBodyPublisher()
                                .addText("id", String.format("WU_FILE_%d", i))
                                .addText("name", filePath)
                                .addText("type", fileMime)
                                .addText("lastModifieDate", LocalDateTime.now().toString())
                                .addText("size", String.valueOf(fileSize))
                                .addText("mediatype", fileType)
                                .addText("uploadmediarequest", JSON.toJSONString(paramMap))
                                .addText("webwx_data_ticket", HttpUtil.getCookie("webwx_data_ticket"))
                                .addText("pass_ticket", Core.getLoginResultData().getPassTicket())
                                .addText("chunks", String.valueOf(totalChunks))
                                .addText("chunk", String.valueOf(i))
                                .addFile("filename", actualBytes, fileMime, filePath)
                                .addText("chunks", String.valueOf(totalChunks));
                        webWXUploadMediaResponse = HttpUtil.doPostFile(url, multipart, HttpUtil.getJsonEntityBodyHandler(WebWXUploadMediaResponse.class));

                        if (webWXUploadMediaResponse == null || !webWXUploadMediaResponse.isSuccess()) {
                            if (retry++ < 3) {
                                //重试当前块
                                log.warn("上传文件块失败：{},{},{}", i, filePath, webWXUploadMediaResponse);
                                i--;
                                continue;
                            }
                            throw new WebWXException("上传失败：response is null.");
                        }

                        if (callback != null) {
                            callback.onTaskSuccess(i, totalChunks);
                        }

                    }
                    webWXUploadMediaResponse.setSignature(checkResponse.getSignature());
                    webWXUploadMediaResponse.setAESKey(checkResponse.getAESKey());
                }
            } else {
                //微信服务器存在该文件
                BeanUtils.copyProperties(checkResponse, webWXUploadMediaResponse);
                webWXUploadMediaResponse.setStartPos(fileSize);
            }


        }

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
    private static WebWXSendMsgResponse sendTextMsgByUserId(String content, String toUserName) throws IOException, InterruptedException {
        String url = String.format(WxURLEnum.WEB_WX_SEND_MSG.getUrl(), Core.getLoginResultData().getUrl());
        WebWXSendMsgRequest msgRequest = new WebWXSendMsgRequest();
        WebWXSendingMsg textMsg = new WebWXSendingTextMsg();
        textMsg.Content = content;
        textMsg.ToUserName = toUserName;
        msgRequest.Msg = textMsg;
        return sendMsg(msgRequest, url);
    }

    /**
     * 根据UserName发送地图消息
     *
     * @param toUserName 消息接收者UserName
     * @param content    消息内容，
     * @return {@link WebWXSendMsgResponse}
     * @author SXS
     * @date 2021年9月26日 下午14:18:38
     */
    private static WebWXSendMsgResponse sendMapMsgByUserId(String toUserName, String content) throws IOException, InterruptedException {
        String url = String.format(WxURLEnum.WEB_WX_SEND_MSG.getUrl(), Core.getLoginResultData().getUrl());
        WebWXSendMsgRequest msgRequest = new WebWXSendMsgRequest();
        WebWXSendingMsg textMsg = new WebWXSendingMapMsg();
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
    private static WebWXSendMsgResponse sendCardMsgByUserId(String toUserName, String content) throws IOException, InterruptedException {
        String url = String.format(WxURLEnum.WEB_WX_SEND_MSG.getUrl(), Core.getLoginResultData().getUrl());
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
    private static WebWXSendMsgResponse sendPicMsgByUserId(String userId, String filePath, String content, UploadTaskCallback callback) throws WebWXException, IOException, InterruptedException {
        String mediaId = "";
        if (StringUtils.isEmpty(content) || !content.startsWith("@")) {
            WebWXUploadMediaResponse resp = webWxUploadMedia(filePath, Core.getUserName(), userId, callback);
            mediaId = resp.getMediaId();
            content = "";
        }
        String url = String.format(WxURLEnum.WEB_WX_SEND_PIC_MSG.getUrl(), Core.getLoginResultData().getUrl(),
                Core.getLoginResultData().getPassTicket());

        WebWXSendMsgRequest msgRequest = new WebWXSendMsgRequest();
        WebWXSendingPicMsg textMsg = new WebWXSendingPicMsg();
        textMsg.MediaId = mediaId;
        textMsg.ToUserName = userId;
        textMsg.Content = content;
        msgRequest.Msg = textMsg;
        WebWXSendMsgResponse webWXSendMsgResponse = sendMsg(msgRequest, url);
        if (callback != null) {
            callback.onTaskSuccess(100, 100);
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
    private static WebWXSendMsgResponse sendEmotionMsgByUserId(String userId, String filePath, String content) throws WebWXException, IOException, InterruptedException {

        String url = String.format(WxURLEnum.WEB_WX_SEND_EMOTION_MSG.getUrl(), Core.getLoginResultData().getUrl());

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
    public static boolean sendRevokeMsgByUserId(String userId, String clientMsgId, String svrMsgId) throws IOException, InterruptedException {

        String url = String.format(WxURLEnum.WEB_WX_REVOKE_MSG.getUrl()
                , Core.getLoginResultData().getUrl()
                , Core.getLoginResultData().getPassTicket());

        WebWXSendingRevokeMsg webWXSendingRevokeMsg = new WebWXSendingRevokeMsg();
        webWXSendingRevokeMsg.ClientMsgId = clientMsgId;
        webWXSendingRevokeMsg.SvrMsgId = svrMsgId;
        webWXSendingRevokeMsg.ToUserName = userId;
        webWXSendingRevokeMsg.BaseRequest = Core.getLoginResultData().getBaseRequest();
        String paramStr = JSON.toJSONString(webWXSendingRevokeMsg);


        JSONObject jsonObject = HttpUtil.doPost(url, paramStr, HttpUtil.getJsonEntityBodyHandler(JSONObject.class));
        return jsonObject.getJSONObject("BaseResponse").getInteger("Ret") == 0;

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
    private static WebWXSendMsgResponse sendVideoMsgByUserId(String userId, String filePath, String content, UploadTaskCallback callback) throws WebWXException, IOException, InterruptedException {
        String mediaId = "";
        if (StringUtils.isEmpty(content) || !content.startsWith("@")) {
            WebWXUploadMediaResponse resp = webWxUploadMedia(filePath, Core.getUserName(), userId, callback);
            mediaId = resp.getMediaId();
            content = "";
        }
        String url = String.format(WxURLEnum.WEB_WX_SEND_VIDEO_MSG.getUrl(), Core.getLoginResultData().getUrl(),
                Core.getLoginResultData().getPassTicket());
        WebWXSendMsgRequest msgRequest = new WebWXSendMsgRequest();
        WebWXSendingVideoMsg textMsg = new WebWXSendingVideoMsg();
        textMsg.MediaId = mediaId;
        textMsg.ToUserName = userId;
        textMsg.Content = content;
        msgRequest.Msg = textMsg;
        msgRequest.BaseRequest = Core.getLoginResultData().getBaseRequest();
        WebWXSendMsgResponse webWXSendMsgResponse = sendMsg(msgRequest, url);
        if (callback != null) {
            callback.onTaskSuccess(100, 100);
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
    private static WebWXSendMsgResponse sendAppMsgByUserId(String userId, String filePath, String content, UploadTaskCallback callback) throws IOException, WebWXException, InterruptedException {
        String url = String.format(WxURLEnum.WEB_WX_SEND_APP_MSG.getUrl(), Core.getLoginResultData().getUrl(),
                Core.getLoginResultData().getPassTicket());
        WebWXSendMsgRequest msgRequest = new WebWXSendMsgRequest();
        WebWXSendingAppMsg textMsg = new WebWXSendingAppMsg();
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
                    "<title>" + title + "</title>" +
                    "<des></des>" +
                    "<action></action>" +
                    "<type>6</type>" +
                    "<content></content>" +
                    "<url></url>" +
                    "<lowurl></lowurl>" +
                    "<appattach>" +
                    "<totallen>" + totallen + "</totallen>" +
                    "<attachid>" + attachid + "</attachid>" +
                    "<fileext>" + fileext + "</fileext>" +
                    "</appattach>" +
                    "<extinfo></extinfo>" +
                    "</appmsg>";

            textMsg.Signature = webWXUploadMediaResponse.getSignature();
        } else {
            //发送服务器上的文件 通过attachid
            Map<String, Object> stringObjectMap = XmlStreamUtil.toMap(content);
            Object attachid_ = stringObjectMap.get("msg.appmsg.appattach.attachid");
            Object totallen = stringObjectMap.get("msg.appmsg.appattach.totallen");
            Object fileext = stringObjectMap.get("msg.appmsg.appattach.fileext");
            Object title = stringObjectMap.get("msg.appmsg.title");
            content = "<appmsg appid='wxeb7ec651dd0aefa9' sdkver=''>" +
                    "<title>" + title + "</title>" +
                    "<des></des>" +
                    "<action></action>" +
                    "<type>6</type><content></content>" +
                    "<url></url><lowurl></lowurl>"
                    + "<appattach>" +
                    "<totallen>" + totallen + "</totallen>" +
                    "<attachid>" + attachid_ + "</attachid>" +
                    "<fileext>" + fileext + "</fileext>" +
                    "</appattach><extinfo></extinfo></appmsg>";

        }

        textMsg.ToUserName = userId;
        textMsg.Content = content;
        msgRequest.Msg = textMsg;
        msgRequest.BaseRequest = Core.getLoginResultData().getBaseRequest();
        WebWXSendMsgResponse webWXSendMsgResponse = sendMsg(msgRequest, url);
        if (callback != null) {
            callback.onTaskSuccess(100, 100);
        }
        return webWXSendMsgResponse;
    }

    /**
     * 发送APP消息
     *
     * @param userId                   消息接收者UserName
     * @param webWXUploadMediaResponse 上传文件信息
     * @return {@link WebWXSendMsgResponse}
     * @author SXS
     * @date 2017年5月10日 上午12:21:28
     */
    private static WebWXSendMsgResponse sendAppMsgByUserId(String userId, WebWXUploadMediaResponse webWXUploadMediaResponse, String filePath) throws IOException, InterruptedException {
        String url = String.format(WxURLEnum.WEB_WX_SEND_APP_MSG.getUrl(), Core.getLoginResultData().getUrl(),
                Core.getLoginResultData().getPassTicket());
        String title = new File(filePath).getName();
        String fileext = title.split("\\.")[1];
        if (fileext == null) {
            fileext = "";
        }
        long totallen = webWXUploadMediaResponse.getStartPos();
        String attachid = webWXUploadMediaResponse.getMediaId();
        String content = "<appmsg appid='wxeb7ec651dd0aefa9' sdkver=''>" +
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
     * @param toUserName 消息接收者UserName
     * @return {@link WebWXSendMsgResponse}
     * @author SXS
     * @date 2017年5月10日 上午12:21:28
     */
    public static WebWXSendMsgResponse sendStatusNotify(String toUserName) throws IOException, InterruptedException {
        String url = String.format(WxURLEnum.WEB_WX_SEND_NOTIFY_MSG.getUrl(), Core.getLoginResultData().getUrl());
        WebWXSendingNotifyMsg webWXSendingNotifyMsg = new WebWXSendingNotifyMsg();
        webWXSendingNotifyMsg.Code = 1;
        webWXSendingNotifyMsg.FromUserName = Core.getUserName();
        webWXSendingNotifyMsg.ToUserName = toUserName;
        String paramStr = JSON.toJSONString(webWXSendingNotifyMsg);


        return HttpUtil.doPost(url, paramStr, HttpUtil.getJsonEntityBodyHandler(WebWXSendMsgResponse.class));

    }

    /**
     * 被动添加好友
     *
     * @param userName 用户名
     * @param ticket   ticket
     * @date 2017年6月29日 下午10:08:43
     */
    public static WebWXSendMsgResponse addFriend(String userName, String ticket) throws IOException, InterruptedException {

        // 接受好友请求
        int status = WxReqParamsConstant.VerifyFriendEnum.ACCEPT.getCode();


        String url = String.format(WxURLEnum.WEB_WX_VERIFYUSER.getUrl(), Core.getLoginResultData().getUrl(),
                System.currentTimeMillis() / 3158L, Core.getLoginResultData().getPassTicket());

        List<Map<String, Object>> verifyUserList = new ArrayList<Map<String, Object>>();
        Map<String, Object> verifyUser = new HashMap<String, Object>();
        verifyUser.put("Value", userName);
        verifyUser.put("VerifyUserTicket", ticket);
        verifyUserList.add(verifyUser);

        List<Integer> sceneList = new ArrayList<Integer>();
        sceneList.add(33);

        JSONObject body = new JSONObject();
        body.put("BaseRequest", Core.getLoginResultData().getBaseRequest());
        body.put("Opcode", status);
        body.put("VerifyUserListSize", 1);
        body.put("VerifyUserList", verifyUserList);
        body.put("VerifyContent", "");
        body.put("SceneListCount", 1);
        body.put("SceneList", sceneList);
        body.put("skey", Core.getLoginResultData().getBaseRequest().getSKey());

        String paramStr = JSON.toJSONString(body);
        WebWXSendMsgResponse webWXSendMsgResponse = HttpUtil.doPost(url, paramStr, HttpUtil.getJsonEntityBodyHandler(WebWXSendMsgResponse.class));
        log.info("自动添加好友：" + webWXSendMsgResponse);
        return webWXSendMsgResponse;


    }

    /**
     * 修改联系人备注
     *
     * @param userName   用户id
     * @param remarkName 备注名称
     * @return 参数
     */
    public static WebWXSendMsgResponse modifyRemarkName(String userName, String remarkName) throws IOException, InterruptedException {
        String url = String.format(WxURLEnum.WEB_WX_REMARKNAME.getUrl(), Core.getLoginResultData().getUrl());
        WebWXSendMsgRequest msgRequest = new WebWXSendMsgRequest();
        WebWXModifyRemarkNameMsg msg = new WebWXModifyRemarkNameMsg();
        msg.CmdId = 2;
        msg.RemarkName = remarkName;
        msg.UserName = userName;
        msgRequest.Msg = msg;
        return sendMsg(msgRequest, url);
    }

    /**
     * 发送消息
     *
     * @param webWXSendMsgRequest 请求体
     * @param url                 请求地址
     * @return {@link WebWXSendMsgResponse}
     * @throws IOException IOException
     */
    private static WebWXSendMsgResponse sendMsg(WebWXSendMsgRequest webWXSendMsgRequest, String url) throws IOException, InterruptedException {
        webWXSendMsgRequest.BaseRequest = Core.getLoginResultData().getBaseRequest();
        String paramStr = JSON.toJSONString(webWXSendMsgRequest);

        return HttpUtil.doPost(url, paramStr, HttpUtil.getJsonEntityBodyHandler(WebWXSendMsgResponse.class));
    }


    public static Message toPicMessage(String filePath, String toUserName) {

        if (filePath != null) {
            Dimension imageSize = ImageUtil.getImageSize(filePath);
            return Message.builder().msgType(WxReqParamsConstant.WXSendMsgCodeEnum.PIC.getCode())
                    .filePath(filePath)
                    .imgWidth(imageSize.width)
                    .imgHeight(imageSize.height)
                    .toUsername(toUserName).build();
        }
        return null;
    }

    /**
     * 随机生成MessageId
     *
     * @return
     */
    public static String randomMessageId() {
        String raw = UUID.randomUUID().toString().replace("-", "");
        return raw;
    }

    /**
     * 将 卡片消息的xml提取到各个字段
     *
     * @param content xml
     * @param message 消息
     */
    public static Map<String, Object> setMessageCardField(String content, Message message) {
        Map<String, Object> map = new HashMap<>();
        try {
            map = XmlStreamUtil.toMap(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Object title = map.get("msg.attr.nickname");
        Object thumbUrl = map.get("msg.attr.smallheadimgurl");
        Object headImgUrl = map.get("msg.attr.bigheadimgurl");
        Object id = map.get("msg.attr.alias");
        Object province = map.get("msg.attr.province");
        Object city = map.get("msg.attr.city");
        Object sex = map.get("msg.attr.sex");
        Object userName = map.get("msg.attr.username");

        message.setContactsNickName(title == null ? null : title.toString());
        message.setContactsId(id == null ? null : id.toString());
        message.setContactsProvince(province == null ? null : province.toString());
        message.setContactsCity(city == null ? null : city.toString());
        message.setContactsSex(sex == null ? null : Byte.valueOf(sex.toString()));
        message.setThumbUrl(thumbUrl == null ? null : thumbUrl.toString());
        message.setContactsUserName(userName == null ? null : userName.toString());
        message.setContactsHeadImgUrl(headImgUrl == null ? null : headImgUrl.toString());
        return map;
    }

    public static WebWXUploadMediaResponse webWXCheckUpload(String md5, String fileName, long fileSize, String fromUserName, String toUserName) throws IOException, InterruptedException {
        JSONObject body = new JSONObject();
        body.put("BaseRequest", Core.getLoginResultData().getBaseRequest());
        body.put("FileMd5", md5);
        body.put("FileName", fileName);
        body.put("FileSize", fileSize);
        body.put("FileType", 7);
        body.put("FromUserName", fromUserName);
        body.put("ToUserName", toUserName);


        String paramStr = JSON.toJSONString(body);
        String url = String.format(WxURLEnum.WEB_WX_CHECK_UPLOAD_MSG.getUrl(), Core.getLoginResultData().getPassTicket());
        return HttpUtil.doPost(url, paramStr, HttpUtil.getJsonEntityBodyHandler(WebWXUploadMediaResponse.class));

    }

}
