package utils;

import lombok.extern.log4j.Log4j2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Log4j2
public class DateUtil {// 返回查询时间
	private DateUtil(){}
	/**
	 *
	 * @return 当前年份
	 */
	public static String getYear(){
		return String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
	}
	/**
	 *
	 * @return 当前月份
	 */
	public static String getMonth(){
		return String.valueOf(Calendar.getInstance().get(Calendar.MONTH)+1);
	}
	/**
	 *
	 * @return 当前日
	 */
	public static String getDay(){
		return String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));// 年
	}
	/**
	 *
	 * @return 当前小时
	 */
	public static String getHour(){
		return String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));// 年
	}
	/**
	 *
	 * @return 当前分钟
	 */
	public static String getMinute(){
		return String.valueOf(Calendar.getInstance().get(Calendar.MINUTE));// 年
	}
	/**
	 *
	 * @return 当前秒
	 */
	public static String getSecond(){
		return String.valueOf(Calendar.getInstance().get(Calendar.SECOND));// 年
	}
	/**
	 *
	 * @return 当前毫秒
	 */
	public static String getMilliSecond(){
		return String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND));
	}
	/**
	 *
	 * @return 2020-03-18
	 */
	public static String getCurrDate() {
		String year = DateUtil.getYear();// 年
		String month = DateUtil.getMonth();// 月 0-11表示12月
		String day = DateUtil.getDay();// 日
		if (month.length() == 1) {
			month = "0" + month;
		}
		if (day.length() == 1) {
			day = "0" + day;
		}
		return year + "-" + month + "-" + day;
	}
	/**
	 *
	 * @return 2020-03-18 15:00:00
	 */
	public static String getCurrDateAndTime() {
		String hour = DateUtil.getHour();// 时
		String minute = DateUtil.getMinute();// 分
		String second = DateUtil.getSecond();// 秒
		if (hour.length() == 1) {
			hour = "0" + hour;
		}
		if (minute.length() == 1) {
			minute = "0" + minute;
		}
		if (second.length() == 1) {
			second = "0" + second;
		}

		return getCurrDate() + " " + hour + ":" + minute + ":"
				+ second;// + "." + milliSecond;
	}
	/**
	 *
	 * @return 2020-03-18 15:00:00.000
	 */
	public static String getCurrDateAndTimeMil() {
		String milliSecond = DateUtil.getSecond();// 秒
		if (milliSecond.length() == 1) {
			milliSecond = "00" + milliSecond;
		}else if (milliSecond.length() == 2) {
			milliSecond = "0" + milliSecond;

		}
		return DateUtil.getCurrDateAndTime() +"." + milliSecond;
	}

	/**
	 *
	 * @param n 前-n天 或 n天后
	 * @return 前n天时间2020-03-18
	 */
	public static String getCurrDateBeforeDay(int n) {

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, n);
		String year = String.valueOf(calendar.get(Calendar.YEAR));// 年
		String  month = String.valueOf(calendar.get(Calendar.MONTH) + 1);// 月 0-11表示12月
		String  day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));// 日
		if (month.length() == 1) {
			month = "0" + month;
		}
		if (day.length() == 1) {
			day = "0" + day;
		}

		return year + "-" + month + "-" + day;
	}
	/**
	 * 返回当前日期的文字表示 例如："昨天"、"前天"、"上周"
	 * @param date
	 * @return "昨天"、"前天"、"上周"
	 */
	public static String getCurrDateStr(String date) {
		String dateStr=date.substring(0, date.indexOf(" "));
		String timeStr=date.substring(date.indexOf(" ")+1);
		timeStr=timeStr.substring(0, timeStr.indexOf("."));
		if (getCurrDate().equals(dateStr)) {
			return timeStr;
		}else if (getCurrDateBeforeDay(-1).equals(dateStr)) {
			return "昨天";
		}else if(getCurrDateBeforeDay(-2).equals(dateStr)) {
			return "前天";
		}else{
			return dateStr;
		}
	}

	public static String[] getCurrDay() {

		return new String[]{getCurrDate()+" 00:00:00.000",getCurrDateBeforeDay(1)+" 00:00:00.000"};
	}
	/**
	 * 获取当前月第一天的日期
	 * @return
	 */
	public static String getCurrMonthBegin() {
		return getYear() + "-" + getMonth() + "-01 00:00:00.000";
	}
	/**
	 * 获取当周第一天的日期
	 * @return
	 */
	public static String getCurrWeekBegin() {
		//当周第一天
		Calendar calendar = Calendar.getInstance();// 当前日期
		int currDayWeek=calendar.get(Calendar.DAY_OF_WEEK);//当周第几天
		if(currDayWeek==1){//周日为第一天
			currDayWeek=7;
		}else{
			currDayWeek--;
		}
		calendar.add(Calendar.DAY_OF_MONTH, -(currDayWeek-1));
		int dayOfWeekBegin = calendar.get(Calendar.DAY_OF_MONTH);// 当周第一天
/*		System.out.println(currDayWeek+"");
		System.out.println(dayOfWeekBegin+"");*/
		int year = calendar.get(Calendar.YEAR);// 年
		int month = calendar.get(Calendar.MONTH) + 1;// 月 0-11表示12月
		return year + "-" + month + "-" + dayOfWeekBegin+ " 00:00:00.000";
	}
	/**
	 * 获取12小时过后的时间
	 * @return
	 */
	public static Integer[] getCurr12HourAfter(){
		//7天前
		Calendar calendar = Calendar.getInstance();// 当前日期
		calendar.add(Calendar.HOUR_OF_DAY, 12);
		int year = calendar.get(Calendar.YEAR);// 年
		int month = calendar.get(Calendar.MONTH) + 1;// 月 0-11表示12月
		int day = calendar.get(Calendar.DAY_OF_MONTH);// 日

		return new Integer[]{year,month,day};
	}
	/**
	 * 获取1天后的时间
	 * @return
	 */
	public static String getdDayAdd1(Date date) {
		//当天
		Calendar calendar = Calendar.getInstance();// 当前日期
		calendar.setTime(date);
		// 后一天凌晨 10-1 00:00 - 10.2 00:00
		calendar.add(Calendar.DAY_OF_MONTH, +1);
		int year1 = calendar.get(Calendar.YEAR);// 年
		int month1 = calendar.get(Calendar.MONTH) + 1;// 月 0-11表示12月
		int dayAdd1 = calendar.get(Calendar.DAY_OF_MONTH);

		return year1 + "-" + month1 + "-" + dayAdd1+" 00:00:00.000";
	}
	/**
	 * 获取时间差值
	 * @param begindate 开始时间
	 * @param endDate  结束时间
	 * @return  时间相差毫秒
	 * @throws ParseException
	 */
	public static long getDateDiff(String begindate,String endDate) throws ParseException{
		String format="yyyy-MM-dd HH:mm:ss.SSS";
		return getDateDiff(begindate, endDate, format);
	}
	/**
	 * 获取时间差值
	 * @param begindate 开始时间
	 * @param endDate  结束时间
	 * @return  时间相差毫秒
	 * @throws ParseException
	 */
	public static long getDateDiff(String begindate,String endDate, String format) throws ParseException{
		SimpleDateFormat simpleDateFormat=new SimpleDateFormat(format);
		long diffMillisecond=simpleDateFormat.parse(endDate).getTime()-simpleDateFormat.parse(begindate).getTime();
		//log.info(""+diffMillisecond);
		return diffMillisecond;
	}

}
