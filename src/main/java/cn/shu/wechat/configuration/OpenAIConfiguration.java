package cn.shu.wechat.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "openai")
public class OpenAIConfiguration {

    private static OpenAIConfiguration instance;
    private String openaiKey;
    private int expire;

    public OpenAIConfiguration() {
        instance = this;
    }

    public static OpenAIConfiguration getInstance() {
        return instance;
    }

    public String getOpenaiKey() {
        return openaiKey;
    }

    public void setOpenaiKey(String openaiKey) {
        this.openaiKey = openaiKey;
    }

    public int getExpire() {
        return expire;
    }

    public void setExpire(int expire) {
        this.expire = expire;
    }

}
