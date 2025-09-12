package cn.shu.wechat.service;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.constant.MessageType;
import cn.shu.wechat.constant.WxReqParamsConstant;
import cn.shu.wechat.constant.WxRespConstant;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.entity.Message;
import cn.shu.wechat.entity.Status;
import cn.shu.wechat.mapper.MessageMapper;
import cn.shu.wechat.mapper.StatusMapper;
import cn.shu.wechat.service.impl.IMsgHandlerFaceImpl;
import cn.shu.wechat.swing.panels.chat.ChatPanelContainer;
import cn.shu.wechat.utils.ChartUtil;
import cn.shu.wechat.utils.SpringContextHolder;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Log4j2
@Component
public class FunctionCallingService{
    private static final ThreadLocal<String> contextToUserName = new ThreadLocal<>();
    public static final ThreadLocal<Boolean> invoke = new ThreadLocal<>();
    @Resource
    private ChartUtil chartUtil;

    @Resource
    private MessageMapper mapper;

    @Resource
    @Qualifier("qwenVLMaximageChatModel")
    private ChatModel qwenVLMaximageChatModel;

    private final Map<String, String> attributeMap = new HashMap<>();

    public FunctionCallingService(){
        attributeMap.put("性别", "sex");
        attributeMap.put("城市", "city");
        attributeMap.put("省份", "province");
        attributeMap.put("用户名", "username");
        attributeMap.put("昵称", "nickname");
        attributeMap.put("签名", "signature");
        attributeMap.put("备注名", "remarkname");
        attributeMap.put("群ID", "chatroomid");
        attributeMap.put("状态", "statues");
        attributeMap.put("拼音全拼", "pyquanpin");
        attributeMap.put("加密群ID", "encrychatroomid");
        attributeMap.put("显示名", "displayname");
        attributeMap.put("验证标志", "verifyflag");
        attributeMap.put("统一好友", "unifriend");
        attributeMap.put("联系人标志", "contactflag");
        attributeMap.put("成员列表", "memberlist");
        attributeMap.put("星标好友", "starfriend");
        attributeMap.put("头像URL", "headimgurl");
        attributeMap.put("应用账号标志", "appaccountflag");
        attributeMap.put("成员数", "membercount");
        attributeMap.put("备注首字母", "remarkpyinitial");
        attributeMap.put("社交标志", "snsflag");
        attributeMap.put("别名", "alias");
        attributeMap.put("关键词", "keyword");
        attributeMap.put("隐藏输入栏标志", "hideinputbarflag");
        attributeMap.put("备注拼音全拼", "remarkpyquanpin");
        attributeMap.put("用户ID", "uin");
        attributeMap.put("群主ID", "owneruin");
        attributeMap.put("是否群主", "isowner");
        attributeMap.put("拼音首字母", "pyinitial");
        attributeMap.put("票据", "ticket");
        attributeMap.put("是否互为好友", "mutualCreate");
        attributeMap.put("类型", "type");
        attributeMap.put("是否联系人", "iscontacts");
        attributeMap.put("头像图标", "avatarIcon");
        attributeMap.put("群名称", "groupName");
    }

    @Resource
    private StatusMapper statusMapper;

    public static void setUserName(String userName) {
        contextToUserName.set(userName);
    }

