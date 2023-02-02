package cn.shu.wechat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import okhttp3.*;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.time.Duration.ofSeconds;

public class AI2 {
    private static final String BASE_URL = "https://api.openai.com/";

    public static void main(String[] args) {
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
                .connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
                .readTimeout(ofSeconds(20).toMillis(), TimeUnit.MILLISECONDS)
                .build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();


        OpenAiService service = new OpenAiService(retrofit.create(OpenAiApi.class));
        CompletionRequest completionRequest = CompletionRequest.builder()
                .prompt("导师call我了")
                .maxTokens(1024)
                .model("text-davinci-003")
                .echo(true)
                .build();
        CompletionResult completion = service.createCompletion(completionRequest);
        Optional<String> result = completion.getChoices().stream().findFirst().map(CompletionChoice::getText);
        if (result.isPresent()) {
            String[] split = result.get().split("\n\n");
            System.out.println(split[1]);
        }
        completion.getChoices().forEach(System.out::println);

    }
}
