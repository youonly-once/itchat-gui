package cn.shu.wechat.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import cn.shu.wechat.beans.AttrHistory;
import cn.shu.wechat.beans.sync.AddMsgList;
import cn.shu.wechat.beans.sync.DelContactList;
import cn.shu.wechat.beans.sync.ModContactList;
import cn.shu.wechat.face.IMsgHandlerFace;
import cn.shu.wechat.utils.JSONObjectUtil;
import cn.shu.wechat.utils.enums.MsgCodeEnum;
import cn.shu.wechat.utils.enums.ReplyMsgTypeEnum;
import cn.shu.wechat.utils.tools.CommonTools;
import cn.shu.wechat.utils.tools.DownloadTools;
import com.alibaba.fastjson.JSON;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;

import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.api.WechatTools;


import cn.shu.wechat.utils.LogUtil;
import cn.shu.wechat.utils.enums.MsgTypeEnum;
import org.nlpcn.commons.lang.util.StringUtil;

/**
 * 消息处理中心
 *
 * @author ShuXinSheng
 * @version 1.1
 * @date 创建时间：2017年5月14日 下午12:47:50
 */
@Log4j2
public class MsgCenter {



    /*   private static AttrHistoryMapper attrHistoryMapper;

    @Resource
    public static void setAttrHistoryMapper(AttrHistoryMapper attrHistoryMapper) {
        MsgCenter.attrHistoryMapper = attrHistoryMapper;
    }*/