    @Tool(name = "help_command", value = "Display help information.")
    public Result<String> help(@MemoryId String sessionId) {
        String toUserName = contextToUserName.get();
        String content;
        if (ContactsTools.isRoomContact(toUserName)) {
            content = "【oauto/cauto】开启/关闭群消息自动回复(@我提问)\n"
                    + "【opundo/cpundo】开启/关闭群消息防撤回\n"
                    + "【ggr】群成员性别比例图\n"
                    + "【welo】开启新成员欢迎功能\n"
                    + "【welc】关闭新成员欢迎功能\n"
                    + "【gpr】群成员省市分布图\n"
                    + "【op/cp】开启/关闭全局个人用户消息自动回复\n"
                    + "【gma10】群成员活跃度TOP10\n"
                    + "【mf10】聊天消息关键词TOP10\n"
                    + "【mft10】聊天消息类型TOP10\n"
                    + "【{消息}&{时间(秒)}】延迟撤回\n";
        } else {
            content = "【oauto/cauto】开启/关闭当前联系人自动回复\n"
                    + "【opundo/cpundo】开启/关闭当前联系人消息防撤回\n"
                    + "【op/cp】开启/关闭全局个人用户消息自动回复\n"
                    + "【welo】开启新成员欢迎功能\n"
                    + "【welc】关闭新成员欢迎功能\n"
                    + "【mf10】聊天消息关键词TOP10\n"
                    + "【gma10】活跃度TOP\n"
                    + "【updateinfo】好友属性更新次数排行\n"
                    + "【mft10】聊天消息类型TOP10\n"
                    + "【{消息}&{时间(秒)}】延迟撤回\n";
        }
        return Result.<String>builder().content(content).build();
    }


//
//    @Tool(
//            name = "get_group_gender_ratio",
//            value = "Get the male-to-female ratio of members in a WeChat group. Only call this when the user explicitly asks for the gender ratio."
//    )

//    public Result<String> getGenderRatio(@MemoryId String sessionId,String groupId) {
//        invoke.set(true);
//        String toUserName = contextToUserName.get();
//        if (!ContactsTools.isRoomContact(toUserName)) {
//            return Result.<String>builder().content("当前不是群聊！").build();
//        }
//        Optional<String> pathOptional = chartUtil.makeContactsAttrPieChartAsPng(toUserName, "sex", 960, 540);
//        if (pathOptional.isPresent()) {
//            //群消息
//            WebWXSendMsgResponse webWXSendMsgResponse = MessageTools.sendMsgByUserId(MessageTools.toPicMessage(pathOptional.get(), toUserName));
//            if (webWXSendMsgResponse.getBaseResponse().getRet() != 0) {
//                return Result.<String>builder().content("生成比例图失败!").build();
//            }
//        }
//        return Result.<String>builder().content("已生成群聊男女比例图。").build() ;
//    }

    @Tool(
            name = "open_auto_reply",
            value = "Enable auto-reply in the group only when the user explicitly requests it."

    )
    public Result<String> openAutoReply(@MemoryId String sessionId) {
        invoke.set(true);
        String toUserName = contextToUserName.get();

        String to = ContactsTools.getContactDisplayNameByUserName(toUserName);
        IMsgHandlerFaceImpl.autoChatUserNameList.add(to);

        Status build = Status.builder()
                .name(to)
                .autoStatus((short) 1)
                .build();
        statusMapper.insertOrUpdateSelectiveForSqlite(build);

        if(ChatPanelContainer.getContext().isCurrentRoom(toUserName)) {
            ChatPanelContainer.get(toUserName)
                    .getChatMessagePanel()
                    .getChatMessageEditorPanel()
                    .setUndoAndAutoLabel();
        }

        return Result.<String>builder().content("已开启【" + to + "】自动回复功能").build();
    }
//    @Tool(name = "disable_chat_auto_reply", value = "关闭指定群聊或好友的自动回复功能")
//    public Result<String> disableChatAutoReply(@MemoryId String sessionId) {
//        String toUserName = contextToUserName.get();
//        String to = ContactsTools.getContactDisplayNameByUserName(toUserName);
////        Status build = Status.builder().name(to).autoStatus((short) 2).build();
////        statusMapper.insertOrUpdateSelectiveForSqlite(build);
//        return Result.<String>builder().content("已关闭【" + to + "】自动回复功能").build();
//    }


//    @Tool(name = "enable_global_auto_reply", value = "开启全局个人用户自动回复功能")
//    public Result<String> enableGlobalAutoReply(@MemoryId String sessionId) {
////        String toUserName = contextToUserName.get();
////        Status build = Status.builder().name(toUserName).autoStatus((short) 1).build();
////        statusMapper.insertOrUpdateSelectiveForSqlite(build);
//        return Result.<String>builder().content("已开启全局个人用户自动回复功能").build();
//    }
//
//    @Tool(name = "disable_global_auto_reply", value = "关闭全局个人用户自动回复功能")
//    public Result<String> disableGlobalAutoReply(@MemoryId String sessionId) {
////        String toUserName = contextToUserName.get();
////        Status build = Status.builder().name(toUserName).autoStatus((short) 2).build();
////        statusMapper.insertOrUpdateSelectiveForSqlite(build);
//        return Result.<String>builder().content("已关闭全局个人用户自动回复功能").build();
//    }



