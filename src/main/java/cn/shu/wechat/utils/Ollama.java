package cn.shu.wechat.utils;

import cn.shu.wechat.service.Assisant;
import cn.shu.wechat.service.FunctionCallingService;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Ollama {


    @Data
    static class Request {
        private String model;
        private List<Message> messages;
        private Boolean stream;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Message {
        private String role;
        private String content;
        private List<String> images;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class ResponseBody {
        private String response;
        private String model;
        //private Date created_at;
        private Message message;
        private String done_reason;
        private boolean done;
        private long total_duration;
        private long load_duration;
        private int prompt_eval_count;
        private long prompt_eval_duration;
        private int eval_count;
        private long eval_duration;
    }


    @Resource
    private Assisant assisant;

    @Bean
    public ChatMemoryStore chatMemoryStore() {
        return new InMemoryChatMemoryStore();
    }

    @Bean
    public ChatMemoryProvider chatMemoryProvider(ChatMemoryStore store) {
        return (sessionId) -> MessageWindowChatMemory.builder().chatMemoryStore(store).maxMessages(20).id(sessionId).build();
    }

    @Bean
    @Qualifier("imageModel")
    public ChatModel imageChatModel() {
        return OllamaChatModel.builder()
                .modelName("llama3.1:8b")
                .baseUrl("http://localhost:11434")
                .build();
    }

    private ChatModel openAIChatModel;
    @Bean
    public Assisant assistant(@Qualifier("openAiChatModel") ChatModel chatModel, ChatMemoryProvider chatMemoryProvider, FunctionCallingService functionCallingService) {
        this.openAIChatModel = chatModel;
        return AiServices.builder(Assisant.class)
                .chatModel(chatModel)
                .tools(functionCallingService)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }

    @Autowired
    @Qualifier("imageModel")
    private ChatModel imageModel;

    private static final Map<String, List<Message>> msgHistory = new HashMap<>();


    public String chatWithSpringAi(String fromUserNme, String toUserName, String question) {
        FunctionCallingService.setUserName(toUserName);
        String chat = assisant.chat(fromUserNme,question);

        return chat;
    }


    public String chatWithHistory(String userName, Path imgPath) throws IOException {
        UserMessage userMessage = UserMessage.from(
                TextContent.from("提取图中的群聊名称。"),
                ImageContent.from(imgPath.toUri())
        );
        OpenAiChatModel build = OpenAiChatModel.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey("sk-a7f53eff7ecb4787a23b7726301dad")
                .modelName("qwen-vl-max").build();
//        byte[] imageData = Files.readAllBytes(imgPath);
//        String base64Image = Base64.getEncoder().encodeToString(imageData);
        ChatResponse chat = build.chat(userMessage);
        return chat.aiMessage().text();
    }


}
