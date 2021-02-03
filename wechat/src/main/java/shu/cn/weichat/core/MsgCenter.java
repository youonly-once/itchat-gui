package shu.cn.weichat.core;

import java.util.List;
import java.util.regex.Matcher;

import com.alibaba.fastjson.JSON;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import shu.cn.weichat.api.MessageTools;
import shu.cn.weichat.api.WechatTools;
import shu.cn.weichat.beans.BaseMsg;
import shu.cn.weichat.face.IMsgHandlerFace;

import shu.cn.weichat.utils.LogUtil;
import shu.cn.weichat.utils.MsgCodeEnum;
import shu.cn.weichat.utils.enums.MsgTypeEnum;
import shu.cn.weichat.utils.tools.CommonTools;

/**
 * 消息处理中心
 *
 * @author ShuXinSheng
 * @version 1.1
 * @date 创建时间：2017年5月14日 下午12:47:50
 */
@Log4j2
public class MsgCenter {

    private static Core core = Core.getInstance();


    /**
     * 接收消息，放入队列
     *
     * @param msgList
     * @return
     * @author ShuXinSheng
     * @date 2017年4月23日 下午2:30:48
     */
    public static void produceMsg(JSONArray msgList) {

        for (int i = 0; i < msgList.size(); i++) {

            JSONObject m = msgList.getJSONObject(i);
            m.put("groupMsg", false);// 是否是群消息
            String fromUserName = m.getString("FromUserName");
            String toUserName = m.getString("ToUserName");
            String content = m.getString("Content");

            if (fromUserName.contains("@@") || toUserName.contains("@@")) { // 群聊消息
                // 群消息与普通消息不同的是在其消息体（Content）中会包含发送者id及":<br/>"消息，这里需要处理一下，去掉多余信息，只保留消息内容
                int index = content.indexOf(":<br/>");
                if (index != -1) {
                    m.put("Content", content.substring(index + ":<br/>".length()));
                    //发送消息的人
                    m.put("MemberName", content.substring(0, index));
                }
                m.put("groupMsg", true);
            } else {
                CommonTools.msgFormatter(m, "Content");
            }
            if (m.getInteger("MsgType") == MsgCodeEnum.MSGTYPE_TEXT.getCode()) { // words
                JSONObject msg = new JSONObject(); // 文本消息
                if (m.getString("Url").length() != 0) {
                    String regEx = "(.+?\\(.+?\\))";
                    Matcher matcher = CommonTools.getMatcher(regEx, m.getString("Content"));
                    String data = "Map";
                    if (matcher.find()) {
                        data = matcher.group(1);
                    }
                    msg.put("Type", "Map");
                    msg.put("Text", data);
                } else {
                    msg.put("Type", MsgTypeEnum.TEXT.getType());
                    msg.put("Text", m.getString("Content"));
                }
                m.put("Type", msg.getString("Type"));
                m.put("Text", msg.getString("Text"));
                //log.info(m.getString("NewMsgId") + "-文本消息:" + m);
            } else if (m.getInteger("MsgType") == MsgCodeEnum.MSGTYPE_IMAGE.getCode()
            ) { // 图片消息
                m.put("Type", MsgTypeEnum.PIC.getType());
                //log.info(m.getString("NewMsgId") + "-图片消息:" + m);
            } else if (m.getInteger("MsgType") == MsgCodeEnum.MSGTYPE_VOICE.getCode()) { // 语音消息
                m.put("Type", MsgTypeEnum.VOICE.getType());
                //log.info(m.getString("NewMsgId") + "-语音消息:" + m);
            } else if (m.getInteger("MsgType") == MsgCodeEnum.MSGTYPE_VERIFYMSG.getCode()) {// friends
                //log.info(m.getString("NewMsgId") + "-好友确认消息:" + m); // 好友确认消息
                m.put("Type", MsgTypeEnum.ADDFRIEND.getType());
            } else if (m.getInteger("MsgType") == MsgCodeEnum.MSGTYPE_SHARECARD.getCode()) { // 共享名片
                m.put("Type", MsgTypeEnum.NAMECARD.getType());
                //log.info(m.getString("NewMsgId") + "-名片分享消息:" + m);
            } else if (m.getInteger("MsgType") == MsgCodeEnum.MSGTYPE_VIDEO.getCode()
                    || m.getInteger("MsgType") == MsgCodeEnum.MSGTYPE_MICROVIDEO.getCode()) {// viedo
                //log.info(m.getString("NewMsgId") + "-视频消息:" + m);
                m.put("Type", MsgTypeEnum.VIDEO.getType());
            } else if (m.getInteger("MsgType") == MsgCodeEnum.MSGTYPE_APP.getCode()) { // sharing
                //log.info(m.getString("NewMsgId") + "-分享链接消息:" + m); // 分享链接
                m.put("Type", MsgTypeEnum.APP.getType());
            } else if (m.getInteger("MsgType") == MsgCodeEnum.MSGTYPE_STATUSNOTIFY.getCode()) {// phone
                //log.info(m.getString("NewMsgId") + "-微信初始化消息:" + m); // init
                // 微信初始化消息

            } else if (m.getInteger("MsgType") == MsgCodeEnum.MSGTYPE_SYS.getCode()) {
                //log.info(m.getString("NewMsgId") + "-系统消息:" + m);
                m.put("Type", MsgTypeEnum.SYSTEM.getType());
            } else if (m.getInteger("MsgType") == MsgCodeEnum.MSGTYPE_RECALLED.getCode()) {
                //log.info(m.getString("NewMsgId") + "-撤回消息:" + m);
                m.put("Type", MsgTypeEnum.UNDO.getType());
            } else if (m.getInteger("MsgType") == MsgCodeEnum.MSGTYPE_EMOTICON.getCode()) {
                //log.info(m.getString("NewMsgId") + "-表情消息:" + m);
                m.put("Type", MsgTypeEnum.EMOTION.getType());
            } else {
                log.warn(m.getString("NewMsgId") + "-未知消息:" + m);
            }
            try {
                //添加元素 会阻塞
                core.getMsgList().put(JSON.toJavaObject(m,
                        BaseMsg.class));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 消息处理
     *
     * @param msgHandler
     * @author ShuXinSheng
     * @date 2017年5月14日 上午10:52:34
     */
    public static void handleMsg(IMsgHandlerFace msgHandler) {
                while (true) {
                    BaseMsg msg = null;
                    try {
                        //拿元素 会阻塞
                        msg = core.getMsgList().take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (msg == null) {
                        continue;
                    }
                    if (StringUtils.isEmpty(msg.getContent())) {
                        continue;
                    }
                    //群消息content格式化
                    groupMsgFormater(msg);

                    if (msg.getType() != null) {
                        if (msg.getType().equals(MsgTypeEnum.TEXT.getType())) {
                            List<MessageTools.Result> result = msgHandler.textMsgHandle(msg);
                            MessageTools.sendMsgById(result, msg.getFromUserName());
                        } else if (msg.getType().equals(MsgTypeEnum.PIC.getType())) {
                            String result = msgHandler.picMsgHandle(msg);
                            MessageTools.sendMsgById(result, msg.getFromUserName());
                        } else if (msg.getType().equals(MsgTypeEnum.VOICE.getType())) {
                            String result = msgHandler.voiceMsgHandle(msg);
                            MessageTools.sendMsgById(result, msg.getFromUserName());
                        } else if (msg.getType().equals(MsgTypeEnum.VIDEO.getType())) {
                            String result = msgHandler.videoMsgHandle(msg);
                            MessageTools.sendMsgById(result, msg.getFromUserName());
                        } else if (msg.getType().equals(MsgTypeEnum.NAMECARD.getType())) {
                            String result = msgHandler.nameCardMsgHandle(msg);
                            MessageTools.sendMsgById(result, msg.getFromUserName());
                        } else if (msg.getType().equals(MsgTypeEnum.UNDO.getType())) {
                            List<MessageTools.Result> results = msgHandler.undoMsgHandle(msg);
                            MessageTools.sendMsgById(results, msg.getFromUserName());
                        } else if (msg.getType().equals(MsgTypeEnum.ADDFRIEND.getType())) {
                            String result = msgHandler.addFriendMsgHandle(msg);
                            MessageTools.sendMsgById(result, msg.getFromUserName());
                        } else if (msg.getType().equals(MsgTypeEnum.SYSTEM.getType())) {
                            String result = msgHandler.systemMsgHandle(msg);
                            MessageTools.sendMsgById(result, msg.getToUserName());
                        } else if (msg.getType().equals(MsgTypeEnum.EMOTION.getType())) {
                            String result = msgHandler.emotionMsgHandle(msg);
                            MessageTools.sendMsgById(result, msg.getFromUserName());
                        } else if (msg.getType().equals(MsgTypeEnum.APP.getType())) {
                            List<MessageTools.Result> results = msgHandler.appMsgHandle(msg);
                            MessageTools.sendMsgById(results, msg.getFromUserName());
                        } else {
                            log.info(LogUtil.printFromMeg(msg));
                            log.warn("未知消息：{}", msg);
                        }
                    }

/*            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
                }


    }
    private static void groupMsgFormater(BaseMsg msg){
        //群消息
        if (msg.getGroupMsg()){
            //获取自己在群里的备注
            String groupMyUserNickNameOfGroup = WechatTools.getGroupUserDisplayNameOfGroup(msg.getFromUserName(), core.getUserName());
            if (groupMyUserNickNameOfGroup != null
            && msg.getContent().contains("@"+groupMyUserNickNameOfGroup+" ")){
                //@自己
                //获取他的群备注
                String groupOtherUserNickNameOfGroup = WechatTools.getGroupUserDisplayNameOfGroup(msg.getFromUserName(), msg.getMemberName());
                if (groupOtherUserNickNameOfGroup != null){
                    msg.setMentionMeUserNickName(groupOtherUserNickNameOfGroup);
                    String replace = msg.getContent().replace("@" + groupMyUserNickNameOfGroup, "");
                    msg.setContent(replace);
                    msg.setText(replace);
                    msg.setMentionMe(true);
                }
            }
        }
    }
}
