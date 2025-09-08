package cn.shu.wechat.service;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.constant.WxReqParamsConstant;
import cn.shu.wechat.dto.response.msg.send.WebWXSendMsgResponse;
import cn.shu.wechat.entity.Message;
import cn.shu.wechat.entity.Status;
import cn.shu.wechat.mapper.StatusMapper;
import cn.shu.wechat.service.impl.IMsgHandlerFaceImpl;
import cn.shu.wechat.swing.panels.chat.ChatPanelContainer;
import cn.shu.wechat.utils.ChartUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FunctionCallingService {
    private static final ThreadLocal<String> contextToUserName = new ThreadLocal<>();
    public static final ThreadLocal<Boolean> invoke = new ThreadLocal<>();
    @Resource
    private ChartUtil chartUtil;

    @Resource
    private StatusMapper statusMapper;

    public static void setUserName(String userName) {
        contextToUserName.set(userName);
    }

    @Tool(
            name = "get_group_gender_ratio",
            description = "Get the male-to-female ratio of members in a WeChat group. Only call this when the user explicitly asks for the gender ratio.",
            returnDirect = true
    )

    public String getGenderRatio(String groupId) {
        invoke.set(true);
        String toUserName = contextToUserName.get();
        if (!ContactsTools.isRoomContact(toUserName)) {
            return "当前不是群聊！";
        }
        Optional<String> pathOptional = chartUtil.makeContactsAttrPieChartAsPng(toUserName, "sex", 960, 540);
        if (pathOptional.isPresent()) {
            //群消息
            WebWXSendMsgResponse webWXSendMsgResponse = MessageTools.sendMsgByUserId(MessageTools.toPicMessage(pathOptional.get(), toUserName));
            if (webWXSendMsgResponse.getBaseResponse().getRet() != 0) {
                return "生成比例图失败!";
            }
        }
        return "已生成群聊男女比例图。";
    }

    @Tool(
            name = "open_auto_reply",
            description = "Enable auto-reply in the group only when the user explicitly requests it.",
            returnDirect = true
    )
    public String openAutoReply( String userId) {
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

        return "已开启【" + to + "】自动回复功能";
    }
}
