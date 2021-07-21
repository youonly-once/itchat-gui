package cn.shu.wechat.core;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.api.DownloadTools;
import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.beans.msg.sync.AddMsgList;
import cn.shu.wechat.beans.msg.sync.DelContactList;
import cn.shu.wechat.beans.msg.sync.ModContactList;
import cn.shu.wechat.beans.pojo.AttrHistory;
import cn.shu.wechat.beans.pojo.Contacts;
import cn.shu.wechat.beans.pojo.Message;
import cn.shu.wechat.enums.WXReceiveMsgCodeEnum;
import cn.shu.wechat.enums.WXReceiveMsgCodeOfAppEnum;
import cn.shu.wechat.enums.WXSendMsgCodeEnum;
import cn.shu.wechat.face.IMsgHandlerFace;
import cn.shu.wechat.mapper.MessageMapper;
import cn.shu.wechat.service.ILoginService;
import cn.shu.wechat.service.impl.LoginServiceImpl;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.panels.RoomChatPanelCard;
import cn.shu.wechat.swing.panels.RoomChatPanel;
import cn.shu.wechat.swing.panels.RoomsPanel;
import cn.shu.wechat.utils.CommonTools;
import cn.shu.wechat.utils.JSONObjectUtil;
import cn.shu.wechat.utils.LogUtil;
import cn.shu.wechat.utils.XmlStreamUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.nlpcn.commons.lang.util.StringUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.swing.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static cn.shu.wechat.enums.WXReceiveMsgCodeEnum.MSGTYPE_TEXT;

/**
 * 消息处理中心
 *
 * @author ShuXinSheng
 * @version 1.1
 * @date 创建时间：2017年5月14日 下午12:47:50
 */
@Log4j2
@Component
public class MsgCenter {
    /**
     * 消息处理类
     */
    @Resource
    private IMsgHandlerFace msgHandler;

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private ILoginService loginService;

    /**
     * 接收消息，放入队列
     *
     * @param msg 新消息
     * @author ShuXinSheng
     * @date 2017年4月23日 下午2:30:48
     */
    public void handleNewMsg(AddMsgList msg) {
        //消息类型封装
        WXReceiveMsgCodeEnum msgType = WXReceiveMsgCodeEnum.getByCode(msg.getMsgType());
        String s = LogUtil.printFromMeg(msg, msgType.getDesc());

        //用户在其他平台消息已读的通知
        if (msg.getMsgType() == WXReceiveMsgCodeEnum.MSGTYPE_STATUSNOTIFY.getCode()){
            log.debug(s);
            //更新聊天列表未读数量

            RoomsPanel.getContext().updateUnreadCount(msg.getToUserName(),0);
            return;
        }else{
            log.info(s);
        }

        /**
         * 文本消息：content为文本内容
         * 图片视频文件消息：content为资源ID，@开头，发送消息时指定Content字段可直接发送，不需mediaid
         *  如果消息是自己发的，content为xml
         */
        //消息格式化
        CommonTools.msgFormatter(msg);
        //地图消息，特殊粗粒

        if (msgType == MSGTYPE_TEXT && msg.getUrl().length() != 0) {
            //地图消息
            msg.setMsgType(WXReceiveMsgCodeEnum.MSGTYPE_MAP.getCode());
            msgType = WXReceiveMsgCodeEnum.MSGTYPE_MAP;
        }

        //下载资源文件
        download(msgType,msg);
        //存储数据库
        Message message = storeMsgToDB(msg,getPlainText(msgType,msg));
        //聊天界面
       updateUI(message, msg);
        //处理自定义逻辑
        processMsg(msg,msgType);


    }

