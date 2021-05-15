package cn.shu.wechat.face;

import java.io.*;
import java.util.*;

import cn.shu.bean.tuling.enums.ResultType;
import cn.shu.bean.tuling.response.Results;
import cn.shu.bean.tuling.response.TuLingResponseBean;
import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.beans.msg.sync.AddMsgList;
import cn.shu.wechat.beans.pojo.Message;
import cn.shu.wechat.beans.pojo.MessageExample;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.enums.WXReceiveMsgCodeEnum;
import cn.shu.wechat.mapper.MessageMapper;
import cn.shu.wechat.utils.*;
import cn.shu.wechat.enums.WXReceiveMsgCodeOfAppEnum;
import cn.shu.wechat.enums.WXSendMsgCodeEnum;

import com.alibaba.fastjson.JSON;
import lombok.extern.log4j.Log4j2;
import net.sf.json.JSONException;

import org.springframework.stereotype.Component;
import cn.shu.utils.DateUtil;
import cn.shu.utils.TuLingUtil;

import javax.annotation.Resource;


@Log4j2
@Component
public class IMsgHandlerFaceImpl implements IMsgHandlerFace {

    @Resource
    private MessageMapper messageMapper;
    /**
     * autoChatUserNameList 包含 发送者：自动回复
     * 不包含：autoChatWithPersonal = true ：自动回复，false ：不回复
     */
    private boolean autoChatWithPersonal = false;

    /**
     * 自动聊天联系人列表，包括个人、群...
     */
    private final Set<String> autoChatUserNameList = new HashSet<>();


    @Resource
    private ChartUtil chartUtil;


    /**
     * 已关闭防撤回联系人列表
     */
    private final Set<String> nonPreventUndoMsgUserName = new HashSet<>();


