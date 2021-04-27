package cn.shu.controller;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author SXS
 * @since 4/26/2021
 */
@RestController

public class TestController {
    public void setMsg(String msg) {
        this.msg = msg;
    }

    private String msg;

    @RequestMapping("getMsg")
    public String getMsg(){
        return msg;
    }
}
