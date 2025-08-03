package cn.shu.wechat.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 8/18/2021 21:31
 */
public class DateUtils {
    public static final String YYYY_MM_DD_HH_MM_SS = "YYYY-MM-dd HH:mm:ss";
    /**
     * 日期格式化
     * @param date 日期
     * @param patten 格式
     * @return 字符串
     */
    public static String format(long date,String patten){
        SimpleDateFormat dateFormat = new SimpleDateFormat(patten);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return dateFormat.format(new Date(date));
    }


    /**
     * 日期格式化
     * @param date 日期
     * @param patten 格式
     * @return 日期
     */
    public static long parse(String date,String patten) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(patten);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        Date parse = dateFormat.parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(parse);
        long time=cal.getTimeInMillis();
        return time ;
    }

    /**
     * 当前时间
     * @param patten 格式
     * @return 字符串
     */
    public static String getCurrDateString(String patten){
        return format(System.currentTimeMillis(),patten);
    }

    /**
     * 判断二个时间是否在同一分钟内
     *
     * @param ts1 时间一
     * @param ts2 时间二
     * @return true 同一分钟内 false不在同一分钟内
     */
    public static boolean inTheSameMinute(LocalDateTime ts1, LocalDateTime ts2) {

        return Duration.between(ts1, ts2).toMinutes() == 0;
    }

    /**
     * 微信 时间差的表示格式
     *
     * @param messageLocalDateTime 消息时间
     * @return 微信时间差格式
     */
    public static String diff(LocalDateTime messageLocalDateTime) {
        return diff(messageLocalDateTime, false);
    }

    /**
     * 微信 时间差的表示格式
     *
     * @param messageLocalDateTime 消息时间
     * @param detail               是否显示详细时间
     * @return 微信时间差格式
     */
    public static String diff(LocalDateTime messageLocalDateTime, boolean detail) {
        LocalDateTime currentLocalDateTime = LocalDateTime.now();
        //当前时间和消息时间相差天数
        long diffDay = ChronoUnit.DAYS.between(messageLocalDateTime.toLocalDate(), currentLocalDateTime.toLocalDate());
        //是否为同一年
        boolean sameYear = (currentLocalDateTime.getYear() - messageLocalDateTime.getYear() == 0);

        String ret;
        if (diffDay < 1) {
            //1天内的消息 时间显示时分秒
            return messageLocalDateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else if (diffDay < 2) {
            if (detail) {
                return messageLocalDateTime.format(DateTimeFormatter.ofPattern("昨天 HH:mm"));
            } else {
                return "昨天";
            }
        } else if (diffDay < 3) {
            if (detail) {
                return messageLocalDateTime.format(DateTimeFormatter.ofPattern("前天 HH:mm"));
            } else {
                return "前天";
            }
        } else if (diffDay < 8) {
            //周
            if (detail) {
                return messageLocalDateTime.getDayOfWeek().toString() + messageLocalDateTime.format(DateTimeFormatter.ofPattern(" HH:mm"));
            } else {
                return messageLocalDateTime.getDayOfWeek().toString();
            }
        } else if (sameYear) {
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
