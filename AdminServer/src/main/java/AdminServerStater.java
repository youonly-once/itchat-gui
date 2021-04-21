import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Configuration;

/**
 * @author SXS
 * @since 4/9/2021
 */
@Configuration
@EnableAutoConfiguration
@EnableAdminServer
@EnableEurekaClient
public class AdminServerStater {
    public static void main(String[] args) {
        SpringApplication.run(AdminServerStater.class, args);
    }
}
