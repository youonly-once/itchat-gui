package cn.shu.wechat.service;

import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
//自动模式找不到Please specify either chatModel or streamingChatModel

public interface Assisant {


    String chat(String userMessage);

    String chat(@MemoryId String sessionId, @UserMessage String userMessage);

}
