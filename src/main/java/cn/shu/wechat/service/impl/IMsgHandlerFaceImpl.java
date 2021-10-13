package cn.shu.wechat.service.impl;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.core.MsgCenter;
import cn.shu.wechat.enums.WXReceiveMsgCodeEnum;
import cn.shu.wechat.enums.WXReceiveMsgCodeOfAppEnum;
import cn.shu.wechat.enums.WXSendMsgCodeEnum;
import cn.shu.wechat.mapper.MessageMapper;
import cn.shu.wechat.mapper.StatusMapper;
import cn.shu.wechat.pojo.dto.msg.sync.AddMsgList;
import cn.shu.wechat.pojo.dto.tuling.enums.ResultType;
import cn.shu.wechat.pojo.dto.tuling.response.Results;
import cn.shu.wechat.pojo.dto.tuling.response.TuLingResponseBean;
import cn.shu.wechat.pojo.entity.Message;
import cn.shu.wechat.pojo.entity.MessageExample;
import cn.shu.wechat.pojo.entity.Status;
import cn.shu.wechat.pojo.entity.StatusExample;
import cn.shu.wechat.service.IMsgHandlerFace;
import cn.shu.wechat.swing.panels.RoomChatContainer;
import cn.shu.wechat.utils.*;
import com.alibaba.fastjson.JSON;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;


@Log4j2
@Component
public class IMsgHandlerFaceImpl implements IMsgHandlerFace {

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private StatusMapper statusMapper;
    /**
     * autoChatUserNameList 包含 发送者：自动回复
     * 不包含：autoChatWithPersonal = true ：自动回复，false ：不回复
     */
    private boolean autoChatWithPersonal = false;

    /**
     * 自动聊天联系人列表，包括个人、群...
     */
    public final Set<String> autoChatUserNameList = new HashSet<>();

    @Resource
    private ChartUtil chartUtil;


    /**
     * 已关闭防撤回联系人列表
     */
    public final Set<String> nonPreventUndoMsgUserName = new HashSet<>();

    @PostConstruct
    private void initSet() {
        log.info("11. 获取自动聊天列表及防撤回列表");
        List<Status> statuses = statusMapper.selectByExample(new StatusExample());
        for (Status status : statuses) {
            if (status.getAutoStatus() != null && status.getAutoStatus() == 1) {
                autoChatUserNameList.add(status.getName());
            }
            if (status.getUndoStatus() != null && status.getUndoStatus() == 2) {
                nonPreventUndoMsgUserName.add(status.getName());
            }
        }
    }

