package cn.shu.wechat.utils;

import cn.shu.wechat.configuration.WechatConfiguration;
import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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

    private final ChatClient ollamaiChatClient;

    public Ollama(ChatModel chatModel, ChatMemory chatMemory) {
        // 构造时，可以设置 ChatClient 的参数
        // {@link org.springframework.ai.chat.client.ChatClient};
        this.ollamaiChatClient = ChatClient.builder(chatModel)
                // 实现 Logger 的 Advisor
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                // 设置 ChatClient 中 ChatModel 的 Options 参数
                .defaultOptions(
                        OllamaOptions.builder()
                                .topP(0.7)
                                .build()
                )
                .build();
    }

    public static String chatNative(String userNme, String question) {


        String json = "{\"model\": \"qwen:7b\", \"prompt\": \"Why is the sky blue?\",\"stream\": false}";

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");


        ResponseBody responseB = HttpUtil.doPost("http://localhost:11434/api/generate", json, headers, HttpUtil.getJsonEntityBodyHandler(ResponseBody.class));

        msgHistory.get(userNme).add(Message.builder().role("user").content(question).build());
        msgHistory.get(userNme).add(Message.builder().role("assistant").content(responseB.response).build());


        return responseB.getResponse();
    }
    private static final Map<String, List<Message>> msgHistory = new HashMap<>();

    public String chatWithSpringAi(String userNme, String question) {
        return ollamaiChatClient.prompt()

                .user(question)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userNme))
                .call().content();
    }

    public static String chatWithHistory(String userName, String question) throws IOException {
        return chatWithHistory(userName, question, null);
    }

    public static String chatWithHistory(String userName, Path imgPath) throws IOException {
        return chatWithHistory(userName, null, imgPath);
    }

    public static String chatWithHistory(String userName, String question, Path imgPath) throws IOException {

        // JSON请求体
        Request request = new Request();
        msgHistory.putIfAbsent(userName, new ArrayList<>());
        List<Message> messages = msgHistory.get(userName);

        Message message = Message.builder().role("user").build();

        WechatConfiguration instance = WechatConfiguration.getInstance();
        if (StringUtils.isNotEmpty(question)) {
            message.setContent(question);
            request.setModel(instance.getBigModelUni());//
        }
        if (imgPath != null && Files.exists(imgPath)) {
            request.setModel(instance.getBigModelDual());
            byte[] imageData = Files.readAllBytes(imgPath);
            String base64Image = Base64.getEncoder().encodeToString(imageData);
            if (StringUtils.isNotEmpty(base64Image)) {
                message.setImages(Collections.singletonList(base64Image));
            }
        }
        messages.add(message);
        request.setMessages(messages);
        request.setStream(false);
        String reqStr = JSON.toJSONString(request);


        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        ResponseBody responseB = HttpUtil.doPost("http://localhost:11434/api/chat", reqStr, headers, HttpUtil.getJsonEntityBodyHandler(ResponseBody.class));

        msgHistory.computeIfAbsent(userName, k -> new ArrayList<>());
        // msgHistory.get(userName).add(Message.builder().role("user").content(question).build());
        msgHistory.get(userName).add(responseB.getMessage());


        return responseB.getMessage().getContent();
    }

    public static void main(String[] args) throws IOException {
        Path p = Paths.get("E:\\weixin\\71cfcd52eb41be223985e38f06c79c8b\\MSGTYPE_IMAGE\\Doctor.丁\\2024-07-06\\254286469952502-2024-07-06-03-43-34.gif");
        System.out.println(Ollama.chatWithHistory("舒新生", "快速排序的代码", null));

    }
}
