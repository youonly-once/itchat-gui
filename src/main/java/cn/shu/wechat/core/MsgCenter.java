package cn.shu.wechat.core;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.api.DownloadTools;
import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.enums.URLEnum;
import cn.shu.wechat.enums.WXReceiveMsgCodeEnum;
import cn.shu.wechat.enums.WXReceiveMsgCodeOfAppEnum;
import cn.shu.wechat.enums.WXReceiveMsgStatusNotifyCodeEnum;
import cn.shu.wechat.mapper.MessageMapper;
import cn.shu.wechat.pojo.dto.msg.send.WebWXSendMsgResponse;
import cn.shu.wechat.pojo.dto.msg.sync.AddMsgList;
import cn.shu.wechat.pojo.dto.msg.sync.RecommendInfo;
import cn.shu.wechat.pojo.entity.Contacts;
import cn.shu.wechat.pojo.entity.Message;
import cn.shu.wechat.service.IMsgHandlerFace;
import cn.shu.wechat.service.LoginService;
import cn.shu.wechat.swing.entity.RoomItem;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.panels.RoomChatContainer;
import cn.shu.wechat.swing.panels.RoomsPanel;
import cn.shu.wechat.swing.utils.ChatUtil;
import cn.shu.wechat.utils.*;
import com.alibaba.fastjson.JSON;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.swing.*;
import java.util.*;
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
     * 群消息处理
     *
     * @param msg 消息
     */
    public static void groupMsgFormat(AddMsgList msg) {
        // 群消息与普通消息不同的是在其消息体（Content）中会包含发送者id及":<br/>"消息，
        // 这里需要处理一下，去掉多余信息，只保留消息内容
        //"群成员UserName:<br/>消息内容"
        if (!msg.getFromUserName().startsWith("@@") && !msg.getToUserName().startsWith("@@")) {
            return;
        }
        msg.setGroupMsg(Boolean.TRUE);
        if (msg.getFromUserName().equals(Core.getUserName())) {
            msg.setMemberName(Core.getUserName());
        } else {
            String content = msg.getContent();
            int index = content.indexOf(":<br/>");
            if (index != -1) {
                msg.setContent(content.substring(index + ":<br/>".length()));
                //发送消息的人
                msg.setMemberName(content.substring(0, index));
            }
        }


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
        ChatUtil.addMineNewMsg(messages);

    }

    /**
     * 第一次收到群消息 加载群成员详细细腻
     *
     * @param msg 消息
     */
    private Contacts loadUserInfo(AddMsgList msg) {

        String userName = msg.getFromUserName();
        if (userName.equals(Core.getUserName())) {
            userName = msg.getToUserName();
        }
        Contacts contacts = Core.getMemberMap().get(userName);
        if (contacts == null) {
            loginService.WebWxBatchGetContact(userName);
            contacts = Core.getMemberMap().get(userName);
        }
        if (userName.startsWith("@@")
                && !StringUtils.isEmpty(msg.getMemberName()) &&
                !Core.getMemberMap().containsKey(msg.getMemberName())) {
            //群成员非好友时，获取群成员的详细信息
            if (!Core.getMemberMap().containsKey(userName)
                    || CollectionUtils.isEmpty(contacts.getMemberlist())
                    || StringUtils.isEmpty(contacts.getMemberlist().get(0).getHeadimgurl())) {
                //使用头像地址来判断是否获取过成员详细信息
                List<Contacts> contactsList = loginService.WebWxBatchGetContact(userName);
                contacts.setMemberlist(contactsList);
            }
        }
        return contacts;
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
     *  @param message
     * @param msg
     * @param contacts
     */
    private void updateUI(Message message, AddMsgList msg, Contacts contacts) {
        if (message == null){
            return;
        }
        //################聊天面板消息处理###########3333
        //未读消息数量
        Integer msgUnReadCount = null;
        //消息预览文本
        String previewLastMsg = "";
        //是否播放音乐
        boolean isPlaySound;
        //是否闪烁任务栏图标
        boolean isFlashingTray;

        //新增消息列表
        String userName = msg.getFromUserName();

        if (userName.startsWith("@@")) {
            //群消息
            if (Core.getUserName().equals(msg.getMemberName())) {
                //自己在群里发的消息
                msgUnReadCount = 0;
                isPlaySound = false;
                isFlashingTray = false;
                previewLastMsg = Core.getNickName() + ": "+message.getPlaintext();
            } else {
                //其他人在群里发的消息
                if (isCurrRoom(message)) {
                    msgUnReadCount = 0;
                    isPlaySound = true;
                    isFlashingTray = false;
                }else if(ContactsTools.isMute(contacts)){
                    msgUnReadCount = 0;
                    isPlaySound = false;
                    isFlashingTray = false;
                } else{
                    isPlaySound = true;
                    isFlashingTray = true;
                    msgUnReadCount = 1;
                }
                previewLastMsg = ContactsTools.getMemberDisplayNameOfGroup(userName, msg.getMemberName()) + ": "+message.getPlaintext();
            }
        } else{
            //自己的消息，默认已读
            if (userName.equals(Core.getUserName())) {

                msgUnReadCount = 0;
                isPlaySound = false;
                isFlashingTray = false;
                previewLastMsg =  message.getPlaintext();
                userName = msg.getToUserName();

            } else {
                //其他人的消息
                if (isCurrRoom(message)) {
                    msgUnReadCount = 0;
                    isPlaySound = true;
                    isFlashingTray = false;
                }else if(ContactsTools.isMute(contacts)){
                    msgUnReadCount = 0;
                    isPlaySound = false;
                    isFlashingTray = false;
                } else{
                    isPlaySound = true;
                    isFlashingTray = true;
                    msgUnReadCount = 1;
                }
                previewLastMsg =  message.getPlaintext();
            }

        }

        if (isFlashingTray){
            MainFrame.getContext().setTrayFlashing(true);
        }
        if (isPlaySound){
            MainFrame.getContext().playMessageSound();
        }
        RoomsPanel.updateUnreadTotalCount(msgUnReadCount);

        //添加一条新消息
        ChatUtil.addNewMsg(message, userName, previewLastMsg, msgUnReadCount,ContactsTools.isMute(contacts));

    }

    /**
     * 判断是否为当前房间
     *
     * @param message 消息
     * @return {@code false 当前房间 }
     */
    private boolean isCurrRoom(Message message) {
        return message.getFromUsername().equals(RoomChatContainer.getCurrRoomId())
                || message.getToUsername().equals(RoomChatContainer.getCurrRoomId());
    }


    /**
     * 判断是否为本人发得消息
     *
     * @param message
     * @return
     */
    private boolean isSelfMessage(Message message) {
        return Core.getUserName().equals(message.getFromUsername())
                || Core.getUserName().equals(message.getFromMemberOfGroupUsername());
    }

    /**
     * 保存消息到数据库
     *
     * @param msg 消息
     */
    private Message newMsgToDBMessage(AddMsgList msg) {
        boolean isFromSelf = msg.getFromUserName().endsWith(Core.getUserName());
        boolean isToSelf = msg.getToUserName().endsWith(Core.getUserName());
        return Message
                .builder()
                .plaintext(msg.getPlainText() == null ? msg.getContent() : msg.getPlainText())
                .content(msg.getContent())
                .filePath(msg.getFilePath())
                .createTime(DateUtils.getCurrDateString(DateUtils.YYYY_MM_DD_HH_MM_SS))
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
                .fromMemberOfGroupDisplayname(msg.isGroupMsg() && !msg.getFromUserName().equals(Core.getUserName())
                        ? ContactsTools.getMemberDisplayNameOfGroup(msg.getFromUserName(), msg.getMemberName()) : null)
                .fromMemberOfGroupNickname(msg.isGroupMsg() && !msg.getFromUserName().equals(Core.getUserName())
                        ? ContactsTools.getMemberNickNameOfGroup(msg.getFromUserName(), msg.getMemberName()) : null)
                .fromMemberOfGroupUsername(msg.isGroupMsg() && !msg.getFromUserName().equals(Core.getUserName())
                        ? msg.getMemberName() : null)
                .slavePath(msg.getSlavePath())
                .response(JSON.toJSONString(WebWXSendMsgResponse.builder()
                        .BaseResponse(WebWXSendMsgResponse.BaseResponse.builder().Ret(0).build())
                        .LocalID(msg.getMsgId())
                        .MsgID(msg.getNewMsgId() + "").build()))
                .playLength(msg.getPlayLength())
                .imgHeight(msg.getImgHeight())
                .imgWidth(msg.getImgWidth())
                .voiceLength(msg.getVoiceLength())
                .fileName(msg.getFileName())
                .fileSize(msg.getFileSize())
                .contentMap(msg.getContentMap())
                .timestamp(System.currentTimeMillis())
                .oriContent(msg.getOriContent())
                .build();
    }

    /**
     * 处理联系人修改消息
     *
     * @param contacts 联系人
     */
    public void handleModContact(Contacts contacts) {
        log.info("联系人修改");
        if (contacts != null) {
            Core.getMemberMap().put(contacts.getUsername(), contacts);
        }

    }

    /**
     * 处理联系人修改消息
     *
     * @param modContactList
     */
    public void handleModContact(List<Contacts> modContactList) {

        if (modContactList != null && !modContactList.isEmpty()) {
            for (Contacts contacts : modContactList) {
                handleModContact(contacts);
            }

        }

    }

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
            //地图消息 地图消息的发送
            msg.setMsgType(WXReceiveMsgCodeEnum.MSGTYPE_MAP.getCode());
            msgType = WXReceiveMsgCodeEnum.MSGTYPE_MAP;
        }
        //=============群消息处理=============
        groupMsgFormat(msg);


        msg.setType(msgType);
        //=============加载群成员==============
        Contacts contacts = loadUserInfo(msg);

        //=============打印日志==============
        String logStr = LogUtil.printFromMeg(msg, msgType.getDesc());
        //=============如果是当前房间 发送已读通知==============
        if (msg.getFromUserName().equals(RoomChatContainer.getCurrRoomId())) {
            ExecutorServiceUtil.getGlobalExecutorService().execute(() -> MessageTools.sendStatusNotify(msg.getFromUserName()));
        }
        log.info(logStr);


        //下载资源后缀
        String ext = null;
        //下载资源文件名
        String fileName = msg.getMsgId();
        //存储的消息
        Message message = null;

        switch (msgType) {
            case MSGTYPE_MAP: {
                Map<String, Object> map = XmlStreamUtil.toMap(msg.getOriContent());
                String thumbUrl = URLEnum.BASE_URL.getUrl() + msg.getContent();
                String url = msg.getUrl();
                String title = map.get("msg.location.attr.poiname").toString();
                String subTitle = map.get("msg.location.attr.label").toString();

                msg.setPlainText("[地图]" + title);
                ext = ".gif";
                downloadFile(msg, fileName, ext);
                message = newMsgToDBMessage(msg);
                message.setMsgType(WXReceiveMsgCodeEnum.MSGTYPE_APP.getCode());
                message.setAppMsgType(WXReceiveMsgCodeOfAppEnum.PICTURE.getType());
                message.setThumbUrl(msg.getFilePath());
                message.setUrl(url);
                message.setTitle(title);

            }
                break;
            case MSGTYPE_TEXT:
                //消息格式化
                CommonTools.emojiFormatter(msg);
                textMsgFormat(msg);
                //文本消息
                msg.setPlainText(msg.getContent());
                message = newMsgToDBMessage(msg);
                break;
            case MSGTYPE_IMAGE:
                msg.setPlainText("[图片]");
                ext = ".gif";
                //存储消息
                downloadThumImg(msg, fileName, ext);
                downloadFile(msg, fileName, ext);
                message = newMsgToDBMessage(msg);
                break;
            case MSGTYPE_VOICE:
                ext = ".mp3";
                msg.setPlainText("[语音]");
                downloadFile(msg, fileName, ext);
                downloadThumImg(msg, fileName, ext);
                message = newMsgToDBMessage(msg);
                break;
            case MSGTYPE_VIDEO:
            case MSGTYPE_MICROVIDEO:
                ext = ".mp4";
                msg.setPlainText("[视频]");
                downloadFile(msg, fileName, ext);
                downloadThumImg(msg, fileName, ext);
                message = newMsgToDBMessage(msg);
                break;
            case MSGTYPE_EMOTICON:
                msg.setPlainText("[表情]");
                ext = ".gif";
                downloadFile(msg, fileName, ext);
                message = newMsgToDBMessage(msg);
                break;
            case MSGTYPE_APP: {
                Map<String, Object> map = new HashMap<>();
                try {
                    map = XmlStreamUtil.toMap(msg.getContent());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                msg.setContentMap(map);
                Object desc = map.get("msg.appmsg.des");
                Object url = map.get("msg.appmsg.url");
                Object title = map.get("msg.appmsg.title");
                Object thumbUrl = map.get("msg.appmsg.thumburl");
                Object sourceIconUrl = map.get("msg.appmsg.weappinfo.weappiconurl");
                Object sourceName = map.get("msg.appmsg.sourcedisplayname");
                Object height = map.get("msg.appmsg.appattach.cdnthumbheight");
                Object width = map.get("msg.appmsg.appattach.cdnthumbwidth");

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
                    default:
                    case PROGRAM:
                        msg.setPlainText("[小程序]" + title);
                        break;
                    case MUSIC:
                        msg.setPlainText("[音乐]" + title);
                    case PICTURE:
                        sourceName = map.get("msg.appinfo.appname");
                        ext = ".gif";
                        downloadFile(msg, fileName, ext);
                        msg.setPlainText("[小程序]图片");
                        break;
                    case TRANSFER:
                        msg.setPlainText(desc == null ? "[微信转账]" : desc.toString());
                        msg.setAppMsgType(WXReceiveMsgCodeOfAppEnum.LINK.getType());
                        break;
                  /*  default:
                        msg.setMsgType(MSGTYPE_TEXT.getCode());
                        msg.setPlainText(msg.getContent());
                        break;*/


                }
                message = newMsgToDBMessage(msg);
                message.setTitle(title == null ? null : title.toString());
                message.setDesc(desc == null ? null : desc.toString());
                message.setImgWidth(width == null ? null : Integer.parseInt(width.toString()));
                message.setImgHeight(height == null ? null : Integer.parseInt(height.toString()));
                message.setThumbUrl(thumbUrl == null ? null : thumbUrl.toString());
                message.setUrl(url == null ? null : url.toString());
                message.setSourceIconUrl(sourceIconUrl == null ? null : sourceIconUrl.toString());
                message.setSourceName(sourceName == null ? null : sourceName.toString());
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
                message = newMsgToDBMessage(msg);
                break;
            case MSGTYPE_SYS:{
                msg.setPlainText(msg.getContent());
                message = newMsgToDBMessage(msg);
                break;
            }
            case MSGTYPE_STATUSNOTIFY: {
                switch ( WXReceiveMsgStatusNotifyCodeEnum.getByCode(msg.getStatusNotifyCode())){
                    case NOTIFY_USER_NAME: {
                        new SwingWorker<Object, Object>() {
                            ArrayList<RoomItem> rooms = new ArrayList<>();

                            @Override
                            protected Object doInBackground() {

                                for (String userId : msg.getStatusNotifyUserName().split(",")) {
                                    rooms.add(new RoomItem(Core.getMemberMap().get(userId), "", 0,false));
                                }
                                return null;
                            }

                            @Override
                            protected void done() {
                                RoomsPanel.getContext().addRoom(rooms);
                            }
                        }.execute();
                        break;
                    }
                    case READ:
                        //=============用户在其他平台消息已读的通知=============
                        //更新聊天列表未读数量
                        RoomsPanel.getContext().hasReadCount(msg.getToUserName());
                        return;
                    case DEFAULT:

                    default:

                            break;

                }

                break;
            }
            case MSGTYPE_SYSNOTICE:
                break;
            case MSGTYPE_POSSIBLEFRIEND_MSG:
                break;
            case MSGTYPE_VERIFYMSG:{
                msg.setPlainText("[好友申请]");
                message = newMsgToDBMessage(msg);

                Map<String, Object> map = new HashMap<>();
                try {
                    map = XmlStreamUtil.toMap(msg.getContent());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Object thumbUrl = map.get("msg.attr.smallheadimgurl");
                Object headImgUrl = map.get("msg.attr.bigheadimgurl");
                Object id = map.get("msg.attr.alias");
                RecommendInfo recommendInfo = msg.getRecommendInfo();
                message.setContactsNickName(recommendInfo.getNickName());
                message.setContactsId(id == null ? null : id.toString());
                message.setContactsProvince(recommendInfo.getProvince());
                message.setContactsCity(recommendInfo.getCity());
                message.setContactsSex(recommendInfo.getSex());
                message.setThumbUrl(thumbUrl == null ? null : thumbUrl.toString());
                message.setContactsUserName(recommendInfo.getUserName());
                message.setContactsTicket(recommendInfo.getTicket());
                message.setContactsHeadImgUrl(headImgUrl == null ? null : headImgUrl.toString());
                break;
            }
            case MSGTYPE_SHARECARD: {

                Map<String, Object> map = new HashMap<>();
                try {
                    map = XmlStreamUtil.toMap(msg.getContent());
                } catch (Exception e) {
                    e.printStackTrace();
                }


                msg.setPlainText("[名片消息]");
                message = newMsgToDBMessage(msg);

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

               //
            }
            break;
            case MSGTYPE_RECALLED: {
                Map<String, Object> map = XmlStreamUtil.toMap(msg.getContent());
                msg.setContentMap(map);
                msg.setPlainText(map.get("sysmsg.revokemsg.replacemsg").toString());
                message = newMsgToDBMessage(msg);
            }
            break;
            case UNKNOWN:
            default:
                log.warn(LogUtil.printFromMeg(msg, msgType.getCode()));
                break;
        }
        if (message != null) {
            if (message.getFromUsername().startsWith("@@") || message.getToUsername().startsWith("@@")) {
                message.setGroup(true);
            }else{
                message.setGroup(false);
            }
            //显示的名称
            if (message.isGroup()) {
                //如果是群则显示群成员名称
                message.setPlainName(message.getFromMemberOfGroupDisplayname());
            } else {
                message.setPlainName(ContactsTools.getContactDisplayNameByUserName(message.getFromUsername()));
            }

            //聊天界面
            updateUI(message, msg,contacts);
            messageMapper.insert(message);
        }
        processExtra(msg);

    }

    /**
     * 文本消息格式化
     * @param msg 消息
     */
    private void textMsgFormat(AddMsgList msg){
        String content = msg.getContent();
        //获取自己在群里的备注
        String groupMyUserNickNameOfGroup = ContactsTools.getMemberDisplayNameOfGroup(msg.getFromUserName(), Core.getUserName());
        //判断是否@自己
        if ( groupMyUserNickNameOfGroup!= null
                && content.contains("@" + groupMyUserNickNameOfGroup + " ")) {
            msg.setMentionMe(true);
            //消息发送成员昵称
            String groupOtherUserNickNameOfGroup =
                    ContactsTools.getMemberDisplayNameOfGroup(msg.getFromUserName(), msg.getMemberName());
            msg.setMentionMeUserNickName(groupOtherUserNickNameOfGroup);
        }
        //@用户后面的空白符 JLabel不支持显示  需要替换
        msg.setContent(content.replace(" "," "));
    }
}
