package cn.shu.wechat.utils;

import cn.shu.wechat.configuration.WechatConfiguration;
import cn.shu.wechat.service.Assisant;
import cn.shu.wechat.service.FunctionCallingService;
import com.alibaba.fastjson.JSON;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.*;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

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

    private final ChatModel chatModel;

    public Ollama(ChatModel chatModel) {
        this.chatModel = chatModel;
//        // 构造时，可以设置 ChatClient 的参数
//        // {@link org.springframework.ai.chat.client.ChatClient};
//        this.ollamaiChatClient = ChatClient.builder(chatModel)
//                // 实现 Logger 的 Advisor
//                .defaultAdvisors(
//                        MessageChatMemoryAdvisor.builder(chatMemory).build()
//                )
//                // 设置 ChatClient 中 ChatModel 的 Options 参数
//                .defaultOptions(
//                        OllamaOptions.builder()
//                                .topP(0.7)
//                                .build()
//                )
//                .build();
//        new T();
//        chatModel.chat()
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
    @Resource
    private FunctionCallingService functionCallingService;
    @Resource
    private Assisant assisant;
    public String chatWithSpringAi(String fromUserNme, String toUserName, String question) {
        FunctionCallingService.setUserName(toUserName);
        String systemPrompt = """
你只能在用户明确请求时调用以下工具：

1. get_group_gender_ratio(groupId)
   - 仅在用户明确要求获取微信群成员男女比例时调用此工具。
   - 不要在问候语、闲聊或与群男女比例无关的问题时调用。

2. open_auto_reply(userId)
   - 仅在用户明确要求开启群聊自动回复功能时调用此工具。
   - 不要在问候语、闲聊或与自动回复无关的问题时调用。

如果没有工具可以调用，请始终用自然语言回答。
不要在一般知识性问题（如“中国有哪些出名的山”）中触发任何工具。
""";

//        String content = ollamaiChatClient.prompt()
//                .tools(functionCallingService)
//                .system(systemPrompt)
//                .user(question)
//                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, fromUserNme))
//                .call().content();
//        if (FunctionCallingService.invoke.get() == null || !FunctionCallingService.invoke.get()){
//            return ollamaiChatClient.prompt()
//                    .user(question)
//                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, fromUserNme))
//                    .call().content();
//        }else{
//            return content;
//        }
//        return ollamaiChatClient.prompt()
//                .user(question)
//                .toolCallbacks(FunctionToolCallback.builder("getWeather", new Function<Object, Object>() {
//                            @Override
//                            public Object apply(Object o) {
//                                return "测试";
//                            }
//                        })
//                        .description("Use api.weather to get weather information.")
//                        .inputType(String.class)
//                        .build())
//                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, fromUserNme))
//                .call().content();
//        UserMessage userMessage = UserMessage.from(
//                TextContent.from(question)
//        );
//        List<ToolSpecification> toolSpecifications = ToolSpecifications.toolSpecificationsFrom(FunctionCallingService.class);
//        ChatRequest request = ChatRequest.builder()
//                .messages(userMessage)
//                .toolSpecifications(toolSpecifications)
//                .build();
//        ChatResponse chat = chatModel.chat(request);



//        if (chat.aiMessage().hasToolExecutionRequests()) {
//            ToolExecutionResultMessage toolExecutionResultMessage = ToolExecutionResultMessage.from(chat.aiMessage().toolExecutionRequests(), result);
//            ChatRequest request2 = ChatRequest.builder()
//                    .messages(List.of(userMessage, chat.aiMessage(), toolExecutionResultMessage))
//                    .toolSpecifications(toolSpecifications)
//                    .build();
//        }
        String chat = assisant.chat(question);

        return chat;
        //return "";
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
