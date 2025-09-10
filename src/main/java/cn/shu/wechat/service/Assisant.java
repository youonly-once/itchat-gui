package cn.shu.wechat.service;

import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
//自动模式找不到Please specify either chatModel or streamingChatModel
@AiService(chatModel = "ollamaChatModel",wiringMode = AiServiceWiringMode.EXPLICIT,tools = { "functionCallingService" })
public interface Assisant {


    String chat(String userMessage);

    @Tool(
            name = "get_group_gender_ratio",
            value = "Get the male-to-female ratio of members in a WeChat group. Only call this when the user explicitly asks for the gender ratio."
    )

    public Result<String> getGenderRatio(String groupId);

    @Tool(
            name = "open_auto_reply",
            value = "Enable auto-reply in the group only when the user explicitly requests it."
    )
    Result<String>  openAutoReply();
}
