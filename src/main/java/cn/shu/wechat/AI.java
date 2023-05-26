package cn.shu.wechat;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;

import java.util.Optional;

public class AI {
    public static void main(String[] args) {


        OpenAiService service = new OpenAiService("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", 20);

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