    @Tool(name = "enable_welcome", value = "开启新成员欢迎功能")
    public Result<String> enableWelcome(@MemoryId String sessionId) {
        String toUserName = contextToUserName.get();
        String to = ContactsTools.getContactDisplayNameByUserName(toUserName);
        Status build = Status.builder().name(to).key("welcome").value("true").build();
        statusMapper.insertOrUpdateSelectiveForSqlite(build);
        return Result.<String>builder().content("已开启【" + to + "】新成员欢迎功能").build();
    }

    @Tool(name = "disable_welcome", value = "关闭新成员欢迎功能")
    public Result<String> disableWelcome(@MemoryId String sessionId) {
        String toUserName = contextToUserName.get();
        String to = ContactsTools.getContactDisplayNameByUserName(toUserName);
        Status build = Status.builder().name(to).key("welcome").value("false").build();
        statusMapper.insertOrUpdateSelectiveForSqlite(build);
        return Result.<String>builder().content("已关闭【" + to + "】新成员欢迎功能").build();
    }

    @Tool(name = "enable_prevent_undo", value = "开启防撤回功能")
    public Result<String> enablePreventUndo(@MemoryId String sessionId) {
        String toUserName = contextToUserName.get();
        Status build = Status.builder().name(toUserName).undoStatus((short) 1).build();
        statusMapper.insertOrUpdateSelectiveForSqlite(build);
        return Result.<String>builder().content("已开启【" + toUserName + "】防撤回功能").build();
    }

    @Tool(name = "disable_prevent_undo", value = "关闭防撤回功能")
    public Result<String> disablePreventUndo(@MemoryId String sessionId) {
        String toUserName = contextToUserName.get();
        Status build = Status.builder().name(toUserName).undoStatus((short) 2).build();
        statusMapper.insertOrUpdateSelectiveForSqlite(build);
        return Result.<String>builder().content("已关闭【" + toUserName + "】防撤回功能").build();
    }

//    @Tool(name = "generate_province_distribution", value = "生成群成员省份分布图")
//    public Result<String> generateProvinceDistribution(@MemoryId String sessionId) {
//        String toUserName = contextToUserName.get();
//        Optional<String> pathOptional = chartUtil.makeContactsAttrPieChartAsPng(toUserName, "province", 1920, 1080);
//        pathOptional.ifPresent(string -> MessageTools.sendMsgByUserId(MessageTools.toPicMessage(string, toUserName)));
//        return Result.<String>builder().content(pathOptional.map(path -> "生成省份分布图成功，路径：" + path)
//                .orElse("生成省份分布图失败")).build();
//    }

    @Tool(name = "generate_activity_top10", value = "生成群成员或个人消息活跃度图表")
    public Result<String> generateActivityTop10(@MemoryId String sessionId, boolean isGroupMsg) {
        String toUserName = contextToUserName.get();
        String imgPath = isGroupMsg ?
                chartUtil.makeWXMemberOfGroupActivityFile(toUserName) :
                chartUtil.makeWXUserActivityFile(toUserName);
        MessageTools.sendMsgByUserId(MessageTools.toPicMessage(imgPath, toUserName));
        return Result.<String>builder().content("已生成活跃度统计图").build();
    }

