package shu.cn;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bean.tuling.enums.ResultType;
import bean.tuling.response.Results;
import bean.tuling.response.TuLingResponseBean;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.log4j.Log4j2;
import net.sf.json.JSONException;
import org.apache.commons.lang.StringUtils;
import org.nlpcn.commons.lang.util.StringUtil;
import shu.cn.weichat.Wechat;
import shu.cn.weichat.api.MessageTools;
import shu.cn.weichat.api.WechatTools;
import shu.cn.weichat.beans.BaseMsg;
import shu.cn.weichat.core.Core;
import shu.cn.weichat.face.IMsgHandlerFace;

import shu.cn.weichat.utils.LogUtil;
import shu.cn.weichat.utils.SleepUtils;
import shu.cn.weichat.utils.XmlUtil;
import shu.cn.weichat.utils.enums.MsgTypeEnum;
import shu.cn.weichat.utils.enums.ReplyMsgTypeEnum;
import shu.cn.weichat.utils.tools.DownloadTools;
import utils.DateUtil;
import utils.HttpUtil;
import utils.TuLingUtil;
import weixin.exception.WXException;
import weixin.utils.WXUntil;

@Log4j2
public class IMsgHandlerFaceImpl implements IMsgHandlerFace {
    private final Properties pps = new Properties();
    private final String msgFileName = "msg.property";
    private int count = 1;
    private boolean autoReply = false;

    private static final Core core = Core.getInstance();
    public String savePath = "D://weixin";

    private final Set<String> groupIdList = new HashSet<>();

