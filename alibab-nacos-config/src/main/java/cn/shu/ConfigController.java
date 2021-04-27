package cn.shu;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author SXS
 * @since 4/26/2021
 */
@RestController
@RequestMapping("/config")
@RefreshScope
public class ConfigController {

    @Value("${x:200}")
    private int useLocalCache;

    @RequestMapping("/get")
    public int get() {
        return useLocalCache;
    }
}