    @Tool(name = "generate_keyword_top10", value = "生成聊天关键词图表")
    public Result<String> generateKeywordTop10(@MemoryId String sessionId) {
        String toUserName = contextToUserName.get();
        String imgPath = chartUtil.makeWXGroupMessageTopFile(toUserName);
        MessageTools.sendMsgByUserId(MessageTools.toPicMessage(imgPath, toUserName));
        return Result.<String>builder().content("已生成关键词TOP10图表").build();
    }

    @Tool(name = "generate_message_type_top10", value = "生成聊天消息类型图表")
    public Result<String> generateMessageTypeTop10(@MemoryId String sessionId) {
        String toUserName = contextToUserName.get();
        String imgPath = chartUtil.makeWXGroupMessageTypeTopFile(toUserName);

        MessageTools.sendMsgByUserId(MessageTools.toPicMessage(imgPath, toUserName));
        return Result.<String>builder().content("已生成消息类型TOP10图表").build();
    }

    @Tool(name = "generate_update_info", value = "生成好友属性更新频率图表")
    public Result<String> generateUpdateInfo(@MemoryId String sessionId) {
        String toUserName = contextToUserName.get();
        List<String> imgs = chartUtil.makeWXContactUpdateAttrBarChart();
        for (String img : imgs) {

            MessageTools.sendMsgByUserId(MessageTools.toPicMessage(img, toUserName));
        }
        return Result.<String>builder().content("已生成好友属性更新频率图表").build();
    }

    @Tool(name = "send_dont_ask_voice", value = "如果发送人是自己，则发送语音消息 '不要问了'")
    public Result<String> sendDontAskVoice(@MemoryId String sessionId, String fromUserName) {
        String toUserName = contextToUserName.get();
        MessageTools.sendMsgByUserId(Message.builder().msgType(WxReqParamsConstant.WXSendMsgCodeEnum.VOICE.getCode())
                .filePath("D:/weixin/MSGTYPE_VOICE/dont_ask.mp3")
                .toUsername(toUserName).build());
        return Result.<String>builder().content("").build();
    }


