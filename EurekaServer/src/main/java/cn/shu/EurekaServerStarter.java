package cn.shu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author SXS
 * @since 4/21/2021
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerStarter {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerStarter.class, args);
    }
}
