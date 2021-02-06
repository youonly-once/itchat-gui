package cn.shu;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bean.tuling.enums.ResultType;
import bean.tuling.response.Results;
import bean.tuling.response.TuLingResponseBean;
import cn.shu.weichat.Wechat;
import cn.shu.weichat.api.MessageTools;
import cn.shu.weichat.api.WechatTools;
import cn.shu.weichat.beans.BaseMsg;
import cn.shu.weichat.core.Core;
import cn.shu.weichat.face.IMsgHandlerFace;
import cn.shu.weichat.utils.LogUtil;
import cn.shu.weichat.utils.SleepUtils;
import cn.shu.weichat.utils.XmlUtil;
import cn.shu.weichat.utils.enums.MsgTypeEnum;
import cn.shu.weichat.utils.enums.MsgTypeOfAppEnum;
import cn.shu.weichat.utils.enums.ReplyMsgTypeEnum;
import cn.shu.weichat.utils.tools.DownloadTools;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.log4j.Log4j2;
import net.sf.json.JSONException;
import org.apache.commons.lang.StringUtils;
import org.nlpcn.commons.lang.util.StringUtil;

import utils.DateUtil;
import utils.HttpUtil;
import utils.TuLingUtil;
import weixin.exception.WXException;
import weixin.utils.WXUntil;

@Log4j2
public class IMsgHandlerFaceImpl implements IMsgHandlerFace {
    private final Properties pps = new Properties();
    private String msgFileName = "msg.property";
    private int count = 1;
    private boolean autoReply = false;

    private static final Core core = Core.getInstance();
    public String savePath = "D://weixin";

    private final Set<String> groupIdList = new HashSet<>();

    //不处理撤回消息的群名列表
    private final Set<String> nonHandleUndoMsgGroupId = new HashSet<>();

