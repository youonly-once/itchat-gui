package cn.shu.wechat.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
}
