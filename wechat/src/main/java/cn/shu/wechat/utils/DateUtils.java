package cn.shu.wechat.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 8/18/2021 21:31
 */
public class DateUtils {
    public static final String yyyy_mm_dd_hh_mm_ss = "YYYY-MM-dd hh:mm:ss";
    /**
     * 日期格式化
     * @param date 日期
     * @param patten 格式
     * @return 字符串
     */
    public static String format(long date,String patten){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(patten);
        return simpleDateFormat.format(new Date(date));
    }

    /**
     * 日期格式化
     * @param date 日期
     * @param patten 格式
     * @return 日期
     */
    public static  Date parse(String date,String patten) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(patten);
        return simpleDateFormat.parse(date);
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