    /**
     * 接收消息，放入队列
     *
     * @param msgLists
     * @return
     * @author ShuXinSheng
     * @date 2017年4月23日 下午2:30:48
     */
    public static void produceNewMsg(List<AddMsgList> msgLists) {

        for (AddMsgList m : msgLists) {
            m.setGroupMsg( false);// 是否是群消息
            String fromUserName = m.getFromUserName();
            String toUserName = m.getToUserName();
            String content = m.getContent();
            if (fromUserName.contains("@@") || toUserName.contains("@@")) { // 群聊消息
                // 群消息与普通消息不同的是在其消息体（Content）中会包含发送者id及":<br/>"消息，这里需要处理一下，去掉多余信息，只保留消息内容
                int index = content.indexOf(":<br/>");
                if (index != -1) {
                    m.setContent( content.substring(index + ":<br/>".length()));
                    //发送消息的人
                    m.setMemberName(content.substring(0, index));
                }
                m.setGroupMsg( Boolean.TRUE);
            } else {
                CommonTools.msgFormatter(JSON.parseObject(JSON.toJSONString(m)), "content");
            }
            MsgCodeEnum msgType = MsgCodeEnum.getByCode(m.getMsgType());
            switch (msgType) {

                case MSGTYPE_TEXT:
                    if (m.getUrl().length() != 0) {
                        String regEx = "(.+?\\(.+?\\))";
                        Matcher matcher = CommonTools.getMatcher(regEx, m.getContent());
                        String data = "Map";
                        if (matcher.find()) {
                            data = matcher.group(1);
                        }
                        m.setType(  MsgTypeEnum.MAP);
                        m.setText( data);
                    } else {
                        m.setType( MsgTypeEnum.TEXT);
                        m.setText(m.getContent());
                    }
                    //log.info(m.getString("NewMsgId") + "-文本消息:" + m);
                    break;
                case MSGTYPE_IMAGE:
                    m.setType( MsgTypeEnum.PIC);
                    //log.info(m.getString("NewMsgId") + "-图片消息:" + m);
                    break;
                case MSGTYPE_VOICE:
                    m.setType( MsgTypeEnum.VOICE);
                    //log.info(m.getString("NewMsgId") + "-语音消息:" + m);
                    break;
                case MSGTYPE_VIDEO:
                case MSGTYPE_MICROVIDEO:
                    //log.info(m.getString("NewMsgId") + "-视频消息:" + m);
                    m.setType(MsgTypeEnum.VIDEO);
                    break;
                case MSGTYPE_EMOTICON:
                    //log.info(m.getString("NewMsgId") + "-表情消息:" + m);
                    m.setType(MsgTypeEnum.EMOTION);
                    break;
                case MSGTYPE_APP:
                    //log.info(m.getString("NewMsgId") + "-分享链接消息:" + m); // 分享链接
                    m.setType( MsgTypeEnum.APP);
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
                    m.setType(MsgTypeEnum.SYSTEM);
                    break;
                case MSGTYPE_SYSNOTICE:
                    break;
                case MSGTYPE_POSSIBLEFRIEND_MSG:
                    break;
                case MSGTYPE_VERIFYMSG:
                    //log.info(m.getString("NewMsgId") + "-好友确认消息:" + m); // 好友确认消息
                    m.setType( MsgTypeEnum.ADDFRIEND);
                    break;
                case MSGTYPE_SHARECARD:
                    m.setType( MsgTypeEnum.NAMECARD);
                    //log.info(m.getString("NewMsgId") + "-名片分享消息:" + m);
                    break;
                case MSGTYPE_SYS:
                    //log.info(m.getString("NewMsgId") + "-系统消息:" + m);
                    m.setType( MsgTypeEnum.SYSTEM);
                    break;
                case MSGTYPE_RECALLED:
                    //log.info(m.getString("NewMsgId") + "-撤回消息:" + m);
                    m.setType( MsgTypeEnum.UNDO);
                    break;
                case UNKNOWN:
                default:
                    log.warn(m.getNewMsgId() + "-未知消息:" + m);
                    break;
            }
            try {
                //添加元素 会阻塞

                Core.getMsgList().put(m);
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
            AddMsgList msg = null;
            try {
                //拿元素 会阻塞
                msg = Core.getMsgList().take();
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
            switch (msg.getType()) {
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

    /**
     * 联系人修改消息
     * @param msgLists
     */
    public static void produceModContactMsg(List<ModContactList> msgLists) {
        for (ModContactList msg : msgLists) {
            JSONObject newV = JSON.parseObject(JSON.toJSONString(msg));
            JSONObject oldV = null;
            if (msg.getUserName().startsWith("@@")){
                oldV = Core.getGroupMap().get(msg.getUserName());
            }else{
                oldV = Core.getContactMap().get(msg.getUserName());
            }
            oldV= JSON.parseObject(JSON.toJSONString(JSON.toJavaObject(oldV,ModContactList.class)));
            if (oldV == null){
                return;
            }
            ArrayList<MessageTools.Result> results = new ArrayList<>();
            String name =oldV.getString("remarkName");
            if (StringUtil.isBlank(name)) {
                name = oldV.getString("nickName");
            }
            //存在key
            Map<String, Map<String, String>> differenceMap = JSONObjectUtil.getDifferenceMap(oldV, newV);
            if (differenceMap.size()>0){
                //Old与New存在差异
                String tip ="联系人";
                log.info("{}（{}）属性更新：{}",tip,name,differenceMap);
                //发送消息
                results.add(MessageTools.Result.builder().msg(tip+"（"+name+"）属性更新："+mapToString(differenceMap))
                        .replyMsgTypeEnum(ReplyMsgTypeEnum.TEXT)
                        .build());
                Core.getContactMap().put(msg.getUserName(),newV);
                //存储数据库
                store(differenceMap,oldV,results);

                MessageTools.sendMsgByUserId(results, msg.getUserName());
            }
        }



    }
    /**
     * 保存修改记录到数据库
     * @param differenceMap
     * @param oldV
     */
    private static void store(Map<String, Map<String, String>> differenceMap,JSONObject oldV,ArrayList<MessageTools.Result> results){
        ArrayList<AttrHistory> attrHistories = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> stringMapEntry : differenceMap.entrySet()) {
            for (Map.Entry<String, String> stringStringEntry : stringMapEntry.getValue().entrySet()) {
                if (stringMapEntry.getKey().equals("headImgUrl")
                        || stringMapEntry.getKey().equals("头像更换")){
/*					String oldHeadPath = DownloadTools.downloadHeadImg(stringStringEntry.getKey()
							, IMsgHandlerFaceImpl.savePath+File.separator+oldV.getString("UserName")+File.separator);*/
                    String oldHeadPath = Core.getContactHeadImgPath().get(oldV.getString("userName"));
                    String newHeadPath = DownloadTools.downloadHeadImg(stringStringEntry.getValue()
                            , oldV.getString("userName"));
                    Core.getContactHeadImgPath().put(oldV.getString("userName"),newHeadPath);
                    //更换前
                    results.add(MessageTools.Result.builder()
                            .replyMsgTypeEnum(ReplyMsgTypeEnum.PIC)
                            //.toUserName("filehelper")
                            .msg(oldHeadPath).build());
                    //更换后
                    results.add(MessageTools.Result.builder()
                            .replyMsgTypeEnum(ReplyMsgTypeEnum.PIC)
                            //.toUserName("filehelper")
                            .msg(newHeadPath).build());
                    AttrHistory build = AttrHistory.builder()
                            .attr(stringMapEntry.getKey())
                            .oldval(oldHeadPath)
                            .newval(newHeadPath)
                            .id(0)
                            .nickname(oldV.getString("nickName"))
                            .remarkname(oldV.getString("remarkName"))
                            .username(oldV.getString("userName"))
                            .createtime(new Date())
                            .build();
                    attrHistories.add(build);
                }else{
                    AttrHistory build = AttrHistory.builder()
                            .attr(stringMapEntry.getKey())
                            .oldval(stringStringEntry.getKey())
                            .newval(stringStringEntry.getValue())
                            .id(0)
                            .nickname(oldV.getString("nickName"))
                            .remarkname(oldV.getString("remarkName"))
                            .username(oldV.getString("userName"))
                            .createtime(new Date())
                            .build();
                    attrHistories.add(build);
                }

            }
        }
        //attrHistoryMapper.batchInsert(attrHistories);
    }
    /**
     * map转string
     * @param differenceMap
     * @return
     */
    private static String mapToString(Map<String, Map<String, String>> differenceMap){
        String str = "";
        for (Map.Entry<String, Map<String, String>> stringMapEntry : differenceMap.entrySet()) {
            Map<String, String> value = stringMapEntry.getValue();
            for (Map.Entry<String, String> stringStringEntry : value.entrySet()) {
                str = str+"\n【"+stringMapEntry.getKey()+"】(\""+stringStringEntry.getKey()+"\" -> \""+stringStringEntry.getValue()+"\")";
            }
        }
        return str;
    }

    /**
     * 联系人删除消息
     * @param msgLists
     */
    public static void produceDelContactMsg(List<DelContactList> msgLists) {

    }

    /**
     * 聊天室修改消息
     * @param msgLists
     */
    public static void produceModChatRoomMemberMsg(List<ModContactList> msgLists) {

    }

    private static void groupMsgFormater(AddMsgList msg) {
        //群消息
        if (msg.getGroupMsg()) {
            //获取自己在群里的备注
            String groupMyUserNickNameOfGroup = WechatTools.getMemberDisplayNameOfGroup(msg.getFromUserName(), Core.getUserName());
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
