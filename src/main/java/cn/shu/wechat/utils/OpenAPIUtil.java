package cn.shu.wechat.utils;

import cn.shu.wechat.pojo.entity.Message;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import okhttp3.*;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.Duration.ofSeconds;

public class OpenAPIUtil {
    private static final String BASE_URL = "https://api.openai.com/";

    public static List<Message> chat(String q) {
        String token = "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";
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request()
                                .newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .build();
                        return chain.proceed(request);
                    }
                })
                .sslSocketFactory(TestSSLSocketClient.getSSLSocketFactory(), TestSSLSocketClient.getX509TrustManager())
                .hostnameVerifier(TestSSLSocketClient.getHostnameVerifier())
                .connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
                .readTimeout(ofSeconds(60).toMillis(), TimeUnit.MILLISECONDS)
                .build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();


        OpenAiService service = new OpenAiService(retrofit.create(OpenAiApi.class));
        CompletionRequest completionRequest = CompletionRequest.builder()
                .prompt(q)
                .maxTokens(1024)
                .model("text-davinci-003")
                .echo(true)
                .build();
        CompletionResult completion = service.createCompletion(completionRequest);
        Stream<Message> messageStream = completion.getChoices().stream()
                .map(e -> {
                    return Message.builder().content(e.getText().substring(e.getText().indexOf("\n\n") + 2)).build();
                });
        return messageStream.collect(Collectors.toList());
    }
}
