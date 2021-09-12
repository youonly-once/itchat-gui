package cn.shu.wechat.utils;

import lombok.extern.log4j.Log4j2;

import java.util.concurrent.TimeUnit;

/**
 * Created by xiaoxiaomo on 2017/5/6.
 */
@Log4j2
public class SleepUtils {

    /**
     * 毫秒为单位
     *
     * @param time 休眠时间 毫秒
     */
    public static void sleep(long time) {
        try {
            TimeUnit.MILLISECONDS.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
