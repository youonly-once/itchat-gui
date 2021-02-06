package cn.shu.weichat.utils;

import lombok.extern.log4j.Log4j2;

/**
 * Created by xiaoxiaomo on 2017/5/6.
 */
@Log4j2
public class SleepUtils {

    /**
     * 毫秒为单位
     * @param time
     */
    public static void sleep( long time ){
        try {
            Thread.sleep( time );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
