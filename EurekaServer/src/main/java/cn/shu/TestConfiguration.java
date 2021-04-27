package cn.shu;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author SXS
 * @since 4/26/2021
 */
@Configuration
@ConfigurationProperties("test")
public class TestConfiguration {
    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    private String a;
    private String b;
}