    /**
     * 消息控制命令
     *
     * @param msg 消息
     * @return 回复消息
     */
    private List<MessageTools.Result> controlCommandHandler(AddMsgList msg) {
        String text = msg.getText().toLowerCase();
        List<MessageTools.Result> results = new ArrayList<>();

        //=========================手动发送消息=====================
        String[] split = msg.getText().split("：");
        if (split.length >= 2 && msg.getFromUserName().equals(Core.getUserName())) {
            try {
                long sleep = 100;
                try {
                    sleep = Long.parseLong(split[2]);
                } catch (ArrayIndexOutOfBoundsException e) {

                }
                String s = split[1];
                int i = Integer.parseInt(s);
                results.add(MessageTools.Result.builder()
                        .content("开始发送：" + i + "个" + split[0])
                        .toUserName(msg.getToUserName())
                        .replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                        .build());
                for (int j = 0; j < i; j++) {
                    results.add(MessageTools.Result.builder()
                            .content(split[0])
                            .replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                            .sleep(sleep)
                            .toUserName(msg.getToUserName())
                            .build());
                }
                return results;

            } catch (NumberFormatException e) {
            }
        }
        //============炸弹消息===================
        if (msg.getText().equals("[Bomb]") || msg.getText().equals("[炸弹]")) {
            String userName = Core.getUserSelf().getString("UserName");
            if (!msg.getFromUserName().equals(userName)) {
                for (int i = 0; i < 1; i++) {
                    results.add(MessageTools.Result.builder()
                            .content("[Bomb]")
                            .replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                            .sleep((long) (Math.random() * (10 - 1) + 1))
                            .build());
                }
                return results;
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
                    results.add(MessageTools.Result.builder()
                            .content("1、【oauto/cauto】\n\t开启/关闭群消息自动回复\n"
                                    + "2、【opundo/cpundo】\n\t开启/关闭群消息防撤回\n"
                                    + "3、【ggr】\n\t群成员性别比例图\n"
                                    + "4、【gpr】\n\t群成员省市分布图\n"
                                    // +"opundo/cpundo：群成员活跃度\n"
                                    + "5、【op/cp】\n\t开启/关闭全局个人用户消息自动回复\n"
                                    + "6、【gma10】\n\t群成员活跃度TOP10\n"
                                    + "7、【mf10】\n\t聊天消息关键词TOP10\n"
                            )
                            .toUserName(toUserName)
                            .replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                            .build());

                } else {
                    //个人消息
                    results.add(MessageTools.Result.builder()
                            .content("1、【oauto/cauto】\n\t开启/关闭当前联系人自动回复\n"
                                    + "2、【opundo/cpundo】\n\t开启/关闭当前联系人消息防撤回\n"
                                    + "3、【op/cp】\n\t开启/关闭全局个人用户消息自动回复\n"
                                    + "4、【mf10】\n\t聊天消息关键词TOP10\n")
                            .toUserName(toUserName)
                            .replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                            .build());
                }
                break;
            case "op":
                autoChatWithPersonal = true;
                results.add(MessageTools.Result.builder().replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                        .content("已开启全局个人用户自动回复功能")
                        .toUserName(toUserName).build());
                log.info("已开启全局个人用户自动回复功能");
                break;
            case "cp":
                autoChatWithPersonal = false;
                results.add(MessageTools.Result.builder().replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                        .content("已关闭全局个人用户自动回复功能")
                        .toUserName(toUserName).build());
                log.info("已关闭全局个人用户自动回复功能");
                break;
            case "oauto":
                autoChatUserNameList.add(toUserName);
                results.add(MessageTools.Result.builder().replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                        .content("已开启【" + remarkNameByGroupUserName + "】自动回复功能")
                        .toUserName(toUserName).build());
                log.info("已开启【" + remarkNameByGroupUserName + "】自动回复功能");
                break;
            case "cauto":
                autoChatUserNameList.remove(toUserName);
                results.add(MessageTools.Result.builder().replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                        .content("已关闭【" + remarkNameByGroupUserName + "】自动回复功能")
                        .toUserName(toUserName).build());
                log.info("已关闭【" + remarkNameByGroupUserName + "】自动回复功能");
                break;
            case "opundo":
                nonPreventUndoMsgUserName.remove(toUserName);
                results.add(MessageTools.Result.builder().replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                        .content("已开启【" + remarkNameByGroupUserName + "】防撤回功能")
                        .toUserName(toUserName).build());
                log.info("已开启【" + remarkNameByGroupUserName + "】防撤回功能");
                break;
            case "cpundo":
                //群消息
                nonPreventUndoMsgUserName.add(toUserName);
                results.add(MessageTools.Result.builder().replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                        .content("已关闭【" + remarkNameByGroupUserName + "】防撤回功能")
                        .toUserName(toUserName).build());
                log.info("已关闭【" + remarkNameByGroupUserName + "】防撤回功能");
                break;
            case "ggr":
                if (msg.isGroupMsg()) {
                    String imgPath = chartUtil.makeGroupMemberAttrPieChart(toUserName, remarkNameByGroupUserName, "Sex", 500, 400);
                    //群消息
                    results.add(MessageTools.Result.builder().replyMsgTypeEnum(WXSendMsgCodeEnum.PIC)
                            .filePath(imgPath)
                            .toUserName(toUserName).build());
                    log.info("计算群【" + remarkNameByGroupUserName + "】成员性别分布图");
                }

                break;
            case "gpr":
                if (msg.isGroupMsg()) {
                    String imgPath = chartUtil.makeGroupMemberAttrPieChart(toUserName, remarkNameByGroupUserName, "Province", 500, 400);
                    //群消息
                    results.add(MessageTools.Result.builder().replyMsgTypeEnum(WXSendMsgCodeEnum.PIC)
                            .filePath(imgPath)
                            .toUserName(toUserName).build());
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
                    String s1 = chartUtil.makeWXMemberOfGroupActivity(toUserName);
                    results.add(MessageTools.Result.builder().replyMsgTypeEnum(WXSendMsgCodeEnum.PIC)

                            .filePath(s1)
                            .toUserName(toUserName).build());
                    log.info("计算【" + remarkNameByGroupUserName + "】成员活跃度");
                }
                break;
            case "mf10":
                //聊天词语频率排名
                List<String> imgs = chartUtil.makeWXUserMessageTop(toUserName);
                for (String s : imgs) {
                    //群消息
                    results.add(MessageTools.Result.builder().replyMsgTypeEnum(WXSendMsgCodeEnum.PIC)
                            .filePath(s)
                            .toUserName(toUserName).build());
                }

                log.info("计算【" + remarkNameByGroupUserName + "】聊天类型及关键词");
                break;
            case "不要问了":
            case "不要问我":
                if (msg.getFromUserName().equals(Core.getUserName())) {
                    results.add(MessageTools.Result.builder().replyMsgTypeEnum(WXSendMsgCodeEnum.VOICE)
                            .filePath("D:/weixin/MSGTYPE_VOICE/dont_ask.mp3")
                            .toUserName(toUserName).build());
                }
                break;
            default:
                break;


        }
        //延迟撤回消息，text:1  延迟一秒
        if (msg.getFromUserName().equals(Core.getUserName())) {
            try {
                String replace = msg.getText();
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
            String substring = msg.getText().substring(msg.getText().indexOf(":") + 1);
            String imgPath = chartUtil.makeGroupMemberAttrPieChart(toUserName, remarkNameByGroupUserName, substring, 500, 400);
            //群消息
            results.add(MessageTools.Result.builder().replyMsgTypeEnum(WXSendMsgCodeEnum.PIC)
                    .filePath(imgPath)
                    .toUserName(toUserName).build());
            log.info("计算群【" + remarkNameByGroupUserName + "】成员" + substring + "比例");
        }
        return results;
    }

    @Override
    public List<MessageTools.Result> textMsgHandle(AddMsgList msg) {

        String text = msg.getText();

        //处理控制命令
        List<MessageTools.Result> results = controlCommandHandler(msg);
        if (results.size() > 0) {
            return results;
        }
        try {
            //是否需要自动回复
            if (autoChatUserNameList.contains(msg.getFromUserName())) {
                results = handleTuLingMsg(TuLingUtil.robotMsgTuling(text), msg);
            } else if (autoChatWithPersonal && !msg.isGroupMsg()) {
                results = handleTuLingMsg(TuLingUtil.robotMsgTuling(text), msg);
            }
        } catch (JSONException | NullPointerException | IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * 图片消息(non-Javadoc)
     *
     * @see
     */
    @Override
    public List<MessageTools.Result> picMsgHandle(AddMsgList msg) {


        return null;
    }

    /**
     * 语音消息(non-Javadoc)
     *
     * @see
     */
    @Override
    public List<MessageTools.Result> voiceMsgHandle(AddMsgList msg) {

        return null;
    }


    @Override
    public List<MessageTools.Result> videoMsgHandle(AddMsgList msg) {

        return null;
    }

    @Override
    public List<MessageTools.Result> undoMsgHandle(AddMsgList msg) {
		/* 撤回消息格式
		#1108768584572118898为被撤回消息ID
		<sysmsg type="revokemsg">
		<revokemsg>
			<session>wxid_lolnmwoj459722</session>
			<oldmsgid>1700173602</oldmsgid>
			<msgid>1108768584572118898</msgid>
			<replacemsg>
				<![CDATA["hello" recalled a oldMessage]]>
			</replacemsg>
		</revokemsg>
		</sysmsg>
		*/
        /*============获取被撤回的消息============*/
        String content = XmlStreamUtil.formatXml(msg.getContent());
        content = "<root>" + content + "</root>";
        Map<String, Object> map = XmlStreamUtil.toMap(content);
        Object msgid = map.get("root.sysmsg.revokemsg.msgid");
        if (msgid == null) {
            return null;
        }
        //查询历史消息
        MessageExample messageExample = new MessageExample();
        MessageExample.Criteria criteria = messageExample.createCriteria();
        criteria.andMsgIdEqualTo(msgid.toString());
        List<Message> messages = messageMapper.selectByExample(messageExample);

        /* String value = PropertyUtil.loadMsg(msgid.toString());*/
        if (messages.isEmpty()) {
            return null;
        }
        Message oldMessage = messages.get(0);
        //======家人群不发送撤回消息====
        if (msg.getFromUserName().startsWith("@@")) {
            String to = ContactsTools.getContactDisplayNameByUserName(msg.getFromUserName());
            if ("❤汪家人❤".equals(to)
                    || "弹性大数据KZK2101".equals(to)
                    || "销秘科技".equals(to)
                    || "艾视医疗集团总群".equals(to)) {
                log.error("重要群群，不发送撤回消息");
                return null;
            }
            //不处理撤回消息的群
            if (nonPreventUndoMsgUserName.contains(msg.getFromUserName())) {
                return null;
            }
        }

        //==============是否为自己的消息
        String oldMsgFromUserName = oldMessage.getFromUsername();
        //自己的撤回消息不处理
        if (Core.getUserName().equals(oldMsgFromUserName)) {
            return null;
        }
        //===============
        ArrayList<MessageTools.Result> results = new ArrayList<>();
        MessageTools.Result result = null;
        //撤回消息的用户的昵称
        String fromNickName = "";
        if (msg.isGroupMsg()) {
            fromNickName = ContactsTools.getMemberDisplayNameOfGroup(msg.getFromUserName(), msg.getMemberName());
        } else {
            fromNickName = ContactsTools.getContactNickNameByUserName(msg.getFromUserName());
        }

        String realMsgContent = oldMessage.getContent();
        String filePath = oldMessage.getFilePath();
        String createTime = DateUtil.format(oldMessage.getCreateTime());
        switch (WXReceiveMsgCodeEnum.getByCode(oldMessage.getMsgType())) {
            case MSGTYPE_TEXT:
                result = MessageTools.Result.builder()
                        .content("【" + fromNickName + " " + createTime + " " + "】撤回的消息：" + realMsgContent)
                        .replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                        .build();
                results.add(result);
                break;
            case MSGTYPE_IMAGE:
                result = MessageTools.Result.builder()
                        .content("【" + fromNickName + " " + createTime + " " + "】撤回的图片(发送中...)：")
                        .replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                        .build();
                results.add(result);
                result = MessageTools.Result.builder()
                        .filePath(filePath)
                        .content(realMsgContent)
                        .replyMsgTypeEnum(WXSendMsgCodeEnum.PIC)
                        .build();
                results.add(result);
                break;
            case MSGTYPE_EMOTICON:
                result = MessageTools.Result.builder()
                        .content("【" + fromNickName + " " + createTime + " " + "】撤回的表情：")
                        .replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                        .build();
                results.add(result);
                result = MessageTools.Result.builder()
                        .filePath(filePath)
                        .content(realMsgContent)
                        .replyMsgTypeEnum(WXSendMsgCodeEnum.EMOTION)
                        .build();
                results.add(result);
                break;
            case MSGTYPE_VOICE:
                result = MessageTools.Result.builder()
                        .content("【" + fromNickName + " " + createTime + " " + "】撤回的语音(发送中...)：")
                        .replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                        .build();
                results.add(result);
                result = MessageTools.Result.builder()
                        .filePath(filePath)
                        .content(realMsgContent)
                        .replyMsgTypeEnum(WXSendMsgCodeEnum.VOICE)
                        .build();
                results.add(result);
                break;
            case MSGTYPE_VIDEO:
                result = MessageTools.Result.builder()
                        .content("【" + fromNickName + " " + createTime + " " + "】撤回的视频(发送中...)：")
                        .replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                        .build();
                results.add(result);
                result = MessageTools.Result.builder()
                        .filePath(filePath)
                        .content(realMsgContent)
                        .replyMsgTypeEnum(WXSendMsgCodeEnum.VIDEO)
                        .build();
                results.add(result);
                break;
            case MSGTYPE_MAP:
                String msgJson = oldMessage.getMsgJson();
                AddMsgList addMsgList = JSON.parseObject(msgJson, AddMsgList.class);
                String oriContent = addMsgList.getOriContent();
                Map<String, Object> stringObjectMap = XmlStreamUtil.toMap(oriContent);
                Object label = stringObjectMap.get("msg.location.attr.label");
                Object poiname = stringObjectMap.get("msg.location.attr.poiname");
                result = MessageTools.Result.builder()
                        .content("【" + fromNickName + " " + createTime + " " + "】撤回的定位：" + label + "(" + poiname + ")")
                        .replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                        .build();
                results.add(result);
                result = MessageTools.Result.builder()
                        .filePath(filePath)
                        .replyMsgTypeEnum(WXSendMsgCodeEnum.PIC)
                        .build();
                results.add(result);
                break;
            case MSGTYPE_SHARECARD:
                result = MessageTools.Result.builder()
                        .content("【" + fromNickName + " " + createTime + " " + "】撤回的联系人名片：")
                        .replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                        .build();
                results.add(result);
                result = MessageTools.Result.builder()
                        /*"【" + fromNickName + "】撤回的联系人名片：" +*/
                        .content(realMsgContent)
                        .replyMsgTypeEnum(WXSendMsgCodeEnum.CARD)
                        .build();
                results.add(result);
                break;
            case MSGTYPE_APP:
                switch (WXReceiveMsgCodeOfAppEnum.getByCode(oldMessage.getAppMsgType())) {
                    case UNKNOWN:
                        break;
                    case FAVOURITE:
                        Map<String, Object> mapA = XmlStreamUtil.toMap(XmlStreamUtil.formatXml(realMsgContent));
                        Object title = mapA.get("msg.appmsg.title");
                        Object url = mapA.get("msg.appmsg.url");
                        result = MessageTools.Result.builder()

                                .content("【" + fromNickName + " " + createTime + " " + "】撤回的收藏消息：" + title + "," + url)
                                .replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                                .build();
                        results.add(result);
                        break;

                    case PROGRAM:
                        result = MessageTools.Result.builder()
                                .content("【" + fromNickName + " " + createTime + " " + "】撤回的小程序：" + realMsgContent)
                                .replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                                .build();
                        results.add(result);
                        break;
                    case FILE:
                    default:
                        //目前是文件消息
                        result = MessageTools.Result.builder()
                                .content("【" + fromNickName + " " + createTime + " " + "】撤回的APP消息(发送中...)：")
                                .replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                                .build();
                        results.add(result);
                        result = MessageTools.Result.builder()
                                .filePath(filePath)
                                .content(realMsgContent)
                                .replyMsgTypeEnum(WXSendMsgCodeEnum.APP)
                                .build();
                        results.add(result);
                        break;
                }
                break;

        }
        return results;
    }

    @Override
    public List<MessageTools.Result> addFriendMsgHandle(AddMsgList msg) {
        log.info(LogUtil.printFromMeg(msg, WXReceiveMsgCodeEnum.MSGTYPE_VERIFYMSG.getCode()));
        //自动同意
        MessageTools.addFriend(msg, true);
        return null;
    }

    @Override
    public List<MessageTools.Result> systemMsgHandle(AddMsgList msg) {
//       log.info(LogUtil.printFromMeg(msg, MsgTypeEnum.SYSTEM.getCode()));
        return null;
    }

    @Override
    public List<MessageTools.Result> emotionMsgHandle(AddMsgList msg) {

        return null;
    }

    @Override
    public List<MessageTools.Result> appMsgHandle(AddMsgList msg) {
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
        return null;
    }

    @Override
    public List<MessageTools.Result> verifyAddFriendMsgHandle(AddMsgList msg) {
        log.info(LogUtil.printFromMeg(msg, "VerifyAddFriendMsg"));
        return null;
    }

    @Override
    public List<MessageTools.Result> mapMsgHandle(AddMsgList msg) {
        return null;
    }


    @Override
    public List<MessageTools.Result> nameCardMsgHandle(AddMsgList msg) {

        return null;
    }

    /**
     * 处理图灵消息
     *
     * @param tl
     * @return
     */
    private List<MessageTools.Result> handleTuLingMsg(TuLingResponseBean tl, AddMsgList msg) {
        ArrayList<MessageTools.Result> msgResults = new ArrayList<>();
        List<Results> results = tl.getResults();
        for (Results result : results) {
            String msgStr = result.getValues().getText();
            if (msg.isGroupMsg() && msg.getMentionMeUserNickName() != null) {
                msgStr = "@" + msg.getMentionMeUserNickName() + " " + msgStr;
            }
            MessageTools.Result.ResultBuilder msgBuilder = MessageTools.Result.builder()
                    .content(msgStr);
            switch (ResultType.getByCode(result.getResultType())) {
                case URL:
                case NEWS:
                case TEXT:
                    msgBuilder.replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT);
                    msgBuilder.content(msgStr + "【自动回复】");
                    break;
                case IMAGE:
                    msgBuilder.replyMsgTypeEnum(WXSendMsgCodeEnum.PIC);
                    break;
                case VIDEO:
                    msgBuilder.replyMsgTypeEnum(WXSendMsgCodeEnum.VIDEO);
                    break;
                case VOICE:
                    msgBuilder.replyMsgTypeEnum(WXSendMsgCodeEnum.VOICE);
                    break;
                case DEFAULT:
            }
            msgResults.add(msgBuilder.build());
        }
        return msgResults;
    }

}
