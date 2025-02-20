package cn.shu.wechat.utils;

import cn.shu.wechat.configuration.WechatConfiguration;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Ollama {
    @Data
    static class Request{
        private String model;
        private List<Message> messages;
        private Boolean stream;
    }
    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Message{
        private String role;
        private String content;
        private List<String> images;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class ResponseBody{
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
    private static final Map<String, List<Message>> msgHistory = new HashMap<>();
    public static String  chat(String userNme ,String question){
        String result = null;
        // 创建HttpClient
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 创建HttpPost请求
            HttpPost httpPost = new HttpPost("http://localhost:11434/api/generate");
            httpPost.setHeader("Content-Type", "application/json");

            // JSON请求体
            String json = "{\"model\": \"qwen:7b\", \"prompt\": \"Why is the sky blue?\",\"stream\": false}";
            StringEntity entity = new StringEntity(json);
            httpPost.setEntity(entity);

            // 发送请求并获取响应
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                ResponseBody responseB = JSON.parseObject(responseBody, ResponseBody.class);
                if(statusCode == 200){
                    msgHistory.get(userNme).add(Message.builder().role("user").content(question).build());
                    msgHistory.get(userNme).add(Message.builder().role("assistant").content(responseB.response).build());
                    result = responseB.getResponse();
                }
                // 输出响应状态码和响应体
                System.out.println("Response Code: " + statusCode);
                System.out.println("Response Body: " + responseBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    public static String  chatWithHistory(String userName ,String question){
        return chatWithHistory(userName,question,null);
    }
    public static String  chatWithHistory(String userName ,Path imgPath){
        return chatWithHistory(userName,null,imgPath);
    }
    public static String  chatWithHistory(String userName ,String question,Path imgPath){
        String result = null;
        // 创建HttpClient
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 创建HttpPost请求
            HttpPost httpPost = new HttpPost("http://localhost:11434/api/chat");
            httpPost.setHeader("Content-Type", "application/json");
            // JSON请求体
            Request request = new Request();
            msgHistory.putIfAbsent(userName,new ArrayList<>());
            List<Message> messages =msgHistory.get(userName);

             Message message = Message.builder().role("user").build();

            WechatConfiguration instance = WechatConfiguration.getInstance();
            if(StringUtils.isNotEmpty(question)){
                message.setContent(question);
                request.setModel(instance.getBigModelUni());//
            }
            if(imgPath != null && Files.exists(imgPath)){
                request.setModel(instance.getBigModelDual());
                byte[] imageData = Files.readAllBytes(imgPath);
                String base64Image = Base64.getEncoder().encodeToString(imageData);
                if(StringUtils.isNotEmpty(base64Image)) {
                    message.setImages(Collections.singletonList(base64Image));
                }
            }
            messages.add(message);
            request.setMessages(messages);
            request.setStream(false);
            String reqStr = JSON.toJSONString(request);
            StringEntity entity = new StringEntity(reqStr,"UTF-8");
            httpPost.setEntity(entity);
            // 发送请求并获取响应
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                ResponseBody responseB = JSON.parseObject(responseBody, ResponseBody.class);
                if(statusCode == 200){
                    msgHistory.computeIfAbsent(userName, k -> new ArrayList<>());
                   // msgHistory.get(userName).add(Message.builder().role("user").content(question).build());
                    msgHistory.get(userName).add(responseB.getMessage());
                    result = responseB.getMessage().getContent();

                }else{
                    messages.remove(messages.size()-1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    public static String  chatWithHistoryStream(String userName ,String question,Path imgPath){
        String result = null;
        // 创建HttpClient
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 创建HttpPost请求
            HttpPost httpPost = new HttpPost("http://localhost:11434/api/chat");
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Accept", "text/event-stream");
            // JSON请求体
            Request request = new Request();
            msgHistory.putIfAbsent(userName,new ArrayList<>());
            List<Message> messages =msgHistory.get(userName);

            Message message = Message.builder().role("user").build();
            if(StringUtils.isNotEmpty(question)){
                message.setContent(question);
                request.setModel("qwen2:72b");
            }
            if(imgPath != null && Files.exists(imgPath)){
                request.setModel("llava");
                byte[] imageData = Files.readAllBytes(imgPath);
                String base64Image = Base64.getEncoder().encodeToString(imageData);
                if(StringUtils.isNotEmpty(base64Image)) {
                    message.setImages(Collections.singletonList(base64Image));
                }
            }
            messages.add(message);
            request.setMessages(messages);
            request.setStream(true);
            String reqStr = JSON.toJSONString(request);
            StringEntity entity = new StringEntity(reqStr,"UTF-8");
            httpPost.setEntity(entity);
            // 发送请求并获取响应
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();

                HttpEntity responseEntity = response.getEntity();

                if (responseEntity != null) {
                    // 使用 BufferedReader 读取响应数据
                    BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
                    StringBuilder responseBody = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                    reader.close();

                    // 打印响应结果
                    System.out.println(responseBody.toString());

                    // 确保实体完全消耗
                    EntityUtils.consume(entity);
                }
//
//                ResponseBody responseB = JSON.parseObject(responseBody, ResponseBody.class);
//                if(statusCode == 200){
//                    msgHistory.computeIfAbsent(userName, k -> new ArrayList<>());
//                    // msgHistory.get(userName).add(Message.builder().role("user").content(question).build());
//                    msgHistory.get(userName).add(responseB.getMessage());
//                    result = responseB.getMessage().getContent();
//
//                }else{
//                    messages.remove(messages.size()-1);
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    public static void main(String[] args) {
        Path p  = Paths.get("E:\\weixin\\71cfcd52eb41be223985e38f06c79c8b\\MSGTYPE_IMAGE\\Doctor.丁\\2024-07-06\\254286469952502-2024-07-06-03-43-34.gif");
        System.out.println( Ollama.chatWithHistory("舒新生","快速排序的代码",null));

    }
}
