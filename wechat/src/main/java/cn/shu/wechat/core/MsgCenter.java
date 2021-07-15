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
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.panels.ChatPanel;
import cn.shu.wechat.swing.panels.RoomsPanel;
import cn.shu.wechat.utils.CommonTools;
import cn.shu.wechat.utils.JSONObjectUtil;
import cn.shu.wechat.utils.LogUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.nlpcn.commons.lang.util.StringUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
    private IMsgHandlerFace msgHandler;//= IMsgHandlerFaceImpl.getiMsgHandlerFace();

    /* //public static MsgCenter getMessageCenter() {
         return messageCenter;
     }*/
    @Resource
    private MessageMapper messageMapper;
    //private static MsgCenter messageCenter= new MsgCenter();
    /**
     * 保存消息
     */
    public static ThreadLocal<AddMsgList> threadLocalOfMsg = new ThreadLocal<AddMsgList>();

    /**
     * 接收消息，放入队列
     *
     * @param msg 新消息
     * @author ShuXinSheng
     * @date 2017年4月23日 下午2:30:48
     */
    public void handleNewMsg(AddMsgList msg) {

        threadLocalOfMsg.set(msg);
        /**
         * 文本消息：content为文本内容
         * 图片视频文件消息：content为资源ID，@开头，发送消息时指定Content字段可直接发送，不需mediaid
         *                  如果消息是自己发的，content为xml
         */
        //消息格式化
        CommonTools.msgFormatter(msg);
        WXReceiveMsgCodeEnum msgType = WXReceiveMsgCodeEnum.getByCode(msg.getMsgType());
        if (msgType == MSGTYPE_TEXT && msg.getUrl().length() != 0) {
            //地图消息
            msg.setMsgType(WXReceiveMsgCodeEnum.MSGTYPE_MAP.getCode());
            msgType = WXReceiveMsgCodeEnum.MSGTYPE_MAP;
        }
        //下载资源文件
        String path = DownloadTools.getDownloadFilePath(msg);
        //false表示当前文件未下载完成，此时其它地方不能使用
        Hashtable<String, Boolean> fileDownloadStatus = DownloadTools.FILE_DOWNLOAD_STATUS;
        if (path != null) {
            fileDownloadStatus.put(path, false);
            DownloadTools.downloadFile(msg, path);
            msg.setFilePath(path);
        }
        //存储消息
        storeMsgToDB(msg);
        //打印日志
        String s = LogUtil.printFromMeg(msg, msgType.getDesc());
        if (s.startsWith("系统通知")) {
            log.debug(s);
        } else {
            MainFrame.getContext().playMessageSound();
            MainFrame.getContext().setTrayFlashing();
            log.info(s);
        }

        //新增消息列表
        ;
        String userName = msg.getFromUserName();
        if (userName.equals(Core.getUserName())) {
            userName = msg.getToUserName();
        }
        if (msgType.getCode() < 51) {
            //只显示常规消息
            //刷新消息
            ChatPanel.getContext().addOrUpdateMessageItem();

            Contacts contacts = Core.getMemberMap().get(userName);
            if (!Core.getRecentContacts().contains(contacts)) {
                //添加新房间并制定
                RoomsPanel.getContext().addRoom(contacts, msg.getContent(), 1);
                Core.getRecentContacts().add(contacts);
            } else {
                //更新消息 置顶
                RoomsPanel.getContext().updateRoomItem(userName, 1, msg.getContent(), System.currentTimeMillis());
            }
        }
        //需要发送的消息
        List<MessageTools.Result> results = null;
        switch (msgType) {
            case MSGTYPE_MAP:
                results = msgHandler.mapMsgHandle(msg);
                break;
            case MSGTYPE_TEXT:
                //文本消息
                msg.setText(msg.getContent());
                results = msgHandler.textMsgHandle(msg);
                break;
            case MSGTYPE_IMAGE:
                //存储消息
                results = msgHandler.picMsgHandle(msg);
                break;
            case MSGTYPE_VOICE:
                results = msgHandler.voiceMsgHandle(msg);
                break;
            case MSGTYPE_VIDEO:
            case MSGTYPE_MICROVIDEO:
                results = msgHandler.videoMsgHandle(msg);
                break;
            case MSGTYPE_EMOTICON:
                results = msgHandler.emotionMsgHandle(msg);
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
                }
                results = msgHandler.appMsgHandle(msg);
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
                results = msgHandler.systemMsgHandle(msg);
                break;
            case MSGTYPE_SYSNOTICE:
                break;
            case MSGTYPE_POSSIBLEFRIEND_MSG:
                break;
            case MSGTYPE_VERIFYMSG:
                results = msgHandler.addFriendMsgHandle(msg);
                break;
            case MSGTYPE_SHARECARD:
                results = msgHandler.nameCardMsgHandle(msg);
                break;
            case MSGTYPE_RECALLED:
                results = msgHandler.undoMsgHandle(msg);
                break;
            case UNKNOWN:
            default:
                log.warn(LogUtil.printFromMeg(msg, msgType.getCode()));
                break;
        }

        //发送消息
        MessageTools.sendMsgByUserId(results, msg.getFromUserName());
        threadLocalOfMsg.remove();
    }


    /**
     * 保存消息到数据库
     *
     * @param msg 消息
     */
    private void storeMsgToDB(AddMsgList msg) {
        try {
            boolean isFromSelf = msg.getFromUserName().endsWith(Core.getUserName());
            boolean isToSelf = msg.getToUserName().endsWith(Core.getUserName());
            Message build = Message
                    .builder()
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
                    .build();
            int insert = messageMapper.insert(build);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            ArrayList<MessageTools.Result> results = new ArrayList<>();
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
                results.add(MessageTools.Result.builder().content(tip + "（" + name + "）属性更新：" + mapToString(differenceMap))
                        .replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                        .build());
                // Core.getContactMap().put(msg.getUserName(), newV);
                //存储数据库
                store(differenceMap, oldV, results);

                MessageTools.sendMsgByUserId(results, msg.getUserName());
            }
        }


    }

    /**
     * 保存修改记录到数据库
     *
     * @param differenceMap
     * @param oldV
     */
    private static void store(Map<String, Map<String, String>> differenceMap, JSONObject oldV, ArrayList<MessageTools.Result> results) {
        ArrayList<AttrHistory> attrHistories = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> stringMapEntry : differenceMap.entrySet()) {
            for (Map.Entry<String, String> stringStringEntry : stringMapEntry.getValue().entrySet()) {
                if (stringMapEntry.getKey().equals("headImgUrl")
                        || stringMapEntry.getKey().equals("头像更换")) {
/*					String oldHeadPath = DownloadTools.downloadHeadImg(stringStringEntry.getKey()
							, IMsgHandlerFaceImpl.savePath+File.separator+oldV.getString("UserName")+File.separator);*/
                    String oldHeadPath = Core.getContactHeadImgPath().get(oldV.getString("userName"));
                    String newHeadPath = DownloadTools.downloadHeadImg(stringStringEntry.getValue()
                            , oldV.getString("userName"));
                    Core.getContactHeadImgPath().put(oldV.getString("userName"), newHeadPath);
                    //更换前
                    results.add(MessageTools.Result.builder()
                            .replyMsgTypeEnum(WXSendMsgCodeEnum.PIC)
                            //.toUserName("filehelper")
                            .content(oldHeadPath).build());
                    //更换后
                    results.add(MessageTools.Result.builder()
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
