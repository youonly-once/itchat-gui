package cn.shu.wechat.core;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.api.DownloadTools;
import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.beans.msg.send.WebWXSendMsgResponse;
import cn.shu.wechat.beans.msg.sync.AddMsgList;
import cn.shu.wechat.beans.pojo.Contacts;
import cn.shu.wechat.beans.pojo.Message;
import cn.shu.wechat.enums.WXReceiveMsgCodeEnum;
import cn.shu.wechat.enums.WXReceiveMsgCodeOfAppEnum;
import cn.shu.wechat.face.IMsgHandlerFace;
import cn.shu.wechat.mapper.MessageMapper;
import cn.shu.wechat.service.LoginService;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.panels.RoomChatPanel;
import cn.shu.wechat.swing.panels.RoomsPanel;
import cn.shu.wechat.utils.*;
import com.alibaba.fastjson.JSON;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.swing.*;
import java.io.File;
import java.text.ParseException;
import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
    private LoginService loginService;

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
        //=============地图消息，特殊处理=============
        if (msgType == MSGTYPE_TEXT && !StringUtils.isEmpty(msg.getUrl())) {
            //地图消息
            msg.setMsgType(WXReceiveMsgCodeEnum.MSGTYPE_MAP.getCode());
            msgType = WXReceiveMsgCodeEnum.MSGTYPE_MAP;
        }
        msg.setType(msgType);
         //=============加载群成员==============
        loadUserInfo(msg);

        //=============打印日志==============
        String logStr = LogUtil.printFromMeg(msg, msgType.getDesc());
        //=============如果是当前房间 发送已读通知==============
        if (RoomChatPanel.getContext().getCurrRoomId().equals(msg.getFromUserName())) {
            ExecutorServiceUtil.getGlobalExecutorService().execute(() -> MessageTools.sendStatusNotify(msg.getFromUserName()));
        }

        //=============用户在其他平台消息已读的通知=============
        if (msg.getMsgType() == WXReceiveMsgCodeEnum.MSGTYPE_STATUSNOTIFY.getCode()) {
            log.debug(logStr);
            //更新聊天列表未读数量
            RoomsPanel.getContext().updateUnreadCount(msg.getToUserName(), 0);
            return;
        } else {
            log.info(logStr);
        }


        //下载资源后缀
        String ext = null;
        //下载资源文件名
        String fileName = msg.getMsgId();
        //存储的消息
        Message message = null;

        switch (msgType) {
            case MSGTYPE_MAP:
                msg.setPlainText("[地图，请在手机上查看]");
                message = storeMsgToDB(msg);
                ext = ".gif";
                downloadFile(msg, fileName, ext);
                break;
            case MSGTYPE_TEXT:
                //消息格式化
                CommonTools.msgFormatter(msg);
                //文本消息
                msg.setPlainText(msg.getContent());
                message = storeMsgToDB(msg);
                break;
            case MSGTYPE_IMAGE:
                msg.setPlainText("[图片]");
                ext = ".gif";
                //存储消息
                downloadThumImg(msg, fileName, ext);
                downloadFile(msg, fileName, ext);
                message = storeMsgToDB(msg);
                break;
            case MSGTYPE_VOICE:
                ext = ".mp3";
                msg.setPlainText("[语音]");
                downloadFile(msg, fileName, ext);
                downloadThumImg(msg, fileName, ext);
                message = storeMsgToDB(msg);

                break;
            case MSGTYPE_VIDEO:
            case MSGTYPE_MICROVIDEO:
                ext = ".mp4";
                msg.setPlainText("[视频]");
                downloadFile(msg, fileName, ext);
                downloadThumImg(msg, fileName, ext);
                message = storeMsgToDB(msg);

                break;
            case MSGTYPE_EMOTICON:
                msg.setPlainText("[表情]");
                ext = ".gif";
                downloadFile(msg, fileName, ext);
                message = storeMsgToDB(msg);

                break;
            case MSGTYPE_APP:
                Map<String, Object> map = XmlStreamUtil.toMap(msg.getContent());
                Object title = map.get("msg.appmsg.title");
                switch (WXReceiveMsgCodeOfAppEnum.getByCode(msg.getAppMsgType())) {
                    case LINK:
                        msg.setPlainText("[链接]" + title);
                        break;
                    case FILE:
                        if (title != null) {
                            fileName = title.toString();
                        }
                        int i = msg.getFileName().lastIndexOf(".");
                        if (i != -1) {
                            ext = msg.getFileName().substring(i);
                        }
                        msg.setPlainText("[文件]" + title);
                        downloadFile(msg, fileName, ext);
                        break;
                    case PROGRAM:
                        msg.setPlainText("[小程序]" + title);
                        break;
                    case PICTURE:
                        ext = ".gif";
                        downloadFile(msg, fileName, ext);
                        msg.setPlainText("[小程序]图片");
                        break;
                    default:
                        break;


                }
                message = storeMsgToDB(msg);
                if (message != null) {
                    message.setContentMap(map);
                }
                break;
            case MSGTYPE_VOIPMSG:
                break;
            case MSGTYPE_VOIPNOTIFY:
                break;
            case MSGTYPE_VOIPINVITE:
                break;
            case MSGTYPE_LOCATION:
                msg.setPlainText("[位置，请在手机上查看]");
                message = storeMsgToDB(msg);
                break;
            case MSGTYPE_SYS:
            case MSGTYPE_STATUSNOTIFY:
                msg.setPlainText("[系统消息]");
                message = storeMsgToDB(msg);
                break;
            case MSGTYPE_SYSNOTICE:
                break;
            case MSGTYPE_POSSIBLEFRIEND_MSG:
                break;
            case MSGTYPE_VERIFYMSG:
                message = storeMsgToDB(msg);
                break;
            case MSGTYPE_SHARECARD:
                msg.setPlainText("[名片消息，请在手机上查看]");
                message = storeMsgToDB(msg);
                break;
            case MSGTYPE_RECALLED:
                map = XmlStreamUtil.toMap(msg.getContent());
                msg.setPlainText(map.get("sysmsg.revokemsg.replacemsg").toString());
                message = storeMsgToDB(msg);
                if (message != null) {
                    message.setContentMap(map);
                }
                break;
            case UNKNOWN:
            default:
                log.warn(LogUtil.printFromMeg(msg, msgType.getCode()));
                break;
        }

        //聊天界面
        updateUI(message, msg);
        processExtra(msg);

    }

    /**
     * 处理额外消息
     *
     * @param msg
     */
    private void processExtra(AddMsgList msg) {
        //需要发送的消息
        List<Message> messages = null;
        switch (msg.getType()) {
            case MSGTYPE_MAP:
                messages = msgHandler.mapMsgHandle(msg);
                break;
            case MSGTYPE_TEXT:
                messages = msgHandler.textMsgHandle(msg);
                break;
            case MSGTYPE_IMAGE:
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
                msg.setPlainText("[名片消息，请在手机上查看]");
                messages = msgHandler.nameCardMsgHandle(msg);
                break;
            case MSGTYPE_RECALLED:
                messages = msgHandler.undoMsgHandle(msg);
                break;
            case UNKNOWN:
            default:
                log.warn(LogUtil.printFromMeg(msg, msg.getType().getCode()));
                break;
        }
        //发送消息
        MessageTools.sendMsgByUserId(messages);
    }

    /**
     *  第一次收到群消息 加载群成员详细细腻
     * @param msg 消息
     */
    private void loadUserInfo(AddMsgList msg){

        String userName = msg.getFromUserName();
        Contacts contacts = Core.getMemberMap().get(userName);
        if (contacts==null){
            loginService.WebWxBatchGetContact(userName);
            contacts = Core.getMemberMap().get(userName);
        }
        if (userName.startsWith("@@")
                &&!StringUtils.isEmpty(msg.getMemberName())&&
                !Core.getMemberMap().containsKey(msg.getMemberName())){
            //群成员非好友时，获取群成员的详细信息
            if (!Core.getMemberMap().containsKey(userName)
                    || CollectionUtils.isEmpty(contacts.getMemberlist())
                    || StringUtils.isEmpty(contacts.getMemberlist().get(0).getHeadimgurl())){
                //使用头像地址来判断是否获取过成员详细信息
                List<Contacts> contactsList = loginService.WebWxBatchGetContact(userName);
                contacts.setMemberlist(contactsList);
            }
        }
    }

    /**
     * 下载文件
     */
    private void downloadFile(AddMsgList msg, String filename, String ext) {

        ConcurrentHashMap<String, Boolean> fileDownloadStatus = DownloadTools.FILE_DOWNLOAD_STATUS;
        //下载资源文件
        String path = DownloadTools.getDownloadFilePath(msg, filename, ext);
        msg.setFilePath(path);

        fileDownloadStatus.put(path, false);
        ExecutorServiceUtil.getGlobalExecutorService().execute(() -> DownloadTools.getDownloadFn(msg));


    }

    /**
     * 下载消息缩略图
     */
    private void downloadThumImg(AddMsgList msg, String filename, String ext) {
        String pathSlave = DownloadTools.getDownloadThumImgPath(msg, filename, ext);
        ConcurrentHashMap<String, Boolean> fileDownloadStatus = DownloadTools.FILE_DOWNLOAD_STATUS;
        fileDownloadStatus.put(pathSlave, false);
        msg.setSlavePath(pathSlave);
        ExecutorServiceUtil.getGlobalExecutorService().execute(() -> DownloadTools.downloadFileByMsgId(msg.getNewMsgId(), pathSlave));
    }

    /**
     * 更新UI
     *
     * @param message
     * @param msg
     */
    private void updateUI(Message message, AddMsgList msg) {
        //################3聊天面板消息处理###########3333
        int msgUnReadCount = 1;
        String lastMsgPrefix = "";
        //新增消息列表
        String userName = msg.getFromUserName();
        if (userName.equals(Core.getUserName())) {
            //自己的消息，默认已读
            msgUnReadCount = 0;
            userName = msg.getToUserName();
        } else if (userName.startsWith("@@")) {
            //自己在群里发的消息
            if (Core.getUserName().equals(msg.getMemberName())) {
                lastMsgPrefix = Core.getNickName() + ": ";
                msgUnReadCount = 0;
            } else {
                lastMsgPrefix = ContactsTools.getMemberDisplayNameOfGroup(userName, msg.getMemberName()) + ": ";
            }
        } else {
            MainFrame.getContext().playMessageSound();
            MainFrame.getContext().setTrayFlashing();
        }

        String lastMsg = lastMsgPrefix+(message==null?msg.getContent():message.getPlaintext());
        String roomId = userName;
        int count = msgUnReadCount;
        SwingUtilities.invokeLater(() -> {

            //刷新消息
            if (message!=null){
                message.setProcess(100);
                message.setIsSend(true);
                //新消息来了后创建房间
                //创建房间的时候会从数据库加载历史消息，由于这次的消息已经写入了数据库，所以不用再添加了
                if (RoomChatPanel.getContext().exists(roomId)) {
                    try {
                        RoomChatPanel.getContext().get(roomId).addMessageItemToEnd(message);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return;
                    }
                }else{
                    RoomChatPanel.getContext().addPanel(roomId);
                }
            }
            //新增或选择聊天列表
            RoomsPanel.getContext().addRoomOrOpenRoomNotSwitch(roomId, lastMsg, count);

        });
    }

    /**
     * 保存消息到数据库
     *
     * @param msg 消息
     */
    private Message storeMsgToDB(AddMsgList msg) {
        try {
            boolean isFromSelf = msg.getFromUserName().endsWith(Core.getUserName());
            boolean isToSelf = msg.getToUserName().endsWith(Core.getUserName());
            Message build = Message
                    .builder()
                    .plaintext(msg.getPlainText() == null ? msg.getContent() : msg.getPlainText())
                    .content(msg.getContent())
                    .filePath(msg.getFilePath())
                    .createTime(DateUtils.getCurrDateString(DateUtils.yyyy_mm_dd_hh_mm_ss))
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
                    .fromMemberOfGroupDisplayname(msg.isGroupMsg()&&!msg.getFromUserName().equals(Core.getUserName())
                            ? ContactsTools.getMemberDisplayNameOfGroup(msg.getFromUserName(), msg.getMemberName()) : null)
                    .fromMemberOfGroupNickname(msg.isGroupMsg()&&!msg.getFromUserName().equals(Core.getUserName())
                            ? ContactsTools.getMemberNickNameOfGroup(msg.getFromUserName(), msg.getMemberName()) : null)
                    .fromMemberOfGroupUsername(msg.isGroupMsg() &&!msg.getFromUserName().equals(Core.getUserName())
                            ? msg.getMemberName() : null)
                    .slavePath(msg.getSlavePath())
                    .response(JSON.toJSONString(WebWXSendMsgResponse.builder()
                            .BaseResponse(WebWXSendMsgResponse.BaseResponse.builder().Ret(0).build())
                    .LocalID(msg.getMsgId())
                    .MsgID(msg.getNewMsgId()+"").build()))
                    .playLength(msg.getPlayLength())
                    .imgHeight(msg.getImgHeight())
                    .imgWidth(msg.getImgWidth())
                    .voiceLength(msg.getVoiceLength())
                    .fileName(msg.getFileName())
                    .fileSize(msg.getFileSize())
                    .build();
            int insert = messageMapper.insert(build);
            return build;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    /**
     * 处理联系人修改消息
     * @param contacts
     */
    public void handleModContact(Contacts contacts){

        if (contacts!=null){
            Core.getMemberMap().put(contacts.getUsername(),contacts);
        }

    }
    /**
     * 处理联系人修改消息
     * @param modContactList
     */
    public void handleModContact(  List<Contacts> modContactList){

        if (modContactList!=null && !modContactList.isEmpty()){
            for (Contacts contacts : modContactList) {
                Core.getMemberMap().put(contacts.getUsername(),contacts);
            }

        }

    }

}
