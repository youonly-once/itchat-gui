package cn.shu.wechat.timedtask;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.service.LoginService;
import cn.shu.wechat.utils.ChartUtil;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import cn.shu.wechat.utils.SleepUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author SXS
 * @since 4/13/2021
 */
@Component
@Log4j2
public class TimedTask {
    @Bean
    public AsyncTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("ThreadPoolTaskExecutor-");
        executor.setMaxPoolSize(1);
        executor.setCorePoolSize(1);
        executor.setQueueCapacity(0);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return executor;
    }

    /**
     * 登录服务
     */
    @Resource
    private LoginService loginService;


    /**
     * 图表工具类
     */
    @Resource
    private ChartUtil chart;

    /**
     * 30秒获取一次联系人信息 76j00
     */
    @Scheduled(cron = "*/59 * * * * ?")
    public void updateContactTask() {
        if (Core.isAlive()) {
            loginService.webWxGetContact();

            loginService.WebWxBatchGetContact();

        }
    }
}