    public IMsgHandlerFaceImpl() {


        String qrPath = savePath + File.separator + "login";
        Wechat wechat = new Wechat(IMsgHandlerFaceImpl.this, qrPath);
        wechat.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000 * 60 * 60 );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                msgFileName = DateUtil.getCurrDate() + "msg.property";
            }
        }, "DelMsgThread").start();

    }

    /**
     * 存储发送的消息
     *
     * @param msgId 消息id
     * @param msg   消息内容
     */
    private void storeMsg(String msgId, String msg) {
        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(new FileOutputStream(msgFileName, true), StandardCharsets.UTF_8);
            pps.setProperty(msgId, msg);
            pps.store(outputStreamWriter, DateUtil.getCurrDateAndTimeMil());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStreamWriter != null) {
                try {
                    outputStreamWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    /**
     * 读取消息
     *
     * @param msgId
     */
    private String loadMsg(String msgId) {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(new FileInputStream(msgFileName), StandardCharsets.UTF_8);
            pps.load(inputStreamReader);
            return pps.getProperty(msgId);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return "";
    }

    private void delMsg(String msgId) {
        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(new FileOutputStream(msgFileName, true), StandardCharsets.UTF_8);
            pps.remove(msgId);
            pps.store(outputStreamWriter, DateUtil.getCurrDateAndTimeMil());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStreamWriter != null) {
                try {
                    outputStreamWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }


    @Override
    public List<MessageTools.Result> textMsgHandle(BaseMsg msg) {
        log.info(LogUtil.printFromMeg(msg, "", MsgTypeEnum.TEXT.getCode()));
        String text = msg.getText();
        //存储消息
        storeMsg(msg.getMsgId(), MsgTypeEnum.TEXT.getType() + ":" + msg.getFromUserName() + "-" + text);
        List<MessageTools.Result> results = new ArrayList<>();

        //=========================手动发送消息=====================
        String[] split = text.split("：");
        if (split.length >= 2) {
        try {
            long sleep = 100;
                try{
                    sleep = Long.parseLong(split[2]);
                }catch (ArrayIndexOutOfBoundsException e){

                }
                String s = split[1];
                int i = Integer.parseInt(s);
                results.add(MessageTools.Result.builder()
                        .msg("开始发送：" + i + "个" + split[0])
                        .toUserName(msg.getToUserName())
                        .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                        .build());
                for (int j = 0; j < i; j++) {
                    results.add(MessageTools.Result.builder()
                            .msg(split[0])
                            .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                            .sleep(sleep)
                            .toUserName(msg.getToUserName())
                            .build());
                }
                return results;

            }catch(NumberFormatException e){
            }
        }
        //=============================================================

        //============炸弹消息===================
        if (text.contains("[Bomb]")) {
            String userName = core.getUserSelf().getString("UserName");
            if (!msg.getFromUserName().equals(userName) ) {
                    for (int i = 0; i < 1; i++) {
                        results.add(MessageTools.Result.builder()
                                .msg("[Bomb]")
                                .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                                .sleep((long) (Math.random() * (10 - 1) + 1))
                                .build());
                    }
                    return results;
            }
        } else if (text.toUpperCase().equals("C")) {
            autoReply = false;
            results.add(MessageTools.Result.builder().replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT).toUserName("filehelper").msg("已关闭自动回复。").build());
            log.info("已关闭自动回复。");
            return results;
        } else if (text.toUpperCase().equals("O")) {
            results.add(MessageTools.Result.builder().replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT).toUserName("filehelper").msg("已开启自动回复。").build());
            log.info("已开启自动回复。");
            autoReply = true;
            return results;
        }
        //===========群防撤回功能开关
        else if (msg.getGroupMsg()
                && text.toUpperCase().equals("UNDO")) {
            nonHandleUndoMsgGroupId.add(msg.getToUserName());
            results.add(MessageTools.Result.builder().replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                    .msg("已关闭群【"+WechatTools.getRemarkNameByGroupUserName(msg.getToUserName())+"】防撤回功能")
                    .toUserName("filehelper").build());
            log.info("已关闭群【"+WechatTools.getRemarkNameByGroupUserName(msg.getToUserName())+"】防撤回功能");
            return results;
        }
        //#######我自己在群里发的消息  控制命令#######
        else if (msg.getGroupMsg()
                && msg.getFromUserName().equals(core.getUserSelf().getString("UserName"))) {
            if ("OG".equals(msg.getContent().toUpperCase())) {//开启群回复
                groupIdList.add((msg.getToUserName()));
                results.add(MessageTools.Result.builder().replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                        .msg("已开启群【"+WechatTools.getRemarkNameByGroupUserName(msg.getToUserName())+"】自动回复")
                        .toUserName("filehelper").build());
                log.info("已开启群【"+WechatTools.getRemarkNameByGroupUserName(msg.getToUserName())+"】自动回复");
            } else if ("CG".equals(msg.getContent().toUpperCase())) {//关闭群回复
                groupIdList.remove(msg.getToUserName());
                results.add(MessageTools.Result.builder().replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                        .msg("已关闭群【"+WechatTools.getRemarkNameByGroupUserName(msg.getToUserName())+"】自动回复")
                        .toUserName("filehelper").build());
                log.info("已关闭群【"+WechatTools.getRemarkNameByGroupUserName(msg.getToUserName())+"】自动回复");
            }
            return results;
        }

        //============炸弹消息= 结束==================
        //============接龙消息========================
        //#接龙<br/>周三 健身房<br/><br/>1. 潘洁
        String userName = core.getUserSelf().getString("UserName");
        if (!msg.getFromUserName().equals(userName)) {
            String regex = "#接龙<br/>.*<br/><br/>.*(\\d+)\\.(.+)$";
            Pattern compile = Pattern.compile(regex);
            Matcher matcher = compile.matcher(text);
            if (matcher.find()) {
                if (!text.contains("舒新胜")) {
                    try {
                        String group = matcher.group(matcher.groupCount() - 1);
                        int num = Integer.parseInt(group);
                        results.add(MessageTools.Result.builder()
                                .msg(text + "<br/>" + (++num) + ". 舒新胜")
                                .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                                .build());
                        return results;

                    } catch (Exception e) {

                    }
                }
            }
        }
        //============接龙消息====结束====================

        text = isReply(msg);
        if (text.isEmpty()) {
            return results;
        }
        try {
            results = handleTuLingMsg(TuLingUtil.robotMsgTuling(text), msg);
        } catch (JSONException | NullPointerException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return results;
    }

    /*
     * 图片消息(non-Javadoc)
     * @see X.cn.zhouyafeng.itchat4j.face.IMsgHandlerFace#picMsgHandle(com.alibaba.fastjson.JSONObject)
     */
    @Override
    public List<MessageTools.Result> picMsgHandle(BaseMsg msg) {
        String path = downloadFile(msg, ".gif", MsgTypeEnum.PIC); // 调用此方法来保存图片
        log.info(LogUtil.printFromMeg(msg, path, MsgTypeEnum.PIC.getCode()));
        if (StringUtil.isNotBlank(path)) {
            storeMsg(msg.getMsgId(), MsgTypeEnum.PIC.getType() + ":" + msg.getFromUserName() + "-" + path);
        }
        return null;
    }

    /*
     * 语音消息(non-Javadoc)
     * @see X.cn.zhouyafeng.itchat4j.face.IMsgHandlerFace#voiceMsgHandle(com.alibaba.fastjson.JSONObject)
     */
    @Override
    public List<MessageTools.Result> voiceMsgHandle(BaseMsg msg) {
        // 调用此方法来保存语音
        String path = downloadFile(msg, ".mp3", MsgTypeEnum.VOICE);
        log.info(LogUtil.printFromMeg(msg, path, MsgTypeEnum.VOICE.getCode()));
        //存储消息
        if (StringUtil.isNotBlank(path)) {
            storeMsg(msg.getMsgId(), MsgTypeEnum.VOICE.getType() + ":" + msg.getFromUserName() + "-" + path);
        }
        return null;
    }

    private String downloadFile(BaseMsg msg, String ext, MsgTypeEnum msgTypeEnum) {
        //发消息的用户或群名称
        String username = core.getRemarkNameByUserName(msg.getFromUserName());
        //群成员名称
        String groupUsername;
        if (msg.getMemberName() != null) {
            groupUsername = core.getRemarkNameByUserName(msg.getMemberName()) + "-";
        } else {
            groupUsername = "";
        }
        String fileName = groupUsername + "-"
                + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + "-" + msg.getNewMsgId()
                + ext;
        fileName = delete(fileName);
        username = delete(username);
        // 保存语音的路径
        String path = savePath + File.separator + msgTypeEnum + File.separator + username + File.separator;
        boolean logDir = createLogDir(path);
        if (logDir) {
            DownloadTools.getDownloadFn(msg, msgTypeEnum, path + fileName);
        } else {
            return null;
        }

        return path + fileName;
    }

    @Override
    public List<MessageTools.Result> videoMsgHandle(BaseMsg msg) {
        String path = downloadFile(msg, ".mp4", MsgTypeEnum.VIDEO);
        log.info(LogUtil.printFromMeg(msg, path, MsgTypeEnum.VIDEO.getCode()));
        if (StringUtil.isNotBlank(path)) {
            storeMsg(msg.getMsgId(), MsgTypeEnum.VIDEO.getType() + ":" + msg.getFromUserName() + "-" + path);
        }
        return null;
    }

    @Override
    public List<MessageTools.Result> undoMsgHandle(BaseMsg msg) {
		/* 撤回消息格式
		#1108768584572118898为被撤回消息ID
		<sysmsg type="revokemsg">
		<revokemsg>
			<session>wxid_lolnmwoj459722</session>
			<oldmsgid>1700173602</oldmsgid>
			<msgid>1108768584572118898</msgid>
			<replacemsg>
				<![CDATA["hello" recalled a message]]>
			</replacemsg>
		</revokemsg>
		</sysmsg>
		*/
        /*============获取被撤回的消息============*/
        String content = msg.getContent();
        content = content.replace("&lt;", "<").replace("&gt;", ">");
        content = "<root>" + content + "</root>";
        Map<String, Object> map = XmlUtil.toMap(content);
        Object msgid = map.get("msgid");
        if (msgid == null) {
            return null;
        }
        String value = loadMsg(msgid.toString());
        if (value == null) {
            return null;
        }
        //======家人群不发送撤回消息====
        if (msg.getFromUserName().startsWith("@@")) {
            String to = WechatTools.getRemarkNameByGroupUserName(msg.getFromUserName());
            if ("<span class=\"emoji emoji2764\"></span>汪家人<span class=\"emoji emoji2764\"></span>".equals(to)) {
                log.error("家人群，不发送撤回消息");
                return null;
            }
            //不处理群消息
            if (nonHandleUndoMsgGroupId.contains(msg.getFromUserName())) {
                return null;
            }
        }

        //==============是否为自己的消息
        String oldMsgFromUserName = value.substring(value.indexOf(":") + 1, value.indexOf("-"));
        JSONObject userSelf = core.getUserSelf();
        //自己的撤回消息不发送
        if (userSelf.getString("UserName").equals(oldMsgFromUserName)) {
            // return null;
        }
        //===============
        ArrayList<MessageTools.Result> results = new ArrayList<>();
        MessageTools.Result result = null;
        //撤回消息用户的昵称
        String fromNickName = "";
        if (msg.getGroupMsg()) {
            fromNickName = WechatTools.getMemberDisplayNameOfGroup(msg.getFromUserName(), msg.getMemberName() == null ? oldMsgFromUserName : msg.getMemberName());
        } else {
            fromNickName = WechatTools.getRemarkNameByUserName(msg.getFromUserName());
        }

        String msgType = value.substring(0, value.indexOf(":"));
        String realMsgContent = value.substring(value.indexOf("-") + 1);
        switch (MsgTypeEnum.valueOf(msgType)) {
            case TEXT:
                result = MessageTools.Result.builder()
                        .msg("【" + fromNickName + "】撤回的消息：" + realMsgContent)
                        .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                        .build();
                results.add(result);
                break;
            case PIC:
                result = MessageTools.Result.builder()
                        .msg("【" + fromNickName + "】撤回的图片：")
                        .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                        .build();
                results.add(result);
                result = MessageTools.Result.builder()
                        .msg(realMsgContent)
                        .replyMsgTypeEnum(ReplyMsgTypeEnum.PIC)
                        .build();
                results.add(result);
                break;
            case EMOTION:
                result = MessageTools.Result.builder()
                        .msg("【" + fromNickName + "】撤回的表情：")
                        .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                        .build();
                results.add(result);
                result = MessageTools.Result.builder()
                        .msg(realMsgContent)
                        .replyMsgTypeEnum(ReplyMsgTypeEnum.PIC)
                        .build();
                results.add(result);
                break;
            case VOICE:
                result = MessageTools.Result.builder()
                        .msg("【" + fromNickName + "】撤回的语音：")
                        .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                        .build();
                results.add(result);
                result = MessageTools.Result.builder()
                        .msg(realMsgContent)
                        .replyMsgTypeEnum(ReplyMsgTypeEnum.VOICE)
                        .build();
                results.add(result);
                break;
            case VIDEO:
                result = MessageTools.Result.builder()
                        .msg("【" + fromNickName + "】撤回的视频：")
                        .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                        .build();
                results.add(result);
                result = MessageTools.Result.builder()
                        .msg(realMsgContent)
                        .replyMsgTypeEnum(ReplyMsgTypeEnum.VIDEO)
                        .build();
                results.add(result);
                break;
            case MAP:
                result = MessageTools.Result.builder()
                        .msg("【" + fromNickName + "】撤回的定位：" + realMsgContent)
                        .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                        .build();
                results.add(result);
                break;
            case NAMECARD:
                result = MessageTools.Result.builder()
                        .msg("【" + fromNickName + "】撤回的联系人名片：" + realMsgContent)
                        .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                        .build();
                results.add(result);
                break;
            case FAVOURITEOFAPP:
                result = MessageTools.Result.builder()
                        .msg("【" + fromNickName + "】撤回的收藏消息：" + realMsgContent)
                        .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                        .build();
                results.add(result);
                break;
            case APP:
                //目前是分享的链接

                //目前是文件消息
                result = MessageTools.Result.builder()
                        .msg("【" + fromNickName + "】撤回的APP消息：")
                        .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                        .build();
                results.add(result);
                result = MessageTools.Result.builder()
                        .msg(realMsgContent)
                        .replyMsgTypeEnum(ReplyMsgTypeEnum.APP)
                        .build();
                results.add(result);
                break;

        }
        return results;
    }

    @Override
    public List<MessageTools.Result> addFriendMsgHandle(BaseMsg msg) {
        log.info(LogUtil.printFromMeg(msg, MsgTypeEnum.ADDFRIEND.getCode()));
        String text = isReply(msg);
        return null;
    }

    @Override
    public List<MessageTools.Result> systemMsgHandle(BaseMsg msg) {
        log.info(LogUtil.printFromMeg(msg, MsgTypeEnum.SYSTEM.getCode()));
        String text = isReply(msg);
        return null;
    }

    @Override
    public List<MessageTools.Result> emotionMsgHandle(BaseMsg msg) {
        String path = downloadFile(msg, ".gif", MsgTypeEnum.EMOTION); // 调用此方法来保存图片
        log.info(LogUtil.printFromMeg(msg, path, MsgTypeEnum.EMOTION.getCode()));
        if (StringUtil.isNotBlank(path)) {
            storeMsg(msg.getMsgId(), MsgTypeEnum.EMOTION.getType() + ":" + msg.getFromUserName() + "-" + path);
        }
        return null;
    }

    @Override
    public List<MessageTools.Result> appMsgHandle(BaseMsg msg) {
        log.info(LogUtil.printFromMeg(msg, MsgTypeEnum.APP.getCode()));
        MsgTypeOfAppEnum byCode = MsgTypeOfAppEnum.getByCode(msg.getAppMsgType());
        switch (byCode) {
            case UNKNOWN:
                break;
            case FAVOURITE:
                storeMsg(msg.getMsgId(), MsgTypeEnum.FAVOURITEOFAPP.getType() + ":" + msg.getFromUserName() + "-" + msg.getUrl());
                break;
            case FILE:
                //文件消息
                String path = downloadFile(msg, msg.getFileName().substring(msg.getFileName().lastIndexOf(".")), MsgTypeEnum.APP);
                //存储消息
                if (StringUtil.isNotBlank(path)) {
                    storeMsg(msg.getMsgId(), MsgTypeEnum.APP.getType() + ":" + msg.getFromUserName() + "-" + path);
                }
                break;
        }
        return null;
    }

    @Override
    public List<MessageTools.Result> verifyAddFriendMsgHandle(BaseMsg msg) {
        log.info(LogUtil.printFromMeg(msg, "VerifyAddFriendMsg"));
        return null;
    }

    @Override
    public List<MessageTools.Result> mapMsgHandle(BaseMsg msg) {
        // String path = downloadFile(msg, ".gif", MsgTypeEnum.MAP); // 调用此方法来保存图片
        log.info(LogUtil.printFromMeg(msg, MsgTypeEnum.MAP.getCode()));
        storeMsg(msg.getMsgId(), MsgTypeEnum.MAP.getType() + ":" + msg.getFromUserName() + "-" + msg.getMemberName()+","+msg.getUrl());
        return null;
    }

    @Override
    public List<MessageTools.Result> mediaMsgHandle(BaseMsg msg) {
        log.info(LogUtil.printFromMeg(msg, MsgTypeEnum.MEDIA.getCode()));
        return null;
    }

    @Override
    public List<MessageTools.Result> nameCardMsgHandle(BaseMsg msg) {
        log.info(LogUtil.printFromMeg(msg, MsgTypeEnum.NAMECARD.getCode()));
        String content = msg.getContent();
        content = content.replace("&lt;", "<").replace("&gt;", ">").replace("<br/>", "");
        //   content = "<root>" + content + "</root>";
        Map<String, Object> map = XmlUtil.toMap(content);
        Map<String, String> msgMap = (Map<String, String>) map.get("msg_V");
        storeMsg(msg.getMsgId(), MsgTypeEnum.NAMECARD.getType() + ":" + msg.getFromUserName() + "-"
                + msgMap.get("username") + "," + msgMap.get("nickname"));
        return null;
    }

    /**
     * 处理图灵消息
     *
     * @param tl
     * @return
     */
    private List<MessageTools.Result> handleTuLingMsg(TuLingResponseBean tl, BaseMsg msg) {
        ArrayList<MessageTools.Result> msgResults = new ArrayList<>();
        List<Results> results = tl.getResults();
        for (Results result : results) {
            String msgStr = result.getValues().getText();
            if (msg.getGroupMsg() && msg.getMentionMeUserNickName() != null) {
                msgStr = "@" + msg.getMentionMeUserNickName() + " " + msgStr;
            }
            MessageTools.Result.ResultBuilder msgBuilder = MessageTools.Result.builder()
                    .msg(msgStr);
            switch (ResultType.getByCode(result.getResultType())) {
                case URL:
                case NEWS:
                case TEXT:
                    msgBuilder.replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT);
                    break;
                case IMAGE:
                    msgBuilder.replyMsgTypeEnum(ReplyMsgTypeEnum.PIC);
                    break;
                case VIDEO:
                    msgBuilder.replyMsgTypeEnum(ReplyMsgTypeEnum.VIDEO);
                    break;
                case VOICE:
                    msgBuilder.replyMsgTypeEnum(ReplyMsgTypeEnum.VOICE);
                    break;
                case DEFAULT:
            }
            msgResults.add(msgBuilder.build());
        }
        return msgResults;
    }

    private String  isReply(BaseMsg m) {
        if (!autoReply) {
            //log.info("已关闭自动回复。");
            return "";
        }
        //是否需要回复
        boolean isReply = true;
        String fromUserName = m.getFromUserName();
        String toUserName = m.getToUserName();
        String content = m.getText();
        String msgId = m.getNewMsgId();
        Boolean groupMsg = m.getGroupMsg();
        if (content == null) {
            return "";
        }
        JSONObject userSelf = core.getUserSelf();
        //#############群消息############
        if (groupMsg) {
            if (groupIdList.contains(fromUserName)) {
                isReply = true;
            } else {
                isReply = false;
            }

        }

        if (isReply) {
            return content;
        } else {
            return "";
        }

    }

    /*	private String uploadVoice(String voicePath) {
            String msgId = "图片上传失败，请联系管理员";

            log.info(voicePath);
            File file = new File(voicePath);
            String accessToken = wXUntil.getLocalServerAccessToken(false);
            // 获取token失败
            if (accessToken == null) {
                return "设置失败，请联系管理员(token获取失败)";
            }
            String url = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token=" + accessToken + "&type=voice";
            String resultJson = HttpUtil.sendFileWeixin(url, file);
            if (resultJson != null) {
                log.info("上传图片返回：" + resultJson);
                Map<String, Object> map = null;
                try {
                    map = JSONUtils.parseJSON2Map(resultJson);
                } catch (JSONException e) {
                    // TODO: handle exception
                    return msgId;
                }

                if (map != null && map.size() > 0) {
                    if (map.containsKey("media_id")) {
                        msgId = (String) map.get("media_id");

                    } else if (map.containsKey("errcode")) {
                        if (wXUntil.tokenExpire(map.get("errcode").toString(),map.get("errmsg").toString())) {


                            if(wXUntil.getLocalServerAccessToken(true)!=null){

                                msgId = uploadVoice(voicePath);
                            }else{//获取token失败 5秒后重试一次
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                if(wXUntil.getLocalServerAccessToken(true)!=null){
                                    msgId = uploadVoice(voicePath);
                                }
                            }

                        }
                    }
                }
            }

            return msgId;
        }
        */
    /*
     * 不可建立文件夹的字符
     */
    private String delete(String string) {
        string = string.replace("/", "");
        string = string.replace("\\", "");
        string = string.replace("*", "");
        string = string.replace(":", "");
        string = string.replace("\"", "");
        string = string.replace("?", "");
        string = string.replace("<", "");
        string = string.replace(">", "");
        return string;

    }

    private String voice2Text(String mediaId, String voicePath) throws WXException, IOException {
        String text = "";
        //
        // if (errcode.equals("0")) { return true; } else if
        // (weixinBasicInfo.tokenExpire(errcode)) {// token过期
        //  重新POST
        String token = WXUntil.getLocalServerAccessToken();
        String uploadurl = "http://api.weixin.qq.com/cgi-bin/media/voice/addvoicetorecofortext?" + "access_token="
                + token + "&format=mp3&voice_id=" + mediaId + "&lang=zh_CN";
        String transurl = "http://api.weixin.qq.com/cgi-bin/media/voice/queryrecoresultfortext?" + "access_token="
                + token + "&voice_id=" + mediaId + "&lang=zh_CN";
        try {
            // 上传语音
            File file = new File(voicePath);
            String resultStr = HttpUtil.sendFileWeixin(uploadurl, file);
            log.info(resultStr);
            net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(resultStr);

            // 上传成功 获取文字结果
            String errcode = jsonObject.getString("errcode");
            if (errcode.equals("0")) {
                SleepUtils.sleep(3000);
                String resultStr1 = HttpUtil.sendPost(transurl, "");
                log.info(resultStr1);
                net.sf.json.JSONObject jsonObject1 = net.sf.json.JSONObject.fromObject(resultStr1);
                // 根据文字做相应处理
                if (!jsonObject1.containsKey("result")) {
                    // token过期
                    if (jsonObject1.containsKey("errcode")
                            && WXUntil.tokenExpire("2020-09-21")) {
                        // token过期重新尝试
                        if (count >= 3) {
                            text = "语音识别失败：" + jsonObject1.getString("errmsg");
                        } else if (WXUntil.tokenExpire("2020-09-21")) {
                            WXUntil.getLocalServerAccessToken();
                            log.info("重新尝试语音识别");
                            count++;
                            text = voice2Text(mediaId, voicePath);

                        }
                    } else {
                        text = "语音识别失败：" + jsonObject1.getString("errmsg");
                    }

                } else {
                    text = jsonObject1.getString("result");
                }

                // token过期重新尝试
            } else if (WXUntil.tokenExpire("2020-09-21")) {
                WXUntil.getLocalServerAccessToken();
                log.info("重新尝试语音识别");
                count++;
                text = voice2Text(mediaId, voicePath);
            } else {
                text = "语音识别失败：" + jsonObject.getString("errmsg");
            }
        } catch (NullPointerException | JSONException e) {
            // TODO: handle exception
            e.printStackTrace();
            text = "语音识别失败：" + e.getMessage();
        }

        log.info(text);

        return text;
    }

    public String voice2Text(String mediaId) throws JSONException, NullPointerException, WXException, IOException {
        String token = WXUntil.getLocalServerAccessToken();
        String uploadurl = "http://api.weixin.qq.com/cgi-bin/media/voice/addvoicetorecofortext?" + "access_token="
                + token + "&format=mp3&voice_id=" + mediaId + "&lang=zh_CN";
        String transurl = "http://api.weixin.qq.com/cgi-bin/media/voice/queryrecoresultfortext?" + "access_token="
                + token + "&voice_id=" + mediaId + "&lang=zh_CN";
        HttpUtil.sendPost(uploadurl, "");
        SleepUtils.sleep(3000);
        String resultStr = HttpUtil.sendPost(transurl, "");
        log.info(resultStr);
        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(resultStr);
        String text = "";
        // 根据文字做相应处理
        if (!jsonObject.containsKey("result")) {
            // token过期
            if (jsonObject.containsKey("errcode") && WXUntil.tokenExpire("2020-09-21")) {
                // token过期重新尝试
                if (count >= 3) {
                    text = "语音识别失败：" + jsonObject.getString("errmsg") + "," + jsonObject.getString("errmsg");
                } else if (WXUntil.tokenExpire("2020-09-21")) {
                    WXUntil.getLocalServerAccessToken();
                    log.info("重新尝试语音识别");
                    count++;
                    text = voice2Text(mediaId);

                }
            } else {
                text = "语音识别失败：" + jsonObject.getString("errmsg") + "," + jsonObject.getString("errmsg");
            }

        } else {
            text = jsonObject.getString("result");
        }
        return text;
    }


    /*
     * 创建目录
     */
    public boolean createLogDir(String dir) {
        File logFile = new File(dir);
        if (!logFile.exists()) {
            return logFile.mkdirs();
        }
        return true;
    }
}