    @Tool(name = "generate_attribute_distribution", value = "生成群成员属性分布图，例如性别、城市、省份等")
    public Result<String> generateAttributeDistribution(
            @MemoryId String sessionId,
            @P("要统计的属性名称，例如 sex（性别）、city（城市）、province（省份）") String attribute) {
        // 属性映射表：自然语言 -> 数据库字段
        String attributeEn = attributeMap.get(attribute);
        if (attributeEn == null) {
            return Result.<String>builder()
                    .content("不支持的属性：" + attribute + "，支持的属性有：" + attributeMap.keySet())
                    .build();
        }
        Optional<String> pathOptional;
        String toUserName = contextToUserName.get();
        if (attributeEn.equals("sex")) {
            pathOptional = chartUtil.makeContactsAttrPieChartAsPng(toUserName, "sex", 960, 540);
        } else {
            pathOptional = chartUtil.makeContactsAttrPieChartAsPng(toUserName, attributeEn, 1920, 1080);

        }
        if (pathOptional.isPresent()) {
            String path = pathOptional.get();
            MessageTools.sendMsgByUserId(MessageTools.toPicMessage(path, toUserName));

            byte[] imageBytes = null;
            try {
                imageBytes = Files.readAllBytes(Path.of(path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Base64.Encoder encoder = Base64.getEncoder();
            Content content =  ImageContent.from(encoder.encodeToString(imageBytes), "image/png");
            UserMessage userMessage = UserMessage.from(content);
            ChatResponse chat = qwenVLMaximageChatModel.chat(userMessage);
            return Result.<String>builder()
                    .content(chat.aiMessage().text())
                    .build();
        }

        return Result.<String>builder()
                .content(pathOptional.map(path -> "生成【" + attributeEn + "】分布图成功" )
                        .orElse("生成【" + attributeEn + "】分布图失败"))
                .build();
    }

    @Tool(name = "extract_recent_activity_names",
            value = "提取最近 X 个活动的名称（例如登山、徒步、爬山活动或系统中记录的活动）")
    public Result<String> extractRecentActivityNames(
            @MemoryId String sessionId,
            @P("要提取的活动数量，例如 10") int count) {

        String toUserName = contextToUserName.get();
        Contacts contacts = Core.getMemberMap().get(toUserName);
        String remarkName = ContactsTools.getContactRemarkNameByUserName(contacts);
        String nickName = ContactsTools.getContactNickNameByUserName(contacts);
        java.util.List<Message> messageList = mapper.selectByPage(1, count, toUserName, remarkName, nickName, 3);
        Base64.Encoder encoder = Base64.getEncoder();
        List<Content> collect = messageList.stream().map(Message::getFilePath)
                .filter(Objects::nonNull)
                .map(e -> {
                    try {
                        return ImageContent.from(encoder.encodeToString(Files.readAllBytes(Path.of(e))), "image/gif");
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }).collect(Collectors.toList());
        collect.add(TextContent.from("提取图中的群聊名称。"));
        UserMessage userMessage = UserMessage.from(collect);

        ChatResponse chat = qwenVLMaximageChatModel.chat(userMessage);
        return Result.<String>builder()
                .content("最近活动的名称:" + chat.aiMessage().text())
                .build();
    }

    /**
     * 分析最近聊天记录
     */
    @Tool(
            name = "analyze_recent_chat",
            value = "分析最近 X 条聊天记录，识别图片内容，分析文本，提炼聊天主题、活动、核心论点。"
    )
    public Result<String> analyzeRecentChat(
            @MemoryId String sessionId,
            @P("要分析的聊天条数，例如 20") int count) {
        return safeRun(()->{


            String toUserName = contextToUserName.get();
            Contacts contacts = Core.getMemberMap().get(toUserName);

            String remarkName = ContactsTools.getContactRemarkNameByUserName(contacts);
            String nickName = ContactsTools.getContactNickNameByUserName(contacts);

            // 1. 查询最近聊天记录
            List<Message> messageList = mapper.selectByPage(1, count, toUserName, remarkName, nickName, null);

            // 2. 转换为多模态内容
            List<Content> contents = buildMessageContents(messageList);

            // 3. 给模型清晰的提示词，引导输出结构化总结
            contents.add(TextContent.from("""
                你是一名聊天记录分析助手，请阅读以下聊天内容（包含文字和图片），并完成以下任务：
                1. 总结主要的聊天主题（用一两句话描述）。
                2. 识别并提取聊天中涉及的活动（如登山、聚会、旅行、跑步等）。
                3. 提炼聊天的核心论点或关键信息（用一两句话描述）。
                4. 如果图片中含有活动或主题相关的信息，请结合文字一并分析（用一两句话描述）。
                输出时请用清晰的小标题分隔，例如：
                【主题】
                ...
                【活动】
                ...
                【核心论点】
                ...
                """));

            UserMessage userMessage = UserMessage.from(contents);

            ChatResponse chatResponse = qwenVLMaximageChatModel.chat(userMessage);

            // 5. 直接返回总结内容
            return Result.<String>builder()
                    .content(chatResponse.aiMessage().text())
                    .build();
        });
    }

    /**
     * 构建多模态内容：支持文本和图片
     */
    private List<Content> buildMessageContents(List<Message> messageList) {
        Base64.Encoder encoder = Base64.getEncoder();

        return messageList.stream()
                .map(e -> switch (WxRespConstant.WXReceiveMsgCodeEnum.getByCode(e.getMsgType())) {
                    case MSGTYPE_TEXT -> TextContent.from(e.getPlaintext());
                    case MSGTYPE_IMAGE -> {
                        try {
                            byte[] imageBytes = Files.readAllBytes(Path.of(e.getFilePath()));
                            yield ImageContent.from(encoder.encodeToString(imageBytes), "image/png");
                        } catch (IOException ex) {
                            yield null;
                        }
                    }
                    default -> null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private <T> Result<T> safeRun(Supplier<Result<T>> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return Result.<T>builder().content((T) ("工具调用失败: " + e.getMessage())).build();
        }
    }
}
