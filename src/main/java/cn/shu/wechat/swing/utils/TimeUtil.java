package cn.shu.wechat.swing.utils;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by 舒新胜 on 28/04/2017.
 * Updated by 舒新胜 on 29/12/2021.
 */

public class TimeUtil {

    /**
     * 判断二个时间是否在同一分钟内
     * @param ts1 时间一
     * @param ts2 时间二
     * @return true 同一分钟内 false不在同一分钟内
     */
    public static boolean inTheSameMinute(LocalDateTime ts1, LocalDateTime ts2) {

       return Duration.between(ts1,ts2).toMinutes() == 0;
    }

    /**
     * 微信 时间差的表示格式
     * @param messageLocalDateTime 消息时间
     * @return 微信时间差格式
     */
    public static String diff(LocalDateTime messageLocalDateTime) {
        return diff(messageLocalDateTime, false);
    }

    /**
     * 微信 时间差的表示格式
     * @param messageLocalDateTime 消息时间
     * @param detail 是否显示详细时间
     * @return 微信时间差格式
     */
    public static String diff(LocalDateTime messageLocalDateTime, boolean detail) {
        LocalDateTime currentLocalDateTime = LocalDateTime.now();

        Duration duration = Duration.between(messageLocalDateTime, currentLocalDateTime);
        Period period = Period.between(messageLocalDateTime.toLocalDate(), currentLocalDateTime.toLocalDate());

        //当前时间和消息时间相差天数
        long diffDay = duration.toDays();
        //是否为同一年
        boolean sameYear = (period.getYears() == 0);

        String ret;
        if (sameYear && diffDay < 1) {
            //1天内的消息 时间显示时分秒
            return messageLocalDateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else if (sameYear && diffDay < 2) {
            if (detail) {
                return messageLocalDateTime.format(DateTimeFormatter.ofPattern("昨天 HH:mm"));
            } else {
                return "昨天"/* + daySimpleDateFormat.format(new Date(timestamp))*/;
            }
        } else if (sameYear && diffDay < 8) {
            if (detail) {
                return messageLocalDateTime.getDayOfWeek().toString() + messageLocalDateTime.format(DateTimeFormatter.ofPattern(" HH:mm"));
            } else {
                return  messageLocalDateTime.getDayOfWeek().toString()/* + " " + daySimpleDateFormat.format(new Date(timestamp))*/;
            }
        } else if (sameYear && diffDay < 366) {
            if (detail) {
                return messageLocalDateTime.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
            } else {
                return messageLocalDateTime.format(DateTimeFormatter.ofPattern("MM-dd"));
            }
        } else {
            return messageLocalDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }
}
