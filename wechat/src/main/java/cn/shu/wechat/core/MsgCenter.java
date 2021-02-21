package cn.shu.wechat.core;

import java.util.List;
import java.util.regex.Matcher;

import cn.shu.wechat.face.IMsgHandlerFace;
import cn.shu.wechat.utils.MsgCodeEnum;
import cn.shu.wechat.utils.tools.CommonTools;
import com.alibaba.fastjson.JSON;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.api.WechatTools;
import cn.shu.wechat.beans.BaseMsg;

import cn.shu.wechat.utils.LogUtil;
import cn.shu.wechat.utils.enums.MsgTypeEnum;

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

        for (Object o : msgList) {
            JSONObject m = (JSONObject) o;
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
            MsgCodeEnum msgType = MsgCodeEnum.getByCode(m.getInteger("MsgType"));
            switch (msgType) {

                case MSGTYPE_TEXT:
                    JSONObject msg = new JSONObject();
                    if (m.getString("Url").length() != 0) {
                        String regEx = "(.+?\\(.+?\\))";
                        Matcher matcher = CommonTools.getMatcher(regEx, m.getString("Content"));
                        String data = "Map";
                        if (matcher.find()) {
                            data = matcher.group(1);
                        }
                        msg.put("Type", MsgTypeEnum.MAP.getType());
                        msg.put("Text", data);
                    } else {
                        msg.put("Type", MsgTypeEnum.TEXT.getType());
                        msg.put("Text", m.getString("Content"));
                    }
                    m.put("Type", msg.getString("Type"));
                    m.put("Text", msg.getString("Text"));
                    //log.info(m.getString("NewMsgId") + "-文本消息:" + m);
                    break;
                case MSGTYPE_IMAGE:
                    m.put("Type", MsgTypeEnum.PIC.getType());
                    //log.info(m.getString("NewMsgId") + "-图片消息:" + m);
                    break;
                case MSGTYPE_VOICE:
                    m.put("Type", MsgTypeEnum.VOICE.getType());
                    //log.info(m.getString("NewMsgId") + "-语音消息:" + m);
                    break;
                case MSGTYPE_VIDEO:
                case MSGTYPE_MICROVIDEO:
                    //log.info(m.getString("NewMsgId") + "-视频消息:" + m);
                    m.put("Type", MsgTypeEnum.VIDEO.getType());
                    break;
                case MSGTYPE_EMOTICON:
                    //log.info(m.getString("NewMsgId") + "-表情消息:" + m);
                    m.put("Type", MsgTypeEnum.EMOTION.getType());
                    break;
                case MSGTYPE_APP:
                    //log.info(m.getString("NewMsgId") + "-分享链接消息:" + m); // 分享链接
                    m.put("Type", MsgTypeEnum.APP.getType());
                    break;
                case MSGTYPE_VOIPMSG:
                    break;
                case MSGTYPE_VOIPNOTIFY:
                    break;
                case MSGTYPE_VOIPINVITE:
                    break;
                case MSGTYPE_LOCATION:
                    break;
                case MSGTYPE_STATUSNOTIFY:
                    //log.info(m.getString("NewMsgId") + "-微信初始化消息:" + m); // init
                    // 微信初始化消息
                    m.put("Type", MsgTypeEnum.SYSTEM.getType());
                    break;
                case MSGTYPE_SYSNOTICE:
                    break;
                case MSGTYPE_POSSIBLEFRIEND_MSG:
                    break;
                case MSGTYPE_VERIFYMSG:
                    //log.info(m.getString("NewMsgId") + "-好友确认消息:" + m); // 好友确认消息
                    m.put("Type", MsgTypeEnum.ADDFRIEND.getType());
                    break;
                case MSGTYPE_SHARECARD:
                    m.put("Type", MsgTypeEnum.NAMECARD.getType());
                    //log.info(m.getString("NewMsgId") + "-名片分享消息:" + m);
                    break;
                case MSGTYPE_SYS:
                    //log.info(m.getString("NewMsgId") + "-系统消息:" + m);
                    m.put("Type", MsgTypeEnum.SYSTEM.getType());
                    break;
                case MSGTYPE_RECALLED:
                    //log.info(m.getString("NewMsgId") + "-撤回消息:" + m);
                    m.put("Type", MsgTypeEnum.UNDO.getType());
                    break;
                case UNKNOWN:
                default:
                    log.warn(m.getString("NewMsgId") + "-未知消息:" + m);
                    break;
            }
            try {
                //添加元素 会阻塞
                BaseMsg baseMsg = JSON.toJavaObject(m,
                        BaseMsg.class);
                core.getMsgList().put(baseMsg);
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
              //  continue;
            }
            //群消息content格式化
            groupMsgFormater(msg);
            //需要发送的消息
            List<MessageTools.Result> results = null;
            switch (MsgTypeEnum.getByCode(msg.getType())) {
                case TEXT:
                    results = msgHandler.textMsgHandle(msg);
                    break;
                case PIC:
                    results = msgHandler.picMsgHandle(msg);
                    break;
                case VOICE:
                    results = msgHandler.voiceMsgHandle(msg);
                    break;
                case VIDEO:
                    results = msgHandler.videoMsgHandle(msg);
                    break;
                case NAMECARD:
                    results = msgHandler.nameCardMsgHandle(msg);
                    break;
                case UNDO:
                    results = msgHandler.undoMsgHandle(msg);
                    break;
                case ADDFRIEND:
                    results = msgHandler.addFriendMsgHandle(msg);
                    break;
                case EMOTION:
                    results = msgHandler.emotionMsgHandle(msg);
                    break;
                case APP:
                    results = msgHandler.appMsgHandle(msg);
                    break;
                case MEDIA:
                    break;
                case MAP:
                    results = msgHandler.mapMsgHandle(msg);
                    break;
                case SYSTEM:
                    results = msgHandler.systemMsgHandle(msg);
                    break;
                case UNKNOWN:
                    log.warn(LogUtil.printFromMeg(msg,MsgTypeEnum.UNKNOWN.getCode()));
                    break;
                default:
                    log.warn(LogUtil.printFromMeg(msg,MsgTypeEnum.UNKNOWN.getCode()));
                    break;
            }
            //发送消息
            MessageTools.sendMsgByUserId(results, msg.getFromUserName());
        }


    }

    private static void groupMsgFormater(BaseMsg msg) {
        //群消息
        if (msg.getGroupMsg()) {
            //获取自己在群里的备注
            String groupMyUserNickNameOfGroup = WechatTools.getMemberDisplayNameOfGroup(msg.getFromUserName(), core.getUserName());
            if (groupMyUserNickNameOfGroup != null
                    && msg.getContent().contains("@" + groupMyUserNickNameOfGroup + " ")) {
                //@自己
                //获取他的群备注
                String groupOtherUserNickNameOfGroup = WechatTools.getMemberDisplayNameOfGroup(msg.getFromUserName(), msg.getMemberName());
                if (groupOtherUserNickNameOfGroup != null) {
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
