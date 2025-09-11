package cn.shu.wechat.service;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.constant.WxReqParamsConstant;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.dto.response.msg.send.WebWXSendMsgResponse;
import cn.shu.wechat.entity.Message;
import cn.shu.wechat.entity.Status;
import cn.shu.wechat.mapper.StatusMapper;
import cn.shu.wechat.service.impl.IMsgHandlerFaceImpl;
import cn.shu.wechat.swing.panels.chat.ChatPanelContainer;
import cn.shu.wechat.utils.ChartUtil;
import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.guardrail.GuardrailResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class FunctionCallingService{
    private static final ThreadLocal<String> contextToUserName = new ThreadLocal<>();
    public static final ThreadLocal<Boolean> invoke = new ThreadLocal<>();
    @Resource
    private ChartUtil chartUtil;

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



    @Tool(
            name = "get_group_gender_ratio",
            value = "Get the male-to-female ratio of members in a WeChat group. Only call this when the user explicitly asks for the gender ratio."
    )

    public Result<String> getGenderRatio(@MemoryId String sessionId,String groupId) {
        invoke.set(true);
        String toUserName = contextToUserName.get();
        if (!ContactsTools.isRoomContact(toUserName)) {
            return Result.<String>builder().content("当前不是群聊！").build();
        }
        Optional<String> pathOptional = chartUtil.makeContactsAttrPieChartAsPng(toUserName, "sex", 960, 540);
        if (pathOptional.isPresent()) {
            //群消息
            WebWXSendMsgResponse webWXSendMsgResponse = MessageTools.sendMsgByUserId(MessageTools.toPicMessage(pathOptional.get(), toUserName));
            if (webWXSendMsgResponse.getBaseResponse().getRet() != 0) {
                return Result.<String>builder().content("生成比例图失败!").build();
            }
        }
        return Result.<String>builder().content("已生成群聊男女比例图。").build() ;
    }

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
    @Tool(name = "disable_chat_auto_reply", value = "关闭指定群聊或好友的自动回复功能")
    public Result<String> disableChatAutoReply(@MemoryId String sessionId) {
        String toUserName = contextToUserName.get();
        String to = ContactsTools.getContactDisplayNameByUserName(toUserName);
//        Status build = Status.builder().name(to).autoStatus((short) 2).build();
//        statusMapper.insertOrUpdateSelectiveForSqlite(build);
        return Result.<String>builder().content("已关闭【" + to + "】自动回复功能").build();
    }


    @Tool(name = "enable_global_auto_reply", value = "开启全局个人用户自动回复功能")
    public Result<String> enableGlobalAutoReply(@MemoryId String sessionId) {
//        String toUserName = contextToUserName.get();
//        Status build = Status.builder().name(toUserName).autoStatus((short) 1).build();
//        statusMapper.insertOrUpdateSelectiveForSqlite(build);
        return Result.<String>builder().content("已开启全局个人用户自动回复功能").build();
    }

    @Tool(name = "disable_global_auto_reply", value = "关闭全局个人用户自动回复功能")
    public Result<String> disableGlobalAutoReply(@MemoryId String sessionId) {
//        String toUserName = contextToUserName.get();
//        Status build = Status.builder().name(toUserName).autoStatus((short) 2).build();
//        statusMapper.insertOrUpdateSelectiveForSqlite(build);
        return Result.<String>builder().content("已关闭全局个人用户自动回复功能").build();
    }



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

    @Tool(name = "generate_province_distribution", value = "生成群成员省份分布图")
    public Result<String> generateProvinceDistribution(@MemoryId String sessionId) {
        String toUserName = contextToUserName.get();
        Optional<String> pathOptional = chartUtil.makeContactsAttrPieChartAsPng(toUserName, "province", 1920, 1080);
        pathOptional.ifPresent(string -> MessageTools.sendMsgByUserId(MessageTools.toPicMessage(string, toUserName)));
        return Result.<String>builder().content(pathOptional.map(path -> "生成省份分布图成功，路径：" + path)
                .orElse("生成省份分布图失败")).build();
    }

    @Tool(name = "generate_activity_top10", value = "生成群成员或个人消息活跃度图表")
    public Result<String> generateActivityTop10(@MemoryId String sessionId, boolean isGroupMsg) {
        String toUserName = contextToUserName.get();
        String imgPath = isGroupMsg ?
                chartUtil.makeWXMemberOfGroupActivityFile(toUserName) :
                chartUtil.makeWXUserActivityFile(toUserName);
        MessageTools.sendMsgByUserId(MessageTools.toPicMessage(imgPath, toUserName));
        return Result.<String>builder().content("已生成活跃度统计图，路径：" + imgPath).build();
    }

    @Tool(name = "generate_keyword_top10", value = "生成聊天关键词图表")
    public Result<String> generateKeywordTop10(@MemoryId String sessionId) {
        String toUserName = contextToUserName.get();
        String imgPath = chartUtil.makeWXGroupMessageTopFile(toUserName);
        MessageTools.sendMsgByUserId(MessageTools.toPicMessage(imgPath, toUserName));
        return Result.<String>builder().content("已生成关键词TOP10图表，路径：" + imgPath).build();
    }

    @Tool(name = "generate_message_type_top10", value = "生成聊天消息类型图表")
    public Result<String> generateMessageTypeTop10(@MemoryId String sessionId) {
        String toUserName = contextToUserName.get();
        String imgPath = chartUtil.makeWXGroupMessageTypeTopFile(toUserName);

        MessageTools.sendMsgByUserId(MessageTools.toPicMessage(imgPath, toUserName));
        return Result.<String>builder().content("已生成消息类型TOP10图表，路径：" + imgPath).build();
    }

    @Tool(name = "generate_update_info", value = "生成好友属性更新频率图表")
    public Result<String> generateUpdateInfo(@MemoryId String sessionId) {
        String toUserName = contextToUserName.get();
        List<String> imgs = chartUtil.makeWXContactUpdateAttrBarChart();
        for (String img : imgs) {

            MessageTools.sendMsgByUserId(MessageTools.toPicMessage(img, toUserName));
        }
        return Result.<String>builder().content("已生成好友属性更新频率图表，路径：" + String.join(",", imgs)).build();
    }

    @Tool(name = "send_dont_ask_voice", value = "如果发送人是自己，则发送语音消息 '不要问了'")
    public Result<String> sendDontAskVoice(@MemoryId String sessionId, String fromUserName) {
        String toUserName = contextToUserName.get();
        MessageTools.sendMsgByUserId(Message.builder().msgType(WxReqParamsConstant.WXSendMsgCodeEnum.VOICE.getCode())
                .filePath("D:/weixin/MSGTYPE_VOICE/dont_ask.mp3")
                .toUsername(toUserName).build());
        return Result.<String>builder().content("").build();
    }

}