    public IMsgHandlerFaceImpl() {


        String qrPath = savePath + File.separator + "login";
        Wechat wechat = new Wechat(IMsgHandlerFaceImpl.this, qrPath);
        wechat.start();

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

    @Override
    public List<MessageTools.Result> textMsgHandle(BaseMsg msg) {
        log.info(LogUtil.printFromMeg(msg, ""));
        List<MessageTools.Result> results = new ArrayList<>();
        String text = msg.getText();
        //存储消息
        storeMsg(msg.getMsgId(), MsgTypeEnum.TEXT.getType() + ":" + text);
        //============炸弹消息===================
        if (text.contains("[Bomb]")) {
            String userName = core.getUserSelf().getString("UserName");
            if (!msg.getFromUserName().equals(userName)
            && (
                    MessageTools.bombMsgMao.get(msg.getFromUserName()) == null ||
                            MessageTools.bombMsgMao.get(msg.getFromUserName())<0)) {

                results.add(MessageTools.Result.builder()
                        .msg("？，炸我")
                        .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                        .build());
                if (msg.getGroupMsg()) {
                    for (int i = 0; i < 10; i++) {
                        results.add(MessageTools.Result.builder()
                                .msg("[Bomb]")
                                .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                                .sleep((long) (Math.random() * (100 - 0) + 100))
                                .type("[Bomb]")
                                .build());
                    }
                    MessageTools.bombMsgMao.put(msg.getFromUserName(),10);
                    return results;
                }
                for (int i = 0; i < 10; i++) {
                    results.add(MessageTools.Result.builder()
                            .msg("[Bomb]")
                            .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                            .sleep((long) (Math.random() * (1000 - 100) + 100))
                            .type("[Bomb]")
                            .build());
                }
                results.add(MessageTools.Result.builder()
                        .msg("你以为完了？还没有！")
                        .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                        .sleep((long) 5000)
                        .build());
                for (int i = 0; i < 500; i++) {
                    results.add(MessageTools.Result.builder()
                            .msg("[Bomb]")
                            .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                            .sleep((long) (Math.random() * (1000 * 10 - 100) + 100))
                            .type("[Bomb]")
                            .build());
                }
                results.add(MessageTools.Result.builder()
                        .msg("慢慢来...")
                        .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                        .sleep((long) (Math.random() * (1000 * 10 - 100) + 100))
                        .build());
                for (int i = 0; i < 500; i++) {
                    results.add(MessageTools.Result.builder()
                            .msg("[Bomb]")
                            .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                            .sleep((long) (Math.random() * (1000 * 10 - 100) + 100))
                            .type("[Bomb]")
                            .build());
                }
                MessageTools.bombMsgMao.put(msg.getFromUserName(),1010);
                return results;
            }
        } else if (text.toUpperCase().equals("C")) {
            autoReply = false;
            log.info("已关闭自动回复。");
            return results;
        } else if (text.toUpperCase().equals("O")) {
            log.info("已开启自动回复。");
            autoReply = true;
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
        //@自己但没消息内容
        if (msg.isMentionMe() && StringUtils.isEmpty(text)) {
            MessageTools.Result.ResultBuilder resultBuilder =
                    MessageTools.Result.builder().msg("什么事？")
                            .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT);
            results.add(resultBuilder.build());
            return results;
        }
        text = isReply(msg);
        if (text.isEmpty()) {
            return results;
        }

        if (text.contains("你是谁")) {
            MessageTools.Result.ResultBuilder resultBuilder =
                    MessageTools.Result.builder().msg("我是女朋友的专属聊天机器人").replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT);
            results.add(resultBuilder.build());
            return results;
        }

        try {
            results = handleTuLingMsg(TuLingUtil.robotMsgTuling(text), msg);
        } catch (JSONException | NullPointerException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       /* int s = (int) (1 + Math.random() * 10 * 6000);//延迟1s到3min
        log.info(("延迟回复：" + (s / 1000) + "S"));
		try {
			Thread.sleep(s);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        return results;
    }

    /*
     * 图片消息(non-Javadoc)
     * @see X.cn.zhouyafeng.itchat4j.face.IMsgHandlerFace#picMsgHandle(com.alibaba.fastjson.JSONObject)
     */
    @Override
    public String picMsgHandle(BaseMsg msg) {
        String path = downloadFile(msg, ".jpg", MsgTypeEnum.PIC); // 调用此方法来保存图片
        log.info(LogUtil.printFromMeg(msg, path));
        if (StringUtil.isNotBlank(path)) {
            storeMsg(msg.getMsgId(), MsgTypeEnum.PIC.getType() + ":" + path);
        }
        if (isReply(msg).isEmpty()) {
            return "";
        }

/*        try {// 识别图片文字
            String string = OCRHelper.recognizeText(picPath, fileName, "chi_sim");
            log.info("图片识别：" + string);
            if (string.isEmpty()) {
                return "这个图片什么含义";
            }
            return  HttpUtil.robotMsgTuling(string);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }*/
        return "";
    }

    /*
     * 语音消息(non-Javadoc)
     * @see X.cn.zhouyafeng.itchat4j.face.IMsgHandlerFace#voiceMsgHandle(com.alibaba.fastjson.JSONObject)
     */
    @Override
    public String voiceMsgHandle(BaseMsg msg) {

        // 调用此方法来保存语音
        String path = downloadFile(msg, ".mp3", MsgTypeEnum.VOICE);
        log.info(LogUtil.printFromMeg(msg, path));
        if (StringUtil.isNotBlank(path)) {
            storeMsg(msg.getMsgId(), MsgTypeEnum.VOICE.getType() + ":" + path);
        }
        if (isReply(msg).isEmpty()) {
            return "";
        }
        String str = "";
        /*try {
            str = voice2Text(fileName, voicePath + fileName);
        } catch (WXException | IOException e1) {
            // TODO Auto-generated catch block
            return "不方便听语音。";
        }
        if (str.isEmpty()) {
            return "不方便听语音.";
        }
        try {
            str = HttpUtil.robotMsgTuling(str);
        } catch (JSONException | NullPointerException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
        return str;
    }

    private String downloadFile(BaseMsg msg, String ext, MsgTypeEnum type) {
        //发消息的用户或群名称
        String username = core.getRemarkNameByUserName(msg.getFromUserName());
/*        //视频存储在中文路径然后用ffpeg压缩卡住
        if (ext.equals(".mp4")){
            username = msg.getFromUserName();
        }*/
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
        String path = savePath + File.separator + type + File.separator + username + File.separator;
/*        //去掉@，FFmpeg会卡住
        path = path.replace("@", "");
        fileName = fileName.replace("@", "");*/
        boolean logDir = createLogDir(path);
        if (logDir) {
            DownloadTools.getDownloadFn(msg, type.getType(), path + fileName);
        } else {
            return null;
        }

        return path + fileName;
    }

    @Override
    public String videoMsgHandle(BaseMsg msg) {
        String path = downloadFile(msg, ".mp4", MsgTypeEnum.VIEDO);
        log.info(LogUtil.printFromMeg(msg, path));
        if (StringUtil.isNotBlank(path)) {
            storeMsg(msg.getMsgId(), MsgTypeEnum.VIEDO.getType() + ":" + path);
        }
        if (isReply(msg).equals("")) {
            return "";
        }
        return "";
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
        JSONObject userSelf = core.getUserSelf();
        //自己的撤回消息不发送
        //TODO BUG，判断不了是不是自己的消息
        if (msg.getMemberName() == null &&
                userSelf.getString("UserName").equals(msg.getFromUserName())) {
            return null;
        }
        String content = msg.getContent();
        content = content.replace("&lt;", "<").replace("&gt;", ">");
        content = "<root>" + content + "</root>";
        Map<String, String> map = XmlUtil.toMap(content);
        Object msgid = map.get("msgid");
        if (msgid == null) {
            return null;
        }
        String value = loadMsg(msgid.toString());
        if (value == null) {
            return null;
        }
        ArrayList<MessageTools.Result> results = new ArrayList<>();
        MessageTools.Result result = null;
        //撤回消息用户的昵称
        String fromNickName = "";
        if (msg.getGroupMsg()) {
            fromNickName = WechatTools.getGroupUserDisplayNameOfGroup(msg.getFromUserName(), msg.getMemberName());
        } else {
            fromNickName = WechatTools.getContactRemarkNameByUserName(msg.getFromUserName());
        }
        //文本消息 回复
        if (value.indexOf(MsgTypeEnum.TEXT.getType() + ":") == 0) {
            result = MessageTools.Result.builder()
                    .msg("【" + fromNickName + "】撤回的文本消息：" + value.substring((MsgTypeEnum.TEXT.getType() + ":").length()))
                    .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                    .build();
            results.add(result);
            //图片消息 回复
        } else if (value.indexOf(MsgTypeEnum.PIC.getType() + ":") == 0) {
            result = MessageTools.Result.builder()
                    .msg("【" + fromNickName + "】撤回的图片消息：")
                    .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                    .build();
            results.add(result);
            result = MessageTools.Result.builder()
                    .msg(value.substring((MsgTypeEnum.PIC.getType() + ":").length()))
                    .replyMsgTypeEnum(ReplyMsgTypeEnum.PIC)
                    .build();
            results.add(result);
        } else if (value.indexOf(MsgTypeEnum.VIEDO.getType() + ":") == 0) {
            result = MessageTools.Result.builder()
                    .msg("【" + fromNickName + "】撤回的视频消息：")
                    .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                    .build();
            results.add(result);
            result = MessageTools.Result.builder()
                    .msg(value.substring((MsgTypeEnum.VIEDO.getType() + ":").length()))
                    .replyMsgTypeEnum(ReplyMsgTypeEnum.VIDEO)
                    .build();
            results.add(result);
        } else if (value.indexOf(MsgTypeEnum.VOICE.getType() + ":") == 0) {
            result = MessageTools.Result.builder()
                    .msg("【" + fromNickName + "】撤回的语音消息：")
                    .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                    .build();
            results.add(result);
            result = MessageTools.Result.builder()
                    .msg(value.substring((MsgTypeEnum.VOICE.getType() + ":").length()))
                    .replyMsgTypeEnum(ReplyMsgTypeEnum.VOICE)
                    .build();
            results.add(result);
        }
        return results;
    }

    @Override
    public String addFriendMsgHandle(BaseMsg msg) {
        log.info(LogUtil.printFromMeg(msg));
        String text = isReply(msg);
        if (text.equals("")) {
            return "";
        }
        return "好友确认消息：" + msg.getContent();
    }

    @Override
    public String systemMsgHandle(BaseMsg msg) {
        log.info(LogUtil.printFromMeg(msg));
        String text = isReply(msg);
        if (text.equals("")) {
            return "";
        }
        if (text.startsWith("你已添加了")) {
            return "hello";
        }
        return "";
    }

    @Override
    public String emotionMsgHandle(BaseMsg msg) {
        log.info(LogUtil.printFromMeg(msg));
        return null;
    }

    @Override
    public List<MessageTools.Result> appMsgHandle(BaseMsg msg) {
        log.info(LogUtil.printFromMeg(msg));
        return null;
    }

    @Override
    public String verifyAddFriendMsgHandle(BaseMsg msg) {
        log.info(LogUtil.printFromMeg(msg));
        return null;
    }

    @Override
    public String mediaMsgHandle(BaseMsg msg) {
        log.info(LogUtil.printFromMeg(msg));
        return null;
    }

    @Override
    public String nameCardMsgHandle(BaseMsg msg) {
        log.info(LogUtil.printFromMeg(msg));
        if (isReply(msg).equals("")) {
            return "";
        }
        return "";
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

    private String isReply(BaseMsg m) {
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
            //#######我自己在群里发的消息  控制命令#######
            if (fromUserName.equals(userSelf.getString("UserName"))) {
                if ("OG".equals(content.toUpperCase())) {//开启群回复
                    groupIdList.add((toUserName));
                } else if ("CG".equals(content.toUpperCase())) {//关闭群回复
                    groupIdList.remove(toUserName);
                }
                isReply = false;
                //其他人发的群消息是否需要回复
            } else if (groupIdList.contains(fromUserName)) {
                isReply = true;
            } else {
                isReply = false;
            }

        } else {
            //非群消息
            // #####################自己的消息 不回复,除非是关闭消息###########
            String userName = userSelf.getString("UserName");
            if (fromUserName.equals(userName)) {
                log.info(msgId + "-自己的消息");
                if (content.toUpperCase().equals("CS") || content.toUpperCase().equals("OS")) {
                    return "";
                }
                isReply = false;
            }
        }



/*        // #############将群名称加入 群列表
        if (groupMsg) {
            core.getGroupIdList().add((fromUserName));
            // 群名称需要在群里有人发信息后才能获取
            replyGroupNameList = core.getGroupUsersName(replyGroupname);
            userNameByGroupname = WechatTools.getUsernameAndGroupName();
        } else if (toUserName.contains("@@") && !core.getGroupIdList().contains(toUserName)) {
            core.getGroupIdList().add((toUserName));
            // 群名称需要在群里有人发信息后才能获取
            replyGroupNameList = core.getGroupUsersName(replyGroupname);
            userNameByGroupname = WechatTools.getUsernameAndGroupName();
        }*/
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