    /**
     * 消息控制命令
     *
     * @param msg 消息
     * @return 回复消息
     */
    private List<Message> controlCommandHandler(AddMsgList msg) {
        String text = msg.getPlainText().toLowerCase();
        List<Message> messages = new ArrayList<>();

        //=========================手动发送消息=====================
        String[] split = msg.getPlainText().split("：");
        if (split.length >= 2 && msg.getFromUserName().equals(Core.getUserName())) {
            try {
                long sleep = 100;
                try {
                    sleep = Long.parseLong(split[2]);
                } catch (ArrayIndexOutOfBoundsException e) {

                }
                String s = split[1];
                int i = Integer.parseInt(s);
                messages.add(Message.builder()
                        .content("开始发送：" + i + "个" + split[0])
                        .toUsername(msg.getToUserName())
                        .msgType(WXSendMsgCodeEnum.TEXT.getCode())
                        .build());
                for (int j = 0; j < i; j++) {
                    messages.add(Message.builder()
                            .content(split[0])
                            .msgType(WXSendMsgCodeEnum.TEXT.getCode())
                            .toUsername(msg.getToUserName())
                            .build());
                }
                return messages;

            } catch (NumberFormatException e) {
            }
        }
        //============炸弹消息===================
        if (msg.getPlainText().equals("[Bomb]") || msg.getPlainText().equals("[炸弹]")) {
            String userName = Core.getUserSelf().getUsername();
            if (!msg.getFromUserName().equals(userName)) {
                for (int i = 0; i < 1; i++) {
                    messages.add(Message.builder()
                            .content("[Bomb]")
                            .msgType(WXSendMsgCodeEnum.TEXT.getCode())
                            .build());
                }
                return messages;
            }
        }

        /**
         * 自己发的消息
         * 回复时则发送给接收方，而不是消息发送者
         */
        /*String objectUserName = msg.getFromUserName();*/
        String toUserName = msg.getFromUserName();
        if (msg.getFromUserName().equals(Core.getUserName())) {
            toUserName = msg.getToUserName();
        }
        String remarkNameByGroupUserName = ContactsTools.getContactDisplayNameByUserName(toUserName);
        switch (text) {
            case "help":
            case "/h":
                if (msg.isGroupMsg()) {
                    //群消息
                    messages.add(Message.builder()
                            .content("1、【oauto/cauto】\n\t开启/关闭群消息自动回复\n"
                                    + "2、【opundo/cpundo】\n\t开启/关闭群消息防撤回\n"
                                    + "3、【ggr】\n\t群成员性别比例图\n"
                                    + "4、【gpr】\n\t群成员省市分布图\n"
                                    // +"opundo/cpundo：群成员活跃度\n"
                                    + "5、【op/cp】\n\t开启/关闭全局个人用户消息自动回复\n"
                                    + "6、【gma10】\n\t群成员活跃度TOP10\n"
                                    + "7、【mf10】\n\t聊天消息关键词TOP10\n"
                            )
                            .toUsername(toUserName)
                            .msgType(WXSendMsgCodeEnum.TEXT.getCode())
                            .build());

                } else {
                    //个人消息
                    messages.add(Message.builder()
                            .content("1、【oauto/cauto】\n\t开启/关闭当前联系人自动回复\n"
                                    + "2、【opundo/cpundo】\n\t开启/关闭当前联系人消息防撤回\n"
                                    + "3、【op/cp】\n\t开启/关闭全局个人用户消息自动回复\n"
                                    + "4、【mf10】\n\t聊天消息关键词TOP10\n")
                            .toUsername(toUserName)
                            .msgType(WXSendMsgCodeEnum.TEXT.getCode())
                            .build());
                }
                break;
            case "op":
                autoChatWithPersonal = true;
                messages.add(Message.builder().msgType(WXSendMsgCodeEnum.TEXT.getCode())
                        .content("已开启全局个人用户自动回复功能")
                        .toUsername(toUserName).build());
                log.info("已开启全局个人用户自动回复功能");
                break;
            case "cp":
                autoChatWithPersonal = false;
                messages.add(Message.builder().msgType(WXSendMsgCodeEnum.TEXT.getCode())
                        .content("已关闭全局个人用户自动回复功能")
                        .toUsername(toUserName).build());
                log.info("已关闭全局个人用户自动回复功能");
                break;
            case "oauto":
                String to = ContactsTools.getContactDisplayNameByUserName(toUserName);
                autoChatUserNameList.add(to);
                Status build = Status.builder().name(to)
                        .autoStatus((short) 1).build();
                statusMapper.insertOrUpdateSelectiveForSqlite(build);
                RoomChatContainer.get(toUserName).getChatPanel().getMessageEditorPanel().setUndoAndAutoLabel();

                messages.add(Message.builder().msgType(WXSendMsgCodeEnum.TEXT.getCode())
                        .content("已开启【" + remarkNameByGroupUserName + "】自动回复功能")
                        .toUsername(toUserName).build());
                log.info("已开启【" + remarkNameByGroupUserName + "】自动回复功能");
                break;
            case "cauto":
                to = ContactsTools.getContactDisplayNameByUserName(toUserName);
                autoChatUserNameList.remove(to);
               build = Status.builder().name(to)
                        .autoStatus((short) 2).build();
                statusMapper.insertOrUpdateSelectiveForSqlite(build);
                RoomChatContainer.get(toUserName).getChatPanel().getMessageEditorPanel().setUndoAndAutoLabel();
                messages.add(Message.builder().msgType(WXSendMsgCodeEnum.TEXT.getCode())
                        .content("已关闭【" + remarkNameByGroupUserName + "】自动回复功能")
                        .toUsername(toUserName).build());
                log.info("已关闭【" + remarkNameByGroupUserName + "】自动回复功能");
                break;
            case "opundo":
                to = ContactsTools.getContactDisplayNameByUserName(toUserName);
                nonPreventUndoMsgUserName.remove(to);
                build = Status.builder().name(to)
                        .undoStatus((short) 1).build();
                statusMapper.insertOrUpdateSelectiveForSqlite(build);
                RoomChatContainer.get(toUserName).getChatPanel().getMessageEditorPanel().setUndoAndAutoLabel();

                messages.add(Message.builder().msgType(WXSendMsgCodeEnum.TEXT.getCode())
                        .content("已开启【" + remarkNameByGroupUserName + "】防撤回功能")
                        .toUsername(toUserName).build());
                log.info("已开启【" + remarkNameByGroupUserName + "】防撤回功能");
                break;
            case "cpundo":
                to = ContactsTools.getContactDisplayNameByUserName(toUserName);
                build = Status.builder().name(to)
                        .undoStatus((short) 2).build();
                statusMapper.insertOrUpdateSelectiveForSqlite(build);
                //群消息
                nonPreventUndoMsgUserName.add(to);
                RoomChatContainer.get(toUserName).getChatPanel().getMessageEditorPanel().setUndoAndAutoLabel();

                messages.add(Message.builder().msgType(WXSendMsgCodeEnum.TEXT.getCode())
                        .content("已关闭【" + remarkNameByGroupUserName + "】防撤回功能")
                        .toUsername(toUserName).build());
                log.info("已关闭【" + remarkNameByGroupUserName + "】防撤回功能");
                break;
            case "ggr":
                if (msg.isGroupMsg()) {
                    String imgPath = chartUtil.makeGroupMemberAttrPieChart(toUserName, remarkNameByGroupUserName, "Sex", 1920, 1080);
                    //群消息
                    messages.add(MessageTools.toPicMessage(imgPath, toUserName));
                    log.info("计算群【" + remarkNameByGroupUserName + "】成员性别分布图");
                }

                break;
            case "gpr":
                if (msg.isGroupMsg()) {
                    String imgPath = chartUtil.makeGroupMemberAttrPieChart(toUserName, remarkNameByGroupUserName, "Province", 1920, 1080);
                    //群消息
                    messages.add(MessageTools.toPicMessage(imgPath, toUserName));
                    log.info("计算群【" + remarkNameByGroupUserName + "】成员省份分布图");
                }
                break;
            case "gmt10":
                break;
            case "pmt10":
                break;
            case "gma10":
                //群成员活跃度排名
                if (msg.isGroupMsg()) {
                    String imgPath = chartUtil.makeWXMemberOfGroupActivity(toUserName);
                    messages.add(MessageTools.toPicMessage(imgPath, toUserName));
                    log.info("计算【" + remarkNameByGroupUserName + "】成员活跃度");
                }
                break;
            case "mf10":
                //聊天词语频率排名
                List<String> imgs = chartUtil.makeWXUserMessageTop(toUserName);
                for (String s : imgs) {
                    //群消息
                    messages.add(MessageTools.toPicMessage(s, toUserName));
                }

                log.info("计算【" + remarkNameByGroupUserName + "】聊天类型及关键词");
                break;
            case "不要问了":
            case "不要问我":
                if (msg.getFromUserName().equals(Core.getUserName())) {
                    messages.add(Message.builder().msgType(WXSendMsgCodeEnum.VOICE.getCode())
                            .filePath("D:/weixin/MSGTYPE_VOICE/dont_ask.mp3")
                            .toUsername(toUserName).build());
                }
                break;
            default:
                break;


        }
        //延迟撤回消息，text:1  延迟一秒
        if (msg.getFromUserName().equals(Core.getUserName())) {
            try {
                String replace = msg.getPlainText();
                int i = replace.indexOf("&amp;");
                if (i != -1) {
                    long sleep = Long.parseLong(replace.substring(i + 5));
                    final long relay = sleep == 0 ? 2 * 60 * 1000 : sleep * 1000;
                    ExecutorServiceUtil.getGlobalExecutorService().execute(() -> {
                        SleepUtils.sleep(relay);
                        MessageTools.sendRevokeMsgByUserId(msg.getToUserName(), msg.getMsgId(), msg.getNewMsgId() + "");
                    });
                }
            } catch (Exception e) {

            }

        }
        if (text.startsWith("attr_rate") && msg.isGroupMsg()) {
            String substring = msg.getPlainText().substring(msg.getPlainText().indexOf(":") + 1);
            String imgPath = chartUtil.makeGroupMemberAttrPieChart(toUserName, remarkNameByGroupUserName, substring, 1920, 1080);
            //群消息
            messages.add(MessageTools.toPicMessage(imgPath, toUserName));
            log.info("计算群【" + remarkNameByGroupUserName + "】成员" + substring + "比例");
        }
        return messages;
    }

