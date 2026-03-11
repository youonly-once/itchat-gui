package cn.shu.wechat.utils;

import cn.shu.wechat.service.Assisant;
import cn.shu.wechat.service.FunctionCallingService;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Component
public class Ollama {


    @Resource
    private Assisant assisant;


    @Autowired
    @Qualifier("imageModel")
    private ChatModel imageModel;

    @Bean
    public ChatMemoryStore chatMemoryStore() {
        return new InMemoryChatMemoryStore();
    }

    @Bean
    public ChatMemoryProvider chatMemoryProvider(ChatMemoryStore store) {
        return (sessionId) -> MessageWindowChatMemory.builder().chatMemoryStore(store).maxMessages(20).id(sessionId).build();
    }

    @Bean
    @Qualifier("qwenVLMaximageChatModel")
    public ChatModel qwenVLMaximageChatModel(@Qualifier("openAiChatModel") ChatModel openAiChatModel) {
        return OpenAiChatModel.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey("sk-a7f53eff7ecb4787a23b7726301dad31")
                .modelName("qwen3-vl-plus-2025-12-19").build();
    }

    @Bean
    @Qualifier("imageModel")
    public ChatModel imageChatModel() {
        return OllamaChatModel.builder()
                .modelName("llama3.1:8b")
                .baseUrl("http://localhost:11434")
                .build();
    }

    @Bean
    public Assisant assistant(@Qualifier("ollamaChatModel") ChatModel openAiChatModel, ChatMemoryProvider chatMemoryProvider, FunctionCallingService functionCallingService) {
        return AiServices.builder(Assisant.class)
                .chatModel(openAiChatModel)
                .tools(functionCallingService)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }



    public String chatWithSpringAi(String fromUserNme, String toUserName, String question) {
        FunctionCallingService.setUserName(toUserName);
        String chat = assisant.chat(fromUserNme,question);

        return chat;
    }


    public String chatWithHistory(String userName, Path imgPath) throws IOException {
        Base64.Encoder encoder = Base64.getEncoder();
        String s = encoder.encodeToString(Files.readAllBytes(imgPath));
//        UserMessage userMessage = UserMessage.from(
//                TextContent.from("提取图中的群聊名称。"),
//                ImageContent.from(s,"image/gif")
//        );
//        OpenAiChatModel build = OpenAiChatModel.builder()
//                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
//                .apiKey("sk-a7f53eff7ecb4787a23b7726301dad31")
//                .modelName("qwen-vl-max").build();
//        byte[] imageData = Files.readAllBytes(imgPath);
//        String base64Image = Base64.getEncoder().encodeToString(imageData);
        //ChatResponse chat = build.chat(userMessage);
        return null;//chat.aiMessage().text();
    }


}