    /**
     * 下载文件
     * @param msgType
     * @param msg
     */
    private void download(WXReceiveMsgCodeEnum msgType,AddMsgList msg){
        //下载资源文件
        String path = DownloadTools.getDownloadFilePath(msg,false);
        String pathSlave = DownloadTools.getDownloadFilePath(msg,true);
        //false表示当前文件未下载完成，此时其它地方不能使用
        Hashtable<String, Boolean> fileDownloadStatus = DownloadTools.FILE_DOWNLOAD_STATUS;
        if (path != null) {
            //缩略图
            switch (msgType) {
                case MSGTYPE_IMAGE:
                case MSGTYPE_VIDEO:
                    fileDownloadStatus.put(pathSlave, false);
                    DownloadTools.downloadFile(msg, pathSlave,true);
                    msg.setSlavePath(pathSlave);
                    break;
                default:break;
            }


            fileDownloadStatus.put(path, false);
            DownloadTools.downloadFile(msg, path,false);
            msg.setFilePath(path);

        }
    }
    /**
     * 更新UI
     * @param message
     * @param msg
     */
    private void updateUI(Message message,AddMsgList msg){
        //################3聊天面板消息处理###########3333
        SwingUtilities.invokeLater(() -> {
            int msgUnReadCount = 1;
            String lastMsgPrefix  = "";
            //新增消息列表
            String userName = msg.getFromUserName();
            if (userName.equals(Core.getUserName())) {
                //自己的消息，默认已读
                msgUnReadCount = 0;
                userName = msg.getToUserName();
            }else if(userName.startsWith("@@")){
                //自己在群里发的消息
                if ( Core.getUserName().equals(msg.getMemberName())){
                    lastMsgPrefix =Core.getNickName()+": ";
                    msgUnReadCount = 0;
                }else{
                    MainFrame.getContext().playMessageSound();
                    MainFrame.getContext().setTrayFlashing();
                    lastMsgPrefix =ContactsTools.getMemberDisplayNameOfGroup(userName,msg.getMemberName())+": ";
                }
            }else{
                MainFrame.getContext().playMessageSound();
                MainFrame.getContext().setTrayFlashing();
            }

            if (!Core.getMemberMap().containsKey(userName)){
                //未保存到通讯录的联系人 手动添加
                loginService.WebWxBatchGetContact(userName);
            }
            Contacts contacts = Core.getMemberMap().get(userName);
            //刷新消息
            if (message!=null){
                message.setProcess(100);
                message.setIsSend(true);
                //新消息来了后创建房间
                //RightPanel rightPanel = RightPanelParent.getContext().createAndShow(message.getFromUsername());
                RoomChatPanelCard roomChatPanelCard = RoomChatPanel.getContext().addPanel(userName);
                roomChatPanelCard.addOrUpdateMessageItem(message);

            }
            //新增或选择聊天列表
            RoomsPanel.getContext().addRoomOrOpenRoomNotSwitch(contacts,lastMsgPrefix+(message==null?msg.getContent():message.getPlaintext()),msgUnReadCount);

        });
    }
    /**
     * 处理自定义逻辑
     * @param msg 消息
     * @param msgType 消息类型
     */
    private void processMsg(AddMsgList msg,WXReceiveMsgCodeEnum msgType){
        //需要发送的消息
        List<MessageTools.Message> messages = null;
        switch (msgType) {
            case MSGTYPE_MAP:
                messages = msgHandler.mapMsgHandle(msg);
                break;
            case MSGTYPE_TEXT:
                //文本消息
                msg.setText(msg.getContent());
                messages = msgHandler.textMsgHandle(msg);
                break;
            case MSGTYPE_IMAGE:
                //存储消息
                messages = msgHandler.picMsgHandle(msg);
                break;
            case MSGTYPE_VOICE:
                messages = msgHandler.voiceMsgHandle(msg);
                break;
            case MSGTYPE_VIDEO:
            case MSGTYPE_MICROVIDEO:
                messages = msgHandler.videoMsgHandle(msg);
                break;
            case MSGTYPE_EMOTICON:
                messages = msgHandler.emotionMsgHandle(msg);
                break;
            case MSGTYPE_APP:
                switch (WXReceiveMsgCodeOfAppEnum.getByCode(msg.getAppMsgType())) {
                    case UNKNOWN:
                        break;
                    case FAVOURITE:
                        break;
                    case FILE:
                        break;
                    case PROGRAM:
                        break;
                    default:
                }
                messages = msgHandler.appMsgHandle(msg);
                break;
            case MSGTYPE_VOIPMSG:
                break;
            case MSGTYPE_VOIPNOTIFY:
                break;
            case MSGTYPE_VOIPINVITE:
                break;
            case MSGTYPE_LOCATION:
                break;
            case MSGTYPE_SYS:
            case MSGTYPE_STATUSNOTIFY:
                //当打开聊天窗口时会像该联系人发送该类型的消息
                //StatusNotifyCode = 1发送图片、视频消息完成  2进入聊天框  0发送文字完成
                messages = msgHandler.systemMsgHandle(msg);
                break;
            case MSGTYPE_SYSNOTICE:
                break;
            case MSGTYPE_POSSIBLEFRIEND_MSG:
                break;
            case MSGTYPE_VERIFYMSG:
                messages = msgHandler.addFriendMsgHandle(msg);
                break;
            case MSGTYPE_SHARECARD:
                messages = msgHandler.nameCardMsgHandle(msg);
                break;
            case MSGTYPE_RECALLED:
                messages = msgHandler.undoMsgHandle(msg);
                break;
            case UNKNOWN:
            default:
                log.warn(LogUtil.printFromMeg(msg, msgType.getCode()));
                break;
        }
        //发送消息
        MessageTools.sendMsgByUserId(messages, msg.getFromUserName());
    }
    /**
     *
     * @param msgType 消息类型
     * @return 明文
     */
    private String getPlainText(WXReceiveMsgCodeEnum msgType,AddMsgList msg){
        String plaintext = null;
        switch (msgType) {
            case MSGTYPE_MAP:
                plaintext = "[地图]";
                break;
            case MSGTYPE_TEXT:
                //文本消息
                break;
            case MSGTYPE_IMAGE:
                plaintext = "[图片]";
                break;
            case MSGTYPE_VOICE:
                plaintext = "[语音]";
                break;
            case MSGTYPE_VIDEO:
            case MSGTYPE_MICROVIDEO:
                plaintext = "[视频]";
                break;
            case MSGTYPE_EMOTICON:
                plaintext = "[表情]";
                break;
            case MSGTYPE_APP:
              /*  switch (WXReceiveMsgCodeOfAppEnum.getByCode(msg.getAppMsgType())) {
                    case UNKNOWN:
                        break;
                    case FAVOURITE:
                        break;
                    case FILE:
                        break;
                    case PROGRAM:
                        break;
                    default:
                }*/
                plaintext = "[APP]";
                break;
            case MSGTYPE_VOIPMSG:
                break;
            case MSGTYPE_VOIPNOTIFY:
                break;
            case MSGTYPE_VOIPINVITE:
                break;
            case MSGTYPE_LOCATION:
                plaintext = "[位置]";
                break;
            case MSGTYPE_SYS:
            case MSGTYPE_STATUSNOTIFY:
                //当打开聊天窗口时会像该联系人发送该类型的消息
                //StatusNotifyCode = 1发送图片、视频消息完成  2进入聊天框  0发送文字完成
                plaintext = "[系统消息]";
                break;
            case MSGTYPE_SYSNOTICE:
                break;
            case MSGTYPE_POSSIBLEFRIEND_MSG:
                break;
            case MSGTYPE_VERIFYMSG:
                break;
            case MSGTYPE_SHARECARD:
                plaintext = "[名片]";
                break;
            case MSGTYPE_RECALLED:
                Map<String, Object> map = MessageTools.parseUndoMsg(msg.getContent());
                plaintext = map.get("root.sysmsg.revokemsg.replacemsg").toString();
                break;
            case UNKNOWN:
            default:
                break;
        }
        return plaintext;
    }
    /**
     * 保存消息到数据库
     *
     * @param msg 消息
     */
    private Message storeMsgToDB(AddMsgList msg,Object plaintext) {
        try {
            boolean isFromSelf = msg.getFromUserName().endsWith(Core.getUserName());
            boolean isToSelf = msg.getToUserName().endsWith(Core.getUserName());

            Message build = cn.shu.wechat.beans.pojo.Message
                    .builder()
                    .plaintext(plaintext ==null?msg.getContent():plaintext.toString())
                    .content(msg.getContent())
                    .filePath(msg.getFilePath())
                    .createTime(new Date())
                    .fromNickname(isFromSelf ? Core.getNickName() : ContactsTools.getContactNickNameByUserName(msg.getFromUserName()))
                    .fromRemarkname(isFromSelf ? Core.getNickName() : ContactsTools.getContactRemarkNameByUserName(msg.getFromUserName()))
                    .fromUsername(msg.getFromUserName())
                    .id(UUID.randomUUID().toString().replace("-", ""))
                    .toNickname(isToSelf ? Core.getNickName() : ContactsTools.getContactNickNameByUserName(msg.getToUserName()))
                    .toRemarkname(isToSelf ? Core.getNickName() : ContactsTools.getContactRemarkNameByUserName(msg.getToUserName()))
                    .toUsername(msg.getToUserName())
                    .msgId(msg.getMsgId())
                    .msgType(msg.getMsgType())
                    .isSend(isFromSelf)
                    .appMsgType(msg.getAppMsgType())
                    .msgJson(JSON.toJSONString(msg))
                    .msgDesc(WXReceiveMsgCodeEnum.getByCode(msg.getMsgType()).getDesc())
                    .fromMemberOfGroupDisplayname(msg.isGroupMsg() ? ContactsTools.getMemberDisplayNameOfGroup(msg.getFromUserName(), msg.getMemberName()) : null)
                    .fromMemberOfGroupNickname(msg.isGroupMsg() ? ContactsTools.getMemberNickNameOfGroup(msg.getFromUserName(), msg.getMemberName()) : null)
                    .fromMemberOfGroupUsername(msg.isGroupMsg() ? msg.getMemberName() : null)
                    .slavePath(msg.getSlavePath())
                    .build();
            int insert = messageMapper.insert(build);
            return build;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 联系人修改消息
     *
     * @param msgLists
     */
    public static void produceModContactMsg(List<ModContactList> msgLists) {
        for (ModContactList msg : msgLists) {
            JSONObject newV = JSON.parseObject(JSON.toJSONString(msg));
            JSONObject oldV = null;
            if (msg.getUserName().startsWith("@@")) {
                //oldV = Core.getGroupMap().get(msg.getUserName());
            } else {
                //oldV = Core.getContactMap().get(msg.getUserName());
            }
            oldV = JSON.parseObject(JSON.toJSONString(JSON.toJavaObject(oldV, ModContactList.class)));
            if (oldV == null) {
                return;
            }
            ArrayList<MessageTools.Message> messages = new ArrayList<>();
            String name = oldV.getString("remarkName");
            if (StringUtil.isBlank(name)) {
                name = oldV.getString("nickName");
            }
            //存在key
            Map<String, Map<String, String>> differenceMap = JSONObjectUtil.getDifferenceMap(oldV, newV);
            if (differenceMap.size() > 0) {
                //Old与New存在差异
                String tip = "联系人";
                log.info("{}（{}）属性更新：{}", tip, name, differenceMap);
                //发送消息
                messages.add(MessageTools.Message.builder().content(tip + "（" + name + "）属性更新：" + mapToString(differenceMap))
                        .replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                        .build());
                // Core.getContactMap().put(msg.getUserName(), newV);
                //存储数据库
                store(differenceMap, oldV, messages);

                MessageTools.sendMsgByUserId(messages, msg.getUserName());
            }
        }


    }

    /**
     * 保存修改记录到数据库
     *
     * @param differenceMap
     * @param oldV
     */
    private static void store(Map<String, Map<String, String>> differenceMap, JSONObject oldV, ArrayList<MessageTools.Message> messages) {
        ArrayList<AttrHistory> attrHistories = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> stringMapEntry : differenceMap.entrySet()) {
            for (Map.Entry<String, String> stringStringEntry : stringMapEntry.getValue().entrySet()) {
                if (stringMapEntry.getKey().equals("headImgUrl")
                        || stringMapEntry.getKey().equals("头像更换")) {
/*					String oldHeadPath = DownloadTools.downloadHeadImg(stringStringEntry.getKey()
							, IMsgHandlerFaceImpl.savePath+File.separator+oldV.getString("UserName")+File.separator);*/
                    String oldHeadPath = Core.getContactHeadImgPath().get(oldV.getString("userName"));
                    String newHeadPath = DownloadTools.downloadHeadImgBig(stringStringEntry.getValue()
                            , oldV.getString("userName"));
                    Core.getContactHeadImgPath().put(oldV.getString("userName"), newHeadPath);
                    //更换前
                    messages.add(MessageTools.Message.builder()
                            .replyMsgTypeEnum(WXSendMsgCodeEnum.PIC)
                            //.toUserName("filehelper")
                            .content(oldHeadPath).build());
                    //更换后
                    messages.add(MessageTools.Message.builder()
                            .replyMsgTypeEnum(WXSendMsgCodeEnum.PIC)
                            //.toUserName("filehelper")
                            .content(newHeadPath).build());
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
                } else {
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
     *
     * @param differenceMap
     * @return
     */
    private static String mapToString(Map<String, Map<String, String>> differenceMap) {
        String str = "";
        for (Map.Entry<String, Map<String, String>> stringMapEntry : differenceMap.entrySet()) {
            Map<String, String> value = stringMapEntry.getValue();
            for (Map.Entry<String, String> stringStringEntry : value.entrySet()) {
                str = str + "\n【" + stringMapEntry.getKey() + "】(\"" + stringStringEntry.getKey() + "\" -> \"" + stringStringEntry.getValue() + "\")";
            }
        }
        return str;
    }

    /**
     * 联系人删除消息
     *
     * @param msgLists
     */
    public static void produceDelContactMsg(List<DelContactList> msgLists) {

    }

    /**
     * 聊天室修改消息
     *
     * @param msgLists
     */
    public static void produceModChatRoomMemberMsg(List<ModContactList> msgLists) {

    }


}
