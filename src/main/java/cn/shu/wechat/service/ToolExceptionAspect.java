package cn.shu.wechat.service;

import dev.langchain4j.service.Result;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Log4j2
public class ToolExceptionAspect {

    //@Around("@annotation(dev.langchain4j.agent.tool.Tool)")
    public Object wrapToolExecution(ProceedingJoinPoint pjp) {
        try {
            return pjp.proceed(); // 执行原方法
        } catch (Throwable e) {
            // 统一返回 Result<String> 错误信息
            log.error(e.getMessage(), e);
            return Result.<String>builder()
                    .content("工具执行异常: " + e.getMessage())
                    .build();
        }
    }
}
