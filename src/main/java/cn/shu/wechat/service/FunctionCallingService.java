package cn.shu.wechat.service;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.dto.response.msg.send.WebWXSendMsgResponse;
import cn.shu.wechat.utils.ChartUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FunctionCallingService {
    private static final ThreadLocal<String> contextToUserName = new ThreadLocal<>();
    @Resource
    private ChartUtil chartUtil;

    public static void setUserName(String userName) {
        contextToUserName.set(userName);
    }

    @Tool(description = "\"仅在用户询问群聊男女比例时调用")
    public String getGenderRatio() {
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
        return "已计算出男女比例并通过外部渠道发送给提问者，你不用再计算，你只需告诉用户已经发送。";
    }
}
