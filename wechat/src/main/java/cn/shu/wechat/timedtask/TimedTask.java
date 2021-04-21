package cn.shu.wechat.timedtask;

import cn.shu.wechat.core.Core;
import cn.shu.wechat.service.ILoginService;
import cn.shu.wechat.utils.ChartUtil;
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
        executor.setMaxPoolSize(3);
        executor.setCorePoolSize(3);
        executor.setQueueCapacity(0);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return executor;
    }

    /**
     * 登录服务
     */
    @Resource
    private ILoginService loginService;

    /**
     * 图表工具类
     */
    @Resource
    private ChartUtil chart;

    /**
     * 30秒获取一次联系人信息 76j00
     */
    @Scheduled(cron = "*/30 * * * * ?")
    /*@Async*/
    public void updateContactTask() {
        if (Core.isAlive()) {
            loginService.webWxGetContact();

            loginService.WebWxBatchGetContact();
            // log.info("更新联系人完成！");
        }
    }

    /**
     * 10分钟检测一次登录状态
     */
    @Scheduled(cron = "0 */10 * * * ?")
    /*@Async*/
    public void checkLoginStatusTask() {
        if (Core.isAlive()) {
            // 秒为单位
            long t1 = System.currentTimeMillis();
            if (t1 - Core.getLastNormalRetCodeTime() > 60 * 1000) {
                // 超过60秒，判为离线
                //Core.setAlive(false);
                // 心跳检测不准确
                log.info("微信已离线");
            }
        }
    }

    /**
     * 生成图标
     */
    @Scheduled(cron = "0 0 12 * * ?")
    @Async
    public void createChart() {
        chart.create();
    }
}