    @Override
    public List<Message> textMsgHandle(AddMsgList msg) {

        String text = msg.getPlainText();

        //处理控制命令
        List<Message> messages = controlCommandHandler(msg);
        if (messages.size() > 0) {
            return messages;
        }
        try {
            //是否需要自动回复
            String to = ContactsTools.getContactDisplayNameByUserName(msg.getFromUserName());
            if (autoChatUserNameList.contains(to)) {
                messages = handleTuLingMsg(TuLingUtil.robotMsgTuling(text), msg);
            } else if (autoChatWithPersonal && !msg.isGroupMsg()) {
                messages = handleTuLingMsg(TuLingUtil.robotMsgTuling(text), msg);
            }
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
        return messages;
    }


    /**
     * 图片消息(non-Javadoc)
     *
     * @see
     */
    @Override
    public List<Message> picMsgHandle(AddMsgList msg) {

        return null;
    }

    /**
     * 语音消息(non-Javadoc)
     *
     * @see
     */
    @Override
    public List<Message> voiceMsgHandle(AddMsgList msg) {

        return null;
    }


    @Override
    public List<Message> videoMsgHandle(AddMsgList msg) {

        return null;
    }

    @Override
    public List<Message> undoMsgHandle(AddMsgList msg) {
        String to = ContactsTools.getContactDisplayNameByUserName(msg.getFromUserName());
        //======家人群不发送撤回消息====
        if (msg.getFromUserName().startsWith("@@")) {
            if ("❤汪家人❤".equals(to) || to.startsWith("三盟")) {
                log.error("重要群群，不发送撤回消息");
                return null;
            }
        }
        //不处理撤回消息的群
        if (nonPreventUndoMsgUserName.contains(to)) {
            return null;
        }
        /*============获取被撤回的消息id============*/
        Map<String, Object> map = msg.getContentMap();
        Object msgId = map.get("sysmsg.revokemsg.msgid");
        if (msgId == null) {
            log.error("撤回消息id is null。");
            return null;
        }

        //查询历史消息
        MessageExample messageExample = new MessageExample();
        MessageExample.Criteria criteria = messageExample.createCriteria();
        criteria.andMsgIdEqualTo(msgId.toString());
        List<Message> messages = messageMapper.selectByExample(messageExample);
        if (messages.isEmpty()) {
            log.error("未获取到历史消息。");
            return null;
        }
        Message oldMessage = messages.get(0);
        //设置撤回状态
        String roomId = msg.getFromUserName();
        if (roomId.equals(Core.getUserName())) {
            roomId = msg.getToUserName();
        }
        RoomChatContainer.get(roomId).getChatPanel().setRevokeStatus(oldMessage.getId());


        //==============是否为自己的消息
        String oldMsgFromUserName = oldMessage.getFromUsername();
        //自己的撤回消息不处理
        if (Core.getUserName().equals(oldMsgFromUserName)) {
            return null;
        }
        ArrayList<Message> results = new ArrayList<>();
        Message message = null;
        //撤回消息的用户的昵称
        String fromNickName = "";
        if (msg.isGroupMsg()) {
            fromNickName = ContactsTools.getMemberDisplayNameOfGroup(msg.getFromUserName(), msg.getMemberName());
        } else {
            fromNickName = ContactsTools.getContactNickNameByUserName(msg.getFromUserName());
        }

        String realMsgContent = oldMessage.getContent();
        String filePath = oldMessage.getFilePath();
        String createTime = oldMessage.getCreateTime().substring(10);
        switch (WXReceiveMsgCodeEnum.getByCode(oldMessage.getMsgType())) {
            case MSGTYPE_TEXT:
                message = Message.builder()
                        .content("【" + fromNickName + " " + createTime + " " + "】撤回的消息：" + realMsgContent)
                        .msgType(WXReceiveMsgCodeEnum.MSGTYPE_TEXT.getCode())
                        .toUsername(msg.getFromUserName())
                        .build();
                results.add(message);
                break;
            case MSGTYPE_IMAGE:
                message = Message.builder()
                        .content("【" + fromNickName + " " + createTime + " " + "】撤回的图片(发送中...)：")
                        .msgType(WXReceiveMsgCodeEnum.MSGTYPE_TEXT.getCode())
                        .toUsername(msg.getFromUserName())
                        .build();
                results.add(message);
                oldMessage.setId(UUID.randomUUID().toString().replace("-",""));
                oldMessage.setFromUsername(Core.getUserName());
                oldMessage.setToUsername(msg.getFromUserName());
                results.add(oldMessage);
                break;
            case MSGTYPE_EMOTICON:
                message = Message.builder()
                        .content("【" + fromNickName + " " + createTime + " " + "】撤回的表情：")
                        .msgType(WXReceiveMsgCodeEnum.MSGTYPE_TEXT.getCode())
                        .toUsername(msg.getFromUserName())
                        .build();
                results.add(message);
                oldMessage.setId(UUID.randomUUID().toString().replace("-",""));
                oldMessage.setFromUsername(Core.getUserName());
                oldMessage.setToUsername(msg.getFromUserName());
                results.add(oldMessage);
                break;
            case MSGTYPE_VOICE:
                message = Message.builder()
                        .content("【" + fromNickName + " " + createTime + " " + "】撤回的语音(发送中...)：")
                        .msgType(WXReceiveMsgCodeEnum.MSGTYPE_TEXT.getCode())
                        .toUsername(msg.getFromUserName())
                        .build();
                results.add(message);
                oldMessage.setId(UUID.randomUUID().toString().replace("-",""));
                oldMessage.setFromUsername(Core.getUserName());
                oldMessage.setContent("");
                oldMessage.setToUsername(msg.getFromUserName());
                results.add(oldMessage);
                break;
            case MSGTYPE_VIDEO:
                message = Message.builder()
                        .content("【" + fromNickName + " " + createTime + " " + "】撤回的视频(发送中...)：")
                        .msgType(WXReceiveMsgCodeEnum.MSGTYPE_TEXT.getCode())
                        .toUsername(msg.getFromUserName())
                        .build();
                results.add(message);
                oldMessage.setId(UUID.randomUUID().toString().replace("-",""));
                oldMessage.setFromUsername(Core.getUserName());
                oldMessage.setToUsername(msg.getFromUserName());
                results.add(oldMessage);
                break;
            case MSGTYPE_MAP:
                //TODO 地图消息可直接发送
                String msgJson = oldMessage.getMsgJson();
                AddMsgList addMsgList = JSON.parseObject(msgJson, AddMsgList.class);
                String oriContent = addMsgList.getOriContent();
                Map<String, Object> stringObjectMap = XmlStreamUtil.toMap(oriContent);
                Object label = stringObjectMap.get("msg.location.attr.label");
                Object poiname = stringObjectMap.get("msg.location.attr.poiname");
                message = Message.builder()
                        .content("【" + fromNickName + " " + createTime + " " + "】撤回的定位：" + label + "(" + poiname + ")")
                        .msgType(WXReceiveMsgCodeEnum.MSGTYPE_TEXT.getCode())
                        .toUsername(msg.getFromUserName())
                        .build();
                results.add(message);
                message = Message.builder()
                        .content(oldMessage.getOriContent())
                        .toUsername(msg.getFromUserName())
                        .msgType(WXReceiveMsgCodeEnum.MSGTYPE_MAP.getCode())
                        .build();
                results.add(message);
                break;
            case MSGTYPE_SHARECARD:
                message = Message.builder()
                        .content("【" + fromNickName + " " + createTime + " " + "】撤回的联系人名片：")
                        .msgType(WXReceiveMsgCodeEnum.MSGTYPE_TEXT.getCode())
                        .toUsername(msg.getFromUserName())
                        .build();
                results.add(message);
                message = Message.builder()
                        .content(oldMessage.getContent())
                        .msgType(WXReceiveMsgCodeEnum.MSGTYPE_SHARECARD.getCode())
                        .toUsername(msg.getFromUserName())
                        .build();
                results.add(message);
                break;
            case MSGTYPE_APP:
                switch (WXReceiveMsgCodeOfAppEnum.getByCode(oldMessage.getAppMsgType())) {
                    case OTHER:
                        break;
                    case LINK:
                        Map<String, Object> mapA = XmlStreamUtil.toMap(XmlStreamUtil.formatXml(realMsgContent));
                        Object title = mapA.get("msg.appmsg.title");
                        Object url = mapA.get("msg.appmsg.url");
                        message = Message.builder()

                                .content("【" + fromNickName + " " + createTime + " " + "】撤回的收藏消息：" + title + "," + url)
                                .msgType(WXReceiveMsgCodeEnum.MSGTYPE_TEXT.getCode())
                                .toUsername(msg.getFromUserName())
                                .build();
                        results.add(message);
                        break;

                    case PROGRAM:
                        message = Message.builder()
                                .content("【" + fromNickName + " " + createTime + " " + "】撤回的小程序：" + realMsgContent)
                                .msgType(WXReceiveMsgCodeEnum.MSGTYPE_TEXT.getCode())
                                .toUsername(msg.getFromUserName())
                                .build();
                        results.add(message);
                        break;
                    case FILE:
                    default:
                        //目前是文件消息
                        message = Message.builder()
                                .content("【" + fromNickName + " " + createTime + " " + "】撤回的APP消息(发送中...)：")
                                .msgType(WXReceiveMsgCodeEnum.MSGTYPE_TEXT.getCode())
                                .toUsername(msg.getFromUserName())
                                .build();
                        results.add(message);
                        oldMessage.setId(UUID.randomUUID().toString().replace("-",""));
                        oldMessage.setFromUsername(Core.getUserName());
                        oldMessage.setToUsername(msg.getFromUserName());
                        results.add(oldMessage);
                        break;
                }
                break;
                default:
                    break;

        }
        return results;
    }

    @Override
    public List<Message> addFriendMsgHandle(AddMsgList msg) {
        log.info(LogUtil.printFromMeg(msg, WXReceiveMsgCodeEnum.MSGTYPE_VERIFYMSG.getCode()));
        //自动同意
  /*      MessageTools.addFriend(msg, true);
        String content = msg.getContent();
        Map<String, Object> stringObjectMap = XmlStreamUtil.toMap(content);
        Object o = stringObjectMap.get("msg.attr.content");
        if (o == null || StringUtils.isEmpty(o.toString())) {
            content = "添加你为好友";
        } else {
            content = o.toString();
        }
        AddMsgList addMsgList = new AddMsgList();
        addMsgList.setFromUserName(msg.getRecommendInfo().getUserName());

        addMsgList.setToUserName(Core.getUserName());
        addMsgList.setContent(content);
        addMsgList.setMsgType(WXReceiveMsgCodeEnum.MSGTYPE_SYS.getCode());
        msgCenter.handleNewMsg(addMsgList);*/

        return null;
    }

    @Override
    public List<Message> systemMsgHandle(AddMsgList msg) {
        return null;
    }

    @Override
    public List<Message> emotionMsgHandle(AddMsgList msg) {
        return null;
    }

    @Override
    public List<Message> appMsgHandle(AddMsgList msg) {
        switch (WXReceiveMsgCodeOfAppEnum.getByCode(msg.getAppMsgType())) {
            case OTHER:
                break;
            case LINK:
                break;
            case FILE:
                break;
            case PROGRAM:
                break;
        }
        return null;
    }

    @Override
    public List<Message> verifyAddFriendMsgHandle(AddMsgList msg) {
        return null;
    }

    @Override
    public List<Message> mapMsgHandle(AddMsgList msg) {
        return null;
    }


    @Override
    public List<Message> nameCardMsgHandle(AddMsgList msg) {

        return null;
    }

    /**
     * 处理图灵消息
     *
     * @param tl
     * @return
     */
    private List<Message> handleTuLingMsg(TuLingResponseBean tl, AddMsgList msg) {
        ArrayList<Message> msgMessages = new ArrayList<>();
        List<Results> results = tl.getResults();
        for (Results result : results) {
            String msgStr = result.getValues().getText();
            if (msg.isGroupMsg() && msg.getMentionMeUserNickName() != null) {
                msgStr = "@" + msg.getMentionMeUserNickName() + " " + msgStr;
            }
            Message.MessageBuilder msgBuilder = Message.builder()
                    .toUsername(msg.getFromUserName())
                    .content(msgStr);
            switch (ResultType.getByCode(result.getResultType())) {
                case URL:
                case NEWS:
                case TEXT:
                    msgBuilder.msgType(WXSendMsgCodeEnum.TEXT.getCode());
                    msgBuilder.content(msgStr + "【自动回复】");
                    break;
                case IMAGE:
                    msgBuilder.msgType(WXSendMsgCodeEnum.PIC.getCode());
                    break;
                case VIDEO:
                    msgBuilder.msgType(WXSendMsgCodeEnum.VIDEO.getCode());
                    break;
                case VOICE:
                    msgBuilder.msgType(WXSendMsgCodeEnum.VOICE.getCode());
                    break;
                case DEFAULT:
            }
            msgMessages.add(msgBuilder.build());
        }
        return msgMessages;
    }

}
