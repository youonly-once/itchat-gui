package cn.shu.wechat.api;


import cn.shu.wechat.beans.msg.SendMsgResponse;
import cn.shu.wechat.beans.pojo.Message;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.enums.*;
import cn.shu.wechat.mapper.MessageMapper;
import cn.shu.wechat.protocol.RspUploadMedia;
import cn.shu.wechat.utils.*;
import cn.shu.wechat.utils.xxx.WeChatToolXXX;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.http.executor.XHttpExecutor;
import me.xuxiaoxiao.xtools.common.http.executor.impl.XHttpExecutorImpl;
import me.xuxiaoxiao.xtools.common.http.executor.impl.XRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import cn.shu.wechat.beans.msg.sync.AddMsgList;
import cn.shu.wechat.beans.msg.sync.RecommendInfo;
import org.springframework.stereotype.Component;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
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
    private final static XHttpExecutor httpExecutor = new XHttpExecutorImpl();
    private static int fileCount = 0;
    private static Date lastSendImgDate = new Date();

    private static MessageMapper messageMapper;

    @Resource
    public  void setMessageMapper(MessageMapper messageMapper) {
        MessageTools.messageMapper = messageMapper;
    }


    /**
     * 根据指定类型发送消息
     *
     * @param results
     * @param toUserId toUserId
     */
    public static void sendMsgByUserId(List<Result> results, String toUserId) {
        if (results == null || results.isEmpty()) {
            return;
        }

        for (Result result : results) {
            //result若指定接收人
            if (StringUtils.isNotEmpty(result.toUserName)) {
                toUserId = result.toUserName;
            }else{
                result.toUserName = toUserId;
            }
            //发送延迟
            if (result.sleep != null && result.sleep > 0) {
                SleepUtils.sleep(result.sleep);

            }
            log.info(" : " + LogUtil.printToMeg(toUserId, result.msg));
            SendMsgResponse sendMsgResponse = null;
            switch (result.replyMsgTypeEnum) {
                case PIC://图片消息
                    //至少间隔1秒发送
                    long l = new Date().getTime() - lastSendImgDate.getTime();
                    lastSendImgDate = new Date();
                    if (l>0 && l<1000)SleepUtils.sleep(1000 - l);
                    sendMsgResponse = sendPicMsgByUserId(toUserId, result.msg);
                    break;
                case TEXT://文本消息
                    sendMsgResponse = sendTextMsgByUserId(result.msg, toUserId);
                    break;
                case VIDEO:
                    sendMsgResponse = sendVideoMsgByUserId(toUserId, result.msg);
                    break;
                default://其他消息发送文件
                    sendMsgResponse = sendFileMsgByUserId(toUserId, result.msg);
            }
            storeMsgToDB(results,sendMsgResponse);
      /*      if (sendMsgResponse != null) {
                SleepUtils.sleep(1000);
                sendRevokeMsgByUserId(toUserId, sendMsgResponse.getLocalID(), sendMsgResponse.getMsgID());
            }*/
        }

    }

    /**
     * 保存发送的消息到数据库
     * @param results
     * @param sendMsgResponse
     */
    private static void storeMsgToDB(List<Result> results,SendMsgResponse sendMsgResponse){
        ArrayList<Message> messages = new ArrayList<>();
        for (Result result : results) {
            boolean isToSelf = result.toUserName.endsWith(Core.getUserName());
            WXReceiveMsgCodeEnum type = WXReceiveMsgCodeEnum.UNKNOWN;
            switch (result.replyMsgTypeEnum){
                case TEXT:
                    type = WXReceiveMsgCodeEnum.MSGTYPE_TEXT;
                    break;
                case PIC:
                    type = WXReceiveMsgCodeEnum.MSGTYPE_IMAGE;
                    break;
                case VOICE:
                    type = WXReceiveMsgCodeEnum.MSGTYPE_VOICE;
                    break;
                case APP:
                    type = WXReceiveMsgCodeEnum.MSGTYPE_APP;
                    break;
                case VIDEO:
                    type = WXReceiveMsgCodeEnum.MSGTYPE_VIDEO;
                    break;
            }
            Message build = Message
                    .builder()
                    .content(result.msg)
                    .createTime(new Date())
                    .fromNickname(Core.getNickName())
                    .fromRemarkname(Core.getNickName())
                    .fromUsername(Core.getUserName())
                    .id(UUID.randomUUID().toString().replace("-", ""))
                    .toNickname(isToSelf ? Core.getNickName() : WechatTools.getNickNameByUserName(result.toUserName))
                    .toRemarkname(isToSelf ? Core.getNickName() : WechatTools.getRemarkNameByUserName(result.toUserName))
                    .toUsername(result.toUserName)
                    .msgId(sendMsgResponse == null?null:sendMsgResponse.getLocalID())
                    .msgType(type.getCode())
                    .isSend(true)
                    .appMsgType(null)
                    .msgJson(JSON.toJSONString(result))
                    .msgDesc(result.replyMsgTypeEnum.getMsg())
                    .build();
            messages.add(build);

        }
        int insert = messageMapper.batchInsert(messages);
    }
    /**
     * 根据UserName发送文本消息
     *
     * @param content
     * @param toUserName
     * @author SXS
     * @date 2017年5月4日 下午11:17:38
     */
    public static SendMsgResponse sendTextMsgByUserId(String content, String toUserName) {
        String url = String.format(URLEnum.WEB_WX_SEND_MSG.getUrl(), Core.getLoginInfoMap().get("url"));
        Map<String, Object> msgMap = new HashMap<String, Object>();
        msgMap.put("Type", WXSendMsgCodeEnum.TEXT.getCode());
        msgMap.put("Content", content);
        msgMap.put("FromUserName", Core.getUserName());
        msgMap.put("ToUserName", toUserName == null ? Core.getUserName() : toUserName);
        msgMap.put("LocalID", new Date().getTime() * 10);
        msgMap.put("ClientMsgId", new Date().getTime() * 10);
        Map<String, Object> paramMap = Core.getParamMap();
        paramMap.put("Msg", msgMap);
        paramMap.put("Scene", 0);
        try {
            String paramStr = JSON.toJSONString(paramMap);
            HttpEntity entity = MyHttpClient.doPost(url, paramStr);
            String s = EntityUtils.toString(entity, Consts.UTF_8);
            return JSON.parseObject(s, SendMsgResponse.class);
        } catch (Exception e) {
            log.error("webWxSendMsg", e);
        }
        return null;
    }

    /**
     * 上传多媒体文件到 微信服务器，目前应该支持3种类型: 1. pic 直接显示，包含图片，表情 2.video 3.doc 显示为文件，包含PDF等
     *
     * @param filePath
     * @return
     * @author SXS
     * @date 2017年5月7日 上午12:41:13
     */
    private static JSONObject webWxUploadMedia(String filePath) {
        File f = new File(filePath);
        if (!f.exists() && f.isFile()) {
            log.info("file is not exist");
            return null;
        }
        //1M以上视频发不出去
        long fileSize = f.length();
        if (f.getName().toLowerCase().endsWith(".mp4")) {
            int bitRate = 800000;
            while (fileSize > 1024 * 1024) {
                f = MediaUtil.compressionVideo(f, "/compression/" + f.getName() + ".mp4", bitRate);
                fileSize = f.length();
                bitRate = (int) (bitRate / 2);
            }
        }
        String url = String.format(URLEnum.WEB_WX_UPLOAD_MEDIA.getUrl(), Core.getLoginInfoMap().get("fileUrl"));
        String mimeType = new MimetypesFileTypeMap().getContentType(f);
        String mediaType = "";
        if (mimeType == null) {
            mimeType = "text/plain";
        } else {
            mediaType = mimeType.split("/")[0].equals("image") ? "pic" : "doc";
        }
        if ("pic".equals(mediaType) && fileSize > 1024 * 1024) {
            f = MediaUtil.compressImage(f, 1024 * 1024);
        }
        fileSize = f.length();
        String lastModifieDate = new SimpleDateFormat("yyyy MM dd HH:mm:ss").format(new Date());

        String passTicket = (String) Core.getLoginInfoMap().get("pass_ticket");
        String clientMediaId = String.valueOf(new Date().getTime())
                + String.valueOf(new Random().nextLong()).substring(0, 4);
        String webwxDataTicket = MyHttpClient.getCookie("webwx_data_ticket");
        if (webwxDataTicket == null) {
            log.error("get cookie webwx_data_ticket error");
            return null;
        }

        Map<String, Object> paramMap = Core.getParamMap();

        paramMap.put("ClientMediaId", clientMediaId);
        paramMap.put("TotalLen", fileSize);
        paramMap.put("StartPos", 0);
        paramMap.put("DataLen", fileSize);
        paramMap.put("MediaType", 4);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        builder.addTextBody("id", "WU_FILE_0", ContentType.TEXT_PLAIN);
        builder.addTextBody("name", filePath, ContentType.TEXT_PLAIN);
        builder.addTextBody("type", mimeType, ContentType.TEXT_PLAIN);
        builder.addTextBody("lastModifieDate", lastModifieDate, ContentType.TEXT_PLAIN);
        builder.addTextBody("size", String.valueOf(fileSize), ContentType.TEXT_PLAIN);
        builder.addTextBody("mediatype", mediaType, ContentType.TEXT_PLAIN);
        builder.addTextBody("uploadmediarequest", JSON.toJSONString(paramMap), ContentType.TEXT_PLAIN);
        builder.addTextBody("webwx_data_ticket", webwxDataTicket, ContentType.TEXT_PLAIN);
        builder.addTextBody("pass_ticket", passTicket, ContentType.TEXT_PLAIN);
        builder.addBinaryBody("filename", f, ContentType.create(mimeType), filePath);
        HttpEntity reqEntity = builder.build();
        HttpEntity entity = MyHttpClient.doPostFile(url, reqEntity);
        if (entity != null) {
            try {
                String result = EntityUtils.toString(entity, Consts.UTF_8);
                return JSON.parseObject(result);
            } catch (Exception e) {
                log.error("webWxUploadMedia 错误： ", e);
            }

        }
        return null;
    }

    /**
     * 上传多媒体文件到 微信服务器，目前应该支持3种类型: 1. pic 直接显示，包含图片，表情 2.video 3.doc 显示为文件，包含PDF等
     *
     * @param filePath
     * @return
     * @author SXS
     * @date 2017年5月7日 上午12:41:13
     */
    private static JSONObject webWxUploadBatchMedia(String filePath, String fromUserName, String toUserName) {
        File f = new File(filePath);
        if (!f.exists() && f.isFile()) {
            log.info("file is not exist");
            return null;
        }
        //1M以上视频发不出去
        long fileSize = f.length();
        if (f.getName().toLowerCase().endsWith(".mp4")) {
            int bitRate = 800000;
            while (fileSize > 1024 * 1024 * 20) {
                f = MediaUtil.compressionVideo(f, "/compression/" + f.getName() + ".mp4", bitRate);
                fileSize = f.length();
                bitRate = (int) (bitRate / 2);
            }
        }
        String url = String.format(URLEnum.WEB_WX_UPLOAD_MEDIA.getUrl(), Core.getLoginInfoMap().get("fileUrl"));
        String mimeType = null;
        try {
            mimeType = Files.probeContentType(Paths.get(f.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String mediaType = "";
        if (mimeType == null) {
            mimeType = "text/plain";
        }
        mediaType = WeChatToolXXX.fileType(f);
        if ("pic".equals(mediaType) && fileSize > 1024 * 1024 * 20) {
            f = MediaUtil.compressImage(f, 1024 * 1024 * 20);
        }
        fileSize = f.length();
        String lastModifieDate = new SimpleDateFormat("yyyy MM dd HH:mm:ss").format(new Date());
        String passTicket = (String) Core.getLoginInfoMap().get("pass_ticket");
        if (StringUtils.isEmpty(passTicket)) {
            passTicket = "undefined";
        }
        String clientMediaId = String.valueOf(new Date().getTime())
                + String.valueOf(new Random().nextLong()).substring(0, 4);
        String webwxDataTicket = MyHttpClient.getCookie("webwx_data_ticket");
        if (webwxDataTicket == null) {
            log.error("get cookie webwx_data_ticket error");
            return null;
        }

        Map<String, Object> paramMap = Core.getParamMap();

        paramMap.put("UploadType", 2);
        paramMap.put("BaseRequest", Core.getParamMap().get("BaseRequest"));
        paramMap.put("ClientMediaId", clientMediaId);
        paramMap.put("TotalLen", fileSize);
        paramMap.put("StartPos", 0);
        paramMap.put("DataLen", fileSize);
        paramMap.put("MediaType", 4);
        paramMap.put("FromUserName", fromUserName);
        paramMap.put("ToUserName", toUserName);
        paramMap.put("FileMd5", XTools.md5(f));
        HttpEntity resEntity = null;
        if (f.length() < 1048576L) {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addTextBody("id", "WU_FILE_0", ContentType.TEXT_PLAIN);
            builder.addTextBody("name", filePath, ContentType.TEXT_PLAIN);
            builder.addTextBody("type", mimeType, ContentType.TEXT_PLAIN);
            builder.addTextBody("lastModifieDate", lastModifieDate, ContentType.TEXT_PLAIN);
            builder.addTextBody("size", String.valueOf(fileSize), ContentType.TEXT_PLAIN);
            builder.addTextBody("mediatype", mediaType, ContentType.TEXT_PLAIN);
            builder.addTextBody("uploadmediarequest", JSON.toJSONString(paramMap), ContentType.TEXT_PLAIN);
            builder.addTextBody("webwx_data_ticket", webwxDataTicket, ContentType.TEXT_PLAIN);
            builder.addTextBody("pass_ticket", passTicket, ContentType.TEXT_PLAIN);
            builder.addBinaryBody("filename", f, ContentType.create(mimeType), filePath);
            HttpEntity reqEntity = builder.build();
            HttpEntity entity = MyHttpClient.doPostFile(url, reqEntity);
            if (entity != null) {
                try {
                    String result = EntityUtils.toString(entity, Consts.UTF_8);
                    return JSON.parseObject(result);
                } catch (Exception e) {
                    log.error("webWxUploadMedia 错误： ", e);
                }

            }
        } else {
            try {
                byte[] sliceBuffer = new byte[512 * 1024];
                BufferedInputStream bfinStream = new BufferedInputStream(new FileInputStream(f));
                long sliceIndex = 0;
                long sliceCount = (long) Math.ceil((double) f.length() / 512.0D / 1024.0D);
                //读取文件所有字节
                byte[] allBytes = Files.readAllBytes(Paths.get(filePath));

                for (; sliceIndex < sliceCount; ++sliceIndex) {
                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    //   builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    builder.addTextBody("id", "WU_FILE_" + sliceIndex, ContentType.TEXT_PLAIN);
                    builder.addTextBody("name", filePath, ContentType.TEXT_PLAIN);
                    builder.addTextBody("type", mimeType, ContentType.TEXT_PLAIN);
                    builder.addTextBody("lastModifieDate", lastModifieDate, ContentType.TEXT_PLAIN);
                    builder.addTextBody("size", String.valueOf(fileSize), ContentType.TEXT_PLAIN);
                    builder.addTextBody("chunks", String.valueOf(sliceCount), ContentType.TEXT_PLAIN);
                    builder.addTextBody("chunk", String.valueOf(sliceIndex), ContentType.TEXT_PLAIN);
                    builder.addTextBody("mediatype", mediaType, ContentType.TEXT_PLAIN);
                    builder.addTextBody("uploadmediarequest", JSON.toJSONString(paramMap), ContentType.TEXT_PLAIN);
                    builder.addTextBody("webwx_data_ticket", webwxDataTicket, ContentType.TEXT_PLAIN);
                    builder.addTextBody("pass_ticket", passTicket, ContentType.TEXT_PLAIN);
                    String s = filePath + "." + sliceIndex + ".part";
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(s));
                    byte[] copyOfRange;
                    if (sliceIndex == sliceCount - 1) {
                        copyOfRange = Arrays.copyOfRange(allBytes, (int) sliceIndex * 512 * 1024, allBytes.length);
                    } else {
                        copyOfRange = Arrays.copyOfRange(allBytes, (int) sliceIndex * 512 * 1024, (int) (sliceIndex + 1) * 512 * 1024);
                    }
                    bos.flush();
                    bos.write(copyOfRange);
                    bos.close();

                    builder.addBinaryBody("filename", new File(s), ContentType.create(mimeType), filePath);
                 /*   while((readCount = bfinStream.read(sliceBuffer, sliceIndex * 512*1024, sliceBuffer.length)) > 0) {
                        slice.count += readCount;
                        if (slice.count >= sliceBuffer.length) {
                            break;
                        }
                    }*/
                    HttpEntity reqEntity = builder.build();
                    resEntity = MyHttpClient.doPostFile(url, reqEntity);
                    String result = EntityUtils.toString(reqEntity);
                    System.out.println(result);
                }
            } catch (Exception e) {
                log.error("webWxUploadMedia 错误： ", e);
            }
        }
        if (resEntity != null) {
            try {
                String result = EntityUtils.toString(resEntity, Consts.UTF_8);
                return JSON.parseObject(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    static JSONObject webwxuploadmedia(String filePath, String fromUserName, String toUserName) {
        File file = new File(filePath);
        if (!file.exists() && !file.isFile()) {
            log.info("file is not exist");
            return null;
        }

        String fileType = WeChatToolXXX.fileType(file);
        long fileSize = file.length();

        if (fileSize > 1024 * 1024) {
            switch (fileType) {
                case "video":  //视频超过1M，则压缩到1M
                    int bitRate = 800000;
                    try {
                        String name = file.getName();
                        while (fileSize > 1024 * 1024) {
                            file = MediaUtil.compressionVideo(file, "/compression/" + name + ".mp4", bitRate);
                            fileSize = file.length();
                            bitRate = (int) (bitRate / 2);
                        }
                    } catch (Exception e) {
                        log.error("发送失败：" + e.getMessage());
                        return null;
                    }
                    break;
                case "pic":  //图片超过1M，则压缩到1M
                    file = MediaUtil.compressImage(file, 1024 * 1024);
                    break;
            }
        }
        int fileId = fileCount++;
        String fileName = file.getName();
        String fileMime = null;
        try {
            fileMime = Files.probeContentType(Paths.get(file.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        String fileMd5 = XTools.md5(file);

        Map<String, Object> paramMap = Core.getParamMap();
        String lastModifyFileDate = new SimpleDateFormat("yyyy MM dd HH:mm:ss").format(file.lastModified());

        String passTicket = (String) Core.getLoginInfoMap().get("pass_ticket");
        if (StringUtils.isEmpty(passTicket)) {
            passTicket = "undefined";
        }
        String clientMediaId = new Date().getTime() + String.valueOf(new Random().nextLong()).substring(0, 4);
        String webWXDataTicket = MyHttpClient.getCookie("webwx_data_ticket");
        if (webWXDataTicket == null) {
            log.error("get cookie webwx_data_ticket error");
        }
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

        if (file.length() !=0 /*1048576L*/) {
            XRequest request = XRequest.POST(String.format("https://file.%s/cgi-bin/mmwebwx-bin/webwxuploadmedia", "wx2.qq.com"));
            request.query("f", "json");
            request.content("id", String.format("WU_FILE_%d", fileId));
            request.content("name", fileName);
            request.content("type", fileMime);
            request.content("lastModifiedDate", lastModifyFileDate);
            request.content("size", file.length());
            request.content("mediatype", fileType);
            request.content("uploadmediarequest", JSON.toJSONString(paramMap));
            request.content("webwx_data_ticket", webWXDataTicket);
            request.content("pass_ticket", passTicket);
            request.content("filename", file);
            result = XTools.http(httpExecutor, request).string();
            //"ret 1205 好像是发送频繁"
        } else {//TODO 以下功能异常
            RspUploadMedia rspUploadMedia = null;
            byte[] sliceBuffer = new byte[524288];
            BufferedInputStream bfinStream = null;
            ArrayList<String> arrayList = splitFile1(filePath);
            try {
                long sliceIndex = 0L;
                bfinStream = new BufferedInputStream(new FileInputStream(file));
                for (long sliceCount = (long) Math.ceil((double) file.length() / 512.0D / 1024.0D); sliceIndex < sliceCount; ++sliceIndex) {
                    XRequest request = XRequest.POST(String.format("https://file.%s/cgi-bin/mmwebwx-bin/webwxuploadmedia", "wx2.qq.com"));
                    request.query("f", "json");
                    request.content("id", String.format("WU_FILE_%d", fileId));
                    request.content("name", fileName);
                    request.content("type", fileMime);
                    request.content("lastModifiedDate", lastModifyFileDate);
                    request.content("size", file.length());
                    request.content("chunks", sliceCount);
                    request.content("chunk", sliceIndex);
                    request.content("mediatype", fileType);
                    request.content("uploadmediarequest", JSON.toJSONString(paramMap));
                    request.content("webwx_data_ticket", webWXDataTicket);
                    request.content("pass_ticket", passTicket);
                    request.content("filename", new File(arrayList.get((int)sliceIndex)));
                    result = XTools.http(httpExecutor, request).string();

           /*         WeChatToolXXX.Slice slice = new WeChatToolXXX.Slice("filename", fileName, fileMime, sliceBuffer, 0);
                    int readCount;
                    while ((readCount = bfinStream.read(sliceBuffer, slice.count, sliceBuffer.length - slice.count)) > 0) {
                        slice.count += readCount;
                        if (slice.count >= sliceBuffer.length) {
                            break;
                        }
                    }

                    request.content("filename", slice);
                    result = XTools.http(httpExecutor, request).string();*/
                   //System.out.println(result);

                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bfinStream != null) {
                    try {
                        bfinStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        return JSON.parseObject(result);
    }
    /**
     * 大文件分片方法1：普通IO方式
     * @param filePath 文件路径
     * */
    public static ArrayList<String> splitFile1(String filePath){
        InputStream bis=null;//输入流用于读取文件数据
        OutputStream bos=null;//输出流用于输出分片文件至磁盘
        ArrayList<String>  arrayList = new ArrayList<String>();
        try {
            File file=new File(filePath);
            bis=new BufferedInputStream(new FileInputStream(file));
            long splitSize=512*1024;//单片文件大小
            long writeByte=0;//已读取的字节数
            int len=0;
            byte[] bt=new byte[1024];
            while (-1!=(len=bis.read(bt))) {
                if(writeByte%splitSize==0){
                    if(bos!=null){
                        bos.flush();
                        bos.close();
                    }
                    String partFile = filePath+"."+(writeByte/splitSize+1)+".part";
                    arrayList.add(partFile);
                    bos=new BufferedOutputStream(new FileOutputStream(partFile));
                }
                writeByte+=len;
                bos.write(bt, 0, len);
            }
            System.out.println("文件分片成功！");
        } catch (Exception e) {
            System.out.println("文件分片失败！");
            e.printStackTrace();
        }finally {
            try {
                if(bos!=null){
                    bos.flush();
                    bos.close();
                }
                if(bis!=null){
                    bis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }


    /**
     * 根据用户id发送图片消息
     *
     * @param userId
     * @param filePath
     * @return
     * @author SXS
     * @date 2017年5月7日 下午10:34:24
     */
    public static SendMsgResponse sendPicMsgByUserId(String userId, String filePath) {
        JSONObject responseObj = webwxuploadmedia(filePath, Core.getUserName(), userId);
        if (responseObj != null) {
            String mediaId = responseObj.getString("MediaId");
            if (StringUtils.isEmpty(mediaId)) {
                return null;
            }
            String url = String.format(URLEnum.WEB_WX_SEND_PIC_MSG.getUrl(), Core.getLoginInfoMap().get("url"),
                    Core.getLoginInfoMap().get("pass_ticket"));
            Map<String, Object> msgMap = new HashMap<String, Object>();
            msgMap.put("Type", WXSendMsgCodeEnum.PIC.getCode());
            msgMap.put("MediaId", mediaId);
            msgMap.put("FromUserName", Core.getUserSelf().getString("UserName"));
            msgMap.put("ToUserName", userId);
            String clientMsgId = String.valueOf(new Date().getTime())
                    + String.valueOf(new Random().nextLong()).substring(1, 5);
            msgMap.put("LocalID", clientMsgId);
            msgMap.put("ClientMsgId", clientMsgId);
            Map<String, Object> paramMap = Core.getParamMap();
            paramMap.put("BaseRequest", Core.getParamMap().get("BaseRequest"));
            paramMap.put("Msg", msgMap);
            String paramStr = JSON.toJSONString(paramMap);
            HttpEntity entity = MyHttpClient.doPost(url, paramStr);
            if (entity != null) {
                try {
                    String result = EntityUtils.toString(entity, Consts.UTF_8);
                    return JSON.parseObject(result, SendMsgResponse.class);
                } catch (Exception e) {
                    log.error("webWxSendMsgImg 错误： ", e);
                }
            }
            return null;

        }
        return null;
    }

    /**
     * 根据用户id发送撤回消息
     *
     * @param userId
     * @param
     * @return
     * @author SXS
     * @date 201714年5月7日 下午10:34:24
     */
    public static boolean sendRevokeMsgByUserId(String userId, String clientMsgId, String svrMsgId) {

        String url = String.format(URLEnum.WEB_WX_REVOKE_MSG.getUrl()
                , URLEnum.BASE_URL.getUrl(), Core.getLoginInfoMap().get("pass_ticket"));

        Map<String, Object> msgMap = new HashMap<String, Object>();
        msgMap.put("ClientMsgId", clientMsgId);
        msgMap.put("SvrMsgId", svrMsgId);
        msgMap.put("ToUserName", userId);
        Map<String, Object> paramMap = Core.getParamMap();
        paramMap.put("BaseRequest", Core.getParamMap().get("BaseRequest"));
        paramMap.put("Msg", msgMap);
        String paramStr = JSON.toJSONString(paramMap);
        HttpEntity entity = MyHttpClient.doPost(url, paramStr);
        if (entity != null) {
            try {
                String result = EntityUtils.toString(entity, Consts.UTF_8);
                return JSON.parseObject(result).getJSONObject("BaseResponse").getInteger("Ret") == 0;
            } catch (Exception e) {
                log.error("webWxSendMsgImg 错误： ", e);
            }
        }
        return false;
    }

    /**
     * 根据用户id发送视频消息
     *
     * @param userId
     * @param filePath
     * @return
     * @author SXS
     * @date 201714年5月7日 下午10:34:24
     */
    public static SendMsgResponse sendVideoMsgByUserId(String userId, String filePath) {
        JSONObject responseObj = webwxuploadmedia(filePath, Core.getUserName(), userId);
        if (responseObj != null) {
            String mediaId = responseObj.getString("MediaId");
            if (StringUtils.isEmpty(mediaId)) {
                return null;
            }
            String url = String.format(URLEnum.WEB_WX_SEND_VIDEO_MSG.getUrl(), Core.getLoginInfoMap().get("url"),
                    Core.getLoginInfoMap().get("pass_ticket"));

            Map<String, Object> msgMap = new HashMap<String, Object>();
            String clientMsgId = String.valueOf(new Date().getTime())
                    + String.valueOf(new Random().nextLong()).substring(1, 5);

            msgMap.put("ClientMsgId", clientMsgId);
            msgMap.put("FromUserName", Core.getUserSelf().getString("UserName"));
            msgMap.put("LocalID", clientMsgId);

            msgMap.put("MediaId", mediaId);
            msgMap.put("ToUserName", userId);
            msgMap.put("Type", WXSendMsgCodeEnum.VIDEO.getCode());
            Map<String, Object> paramMap = Core.getParamMap();
            paramMap.put("BaseRequest", Core.getParamMap().get("BaseRequest"));
            paramMap.put("Msg", msgMap);
            String paramStr = JSON.toJSONString(paramMap);
            HttpEntity entity = MyHttpClient.doPost(url, paramStr);
            if (entity != null) {
                try {
                    String result = EntityUtils.toString(entity, Consts.UTF_8);
                    return JSON.parseObject(result, SendMsgResponse.class);
                } catch (Exception e) {
                    log.error("webWxSendMsgImg 错误： ", e);
                }
            }
            return null;

        }
        return null;
    }

    /**
     * 根据用户id发送文件
     *
     * @param userId
     * @param filePath
     * @return
     * @author SXS
     * @date 2017年5月7日 下午11:57:36
     */
    public static SendMsgResponse sendFileMsgByUserId(String userId, String filePath) {
        String title = new File(filePath).getName();
        Map<String, String> data = new HashMap<String, String>();
        data.put("appid", Config.API_WXAPPID);
        data.put("title", title);
        data.put("totallen", "");
        data.put("attachid", "");
        data.put("type", WXSendMsgCodeEnum.APP.getCode() + ""); // APPMSGTYPE_ATTACH
        data.put("fileext", title.split("\\.")[1]); // 文件后缀
        JSONObject responseObj = null;



        responseObj = webwxuploadmedia(filePath, Core.getUserName(), userId);
        if (responseObj != null) {
            data.put("totallen", responseObj.getString("StartPos"));
            data.put("attachid", responseObj.getString("MediaId"));
        } else {
            log.error("sednFileMsgByUserId 错误: ", data);
        }
        return webWxSendAppMsg(userId, data);
    }

    /**
     * 内部调用
     *
     * @param userId
     * @param data
     * @return
     * @author SXS
     * @date 2017年5月10日 上午12:21:28
     */
    private static SendMsgResponse webWxSendAppMsg(String userId, Map<String, String> data) {
        String url = String.format("%s/webwxsendappmsg?fun=async&f=json&pass_ticket=%s", Core.getLoginInfoMap().get("url"),
                Core.getLoginInfoMap().get("pass_ticket"));
        String clientMsgId = String.valueOf(new Date().getTime())
                + String.valueOf(new Random().nextLong()).substring(1, 5);
        String content = "<appmsg appid='wxeb7ec651dd0aefa9' sdkver=''><title>" + data.get("title")
                + "</title><des></des><action></action><type>6</type><content></content><url></url><lowurl></lowurl>"
                + "<appattach><totallen>" + data.get("totallen") + "</totallen><attachid>" + data.get("attachid")
                + "</attachid><fileext>" + data.get("fileext") + "</fileext></appattach><extinfo></extinfo></appmsg>";
        Map<String, Object> msgMap = new HashMap<String, Object>();
        msgMap.put("Type", data.get("type"));
        msgMap.put("Content", content);
        msgMap.put("FromUserName", Core.getUserSelf().getString("UserName"));
        msgMap.put("ToUserName", userId);
        msgMap.put("LocalID", clientMsgId);
        msgMap.put("ClientMsgId", clientMsgId);
        /*
         * Map<String, Object> paramMap = new HashMap<String, Object>();
         *
         * @SuppressWarnings("unchecked") Map<String, Map<String, String>>
         * baseRequestMap = (Map<String, Map<String, String>>)
         * Core.getLoginInfo() .get("baseRequest"); paramMap.put("BaseRequest",
         * baseRequestMap.get("BaseRequest"));
         */

        Map<String, Object> paramMap = Core.getParamMap();
        paramMap.put("Msg", msgMap);
        paramMap.put("Scene", 0);
        String paramStr = JSON.toJSONString(paramMap);
        HttpEntity entity = MyHttpClient.doPost(url, paramStr);
        if (entity != null) {
            try {
                String result = EntityUtils.toString(entity, Consts.UTF_8);
                return JSON.parseObject(result, SendMsgResponse.class);
            } catch (Exception e) {
                log.error("错误: ", e);
            }
        }
        return null;
    }

    /**
     * 被动添加好友
     *
     * @param msg
     * @param accept true 接受 false 拒绝
     * @date 2017年6月29日 下午10:08:43
     */
    public static void addFriend(AddMsgList msg, boolean accept) {
        if (!accept) { // 不添加
            return;
        }
        int status = VerifyFriendEnum.ACCEPT.getCode(); // 接受好友请求
        RecommendInfo recommendInfo = msg.getRecommendInfo();
        String userName = recommendInfo.getUserName();
        String ticket = recommendInfo.getTicket();
        // 更新好友列表
        // TODO 此处需要更新好友列表
        // Core.getContactList().add(msg.getJSONObject("RecommendInfo"));

        String url = String.format(URLEnum.WEB_WX_VERIFYUSER.getUrl(), Core.getLoginInfoMap().get("url"),
                String.valueOf(System.currentTimeMillis() / 3158L), Core.getLoginInfoMap().get("pass_ticket"));

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
        } catch (Exception e) {
            log.error("webWxSendMsg", e);
        }

        if (StringUtils.isBlank(result)) {
            log.error("被动添加好友失败");
        }

        log.debug(result);

    }

    /**
     * 回复的消息类型封装
     */
    @Builder
    public static class Result {
        //消息类型
        private final WXSendMsgCodeEnum replyMsgTypeEnum;
        //文本消息或图片、文件消息的文件路径路径
        private final String msg;
        //延迟发送
        private final Long sleep;
        //消息接收者
        private  String toUserName;
    }


}
