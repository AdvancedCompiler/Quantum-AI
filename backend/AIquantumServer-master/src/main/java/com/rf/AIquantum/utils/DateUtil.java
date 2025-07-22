package com.rf.AIquantum.utils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateUtil {
	/**
	 * 默认的日期格式组合，用来将字符串转化为日期用
	 * 
	 */
	public static final String[] DATE_PARSE_PATTERNS = { "yyyy/MM/dd",
			"yyyy-MM-dd", "yyyy年MM月dd日" };
	/**
	 * 获取当前系统时间格式
	 * 
	 */
	public static final String DEFAULT_DATE = "yyyy-MM-dd HH:mm:ss";
	/**
	 * 特殊日期格式
	 */
	public static final String DEFAULT_DATE_T = "yyyy-MM-dd:HH:mm";
	
	/**
	 * 特殊日期格式2
	 */
	public static final String DEFAULT_DATE_T2 = "yyyy-MM-dd HH:mm";
	/**
	 * 默认的时间格式
	 */
	public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

	/**
	 * 默认的日期格式
	 */
	public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
	
	/**
	 * 获取当前系统时间格式
	 * 用于生成文件名
	 */
	public static final String DEFAULT_FILE_DATE = "yyyyMMddHHmmss";
	

	/**
	 * 日期代码，周日
	 */
	public static final int SUNDAY = 1;

	/**
	 * 日期代码，周一
	 */
	public static final int MONDAY = 2;

	/**
	 * 日期代码，周二
	 */
	public static final int TUESDAY = 3;

	/**
	 * 日期代码，周三
	 */
	public static final int WEDNESDAY = 4;

	/**
	 * 日期代码，周四
	 */
	public static final int THURSDAY = 5;

	/**
	 * 日期代码，周五
	 */
	public static final int FRIDAY = 6;

	/**
	 * 日期代码，周六
	 */
	public static final int SATURDAY = 7;

	/**
	 * 日期精度，秒
	 */
	public static final int ACCURACY_SECOND = 1;

	/**
	 * 日期精度，分
	 */
	public static final int ACCURACY_MINUTE = 2;

	/**
	 * 日期精度，小时
	 */
	public static final int ACCURACY_HOUR = 3;

	/**
	 * 日期精度，天
	 */
	public static final int ACCURACY_DAY = 4;

	/**
	 * 日期精度，月
	 */
	public static final int ACCURACY_MONTH = 5;

	/**
	 * 日期精度，年
	 */
	public static final int ACCURACY_YEAR = 6;

	/**
	 * 比较用日期格式，精度为年
	 */
	public static final String ACCURACY_PATTERN_YEAR = "yyyy";

	/**
	 * 比较用日期格式，精度为月
	 */
	public static final String ACCURACY_PATTERN_MONTH = "yyyyMM";

	/**
	 * 比较用日期格式，精度为日
	 */
	public static final String ACCURACY_PATTERN_DAY = "yyyyMMdd";

	/**
	 * 比较用日期格式，精度为时
	 */
	public static final String ACCURACY_PATTERN_HOUR = "yyyyMMddHH";

	/**
	 * 比较用日期格式，精度为分
	 */
	public static final String ACCURACY_PATTERN_MINUTE = "yyyyMMddHHmm";

	/**
	 * 比较用日期格式，精度为秒
	 */
	public static final String ACCURACY_PATTERN_SECOND = "yyyyMMddHHmmss";

	/**
	 * 单一属性格式，时
	 */
	public static final String SINGLE_YEAR = "yyyy";

	/**
	 * 单一属性格式，时
	 */
	public static final String SINGLE_MONTH = "M";

	/**
	 * 单一属性格式，时
	 */
	public static final String SINGLE_DAY = "d";

	/**
	 * 单一属性格式，时
	 */
	public static final String SINGLE_HOUR = "H";

	/**
	 * 单一属性格式，分
	 */
	public static final String SINGLE_MINUTE = "m";

	/**
	 * 单一属性格式，秒
	 */
	public static final String SINGLE_SECOND = "s";

	/**
     * 
     */
	public static final long MILLISECONDS_PER_SECOND = 1000;

	/**
     * 
     */
	public static final long MILLISECONDS_PER_MINUTE = 1000 * 60;

	/**
     * 
     */
	public static final long MILLISECONDS_PER_HOUR = 1000 * 60 * 60;

	/**
     * 
     */
	public static final long MILLISECONDS_PER_DAY = 1000 * 60 * 60 * 24;

	/**
	 * 将给定的日期字符串，按照预定的日期格式，转化为Date型数据
	 * 
	 * @param dateStr
	 *            日期字符字符串
	 * @return 日期型结果
	 */
	public static Date parseDate(String dateStr) {
		Date date = null;
		try {
			date = org.apache.commons.lang.time.DateUtils.parseDate(dateStr,
					DATE_PARSE_PATTERNS);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	/**
	 * 根据指定格式转化String型日期到Date型
	 * 
	 * @param dateStr
	 *            String型日期
	 * @param parsePattern
	 *            指定的格式
	 * @return Date型日期
	 */
	public static Date parseDate(String dateStr, String parsePattern) {
		Date date = null;
		try {
			date = org.apache.commons.lang.time.DateUtils.parseDate(dateStr,
					new String[] { parsePattern.toString() });
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	/**
	 * 返回系统当前时间（Date型）
	 * 
	 * @return 系统当前时间
	 */
	public static Date getCurrentDate() {
		return new Date();
	}

	/**
	 * 日期计算，日加减
	 * 
	 * @param date
	 *            初始日期
	 * @param amount
	 *            天数增量（负数为减）
	 * @return 计算后的日期
	 */
	public static Date addDays(Date date, int amount) {
		return org.apache.commons.lang.time.DateUtils.addDays(date, amount);
	}

	/**
	 * 日期计算，周加减
	 * 
	 * @param date
	 *            初始日期
	 * @param amount
	 *            周数增量（负数为减）
	 * @return 计算后的日期
	 */
	public static Date addWeeks(Date date, int amount) {
		return org.apache.commons.lang.time.DateUtils.addWeeks(date, amount);
	}

	/**
	 * 日期计算，月加减
	 * 
	 * @param date
	 *            初始日期
	 * @param amount
	 *            月数增量（负数为减）
	 * @return 计算后的日期
	 */
	public static Date addMonths(Date date, int amount) {
		return org.apache.commons.lang.time.DateUtils.addMonths(date, amount);
	}

	/**
	 * 日期计算，年加减
	 * 
	 * @param date
	 *            初始日期
	 * @param amount
	 *            年数增量（负数为减）
	 * @return 计算后的日期
	 */
	public static Date addYears(Date date, int amount) {
		return org.apache.commons.lang.time.DateUtils.addYears(date, amount);
	}

	/**
	 * 日期计算，小时加减
	 * 
	 * @param date
	 *            初始日期
	 * @param amount
	 *            小时增量（负数为减）
	 * @return 计算后的日期
	 */
	public static Date addHours(Date date, int amount) {
		return org.apache.commons.lang.time.DateUtils.addHours(date, amount);
	}

	/**
	 * 日期计算，分钟加减
	 * 
	 * @param date
	 *            初始日期
	 * @param amount
	 *            分钟增量（负数为减）
	 * @return 计算后的日期
	 */
	public static Date addMinutes(Date date, int amount) {
		return org.apache.commons.lang.time.DateUtils.addMinutes(date, amount);
	}

	/**
	 * 日期计算，秒加减
	 * 
	 * @param date
	 *            初始日期
	 * @param amount
	 *            秒增量（负数为减）
	 * @return 计算后的日期
	 */
	public static Date addSeconds(Date date, int amount) {
		return org.apache.commons.lang.time.DateUtils.addSeconds(date, amount);
	}

	/**
	 * 根据指定格式，返回日期时间字符串
	 * 
	 * @param date
	 *            日期变量
	 * @param pattern
	 *            日期格式
	 * @return 日期时间字符串
	 */
	public static String getDateStr(Date date, String pattern) {
		DateFormat df = new SimpleDateFormat(pattern);
		return df.format(date);
	}

	/**
	 * 输出时间String(默认格式)
	 * 
	 * @param date
	 *            日期
	 * @return 默认格式化的日期
	 */
	public static String getTimeStr(Date date) {
		return getDateStr(date, DEFAULT_TIME_PATTERN);
	}

	/**
	 * 取指定日期所在月的第一天的日期
	 * 
	 * @param date
	 *            指定的日期
	 * @return 指定日期所在月的第一天
	 */
	public static Date getFirstDayOfMonth(Date date) {
		Calendar cal = getCalendar(date);
		cal.set(Calendar.DATE, 1);
		return cal.getTime();
	}

	/**
	 * 取指定日期所在月的最后一天的日期
	 * 
	 * @param date
	 *            指定的日期
	 * @return 指定日期所在月的最后一天
	 */
	public static Date getLastDayOfMonth(Date date) {
		Date nextMonth = addMonths(date, 1);
		Date firstDayOfNextMonth = getFirstDayOfMonth(nextMonth);
		return addDays(firstDayOfNextMonth, -1);
	}

	/**
	 * 取指定日期所在年的第一天的日期
	 * 
	 * @param date
	 *            指定的日期
	 * @return 指定日期所在年的第一天
	 */
	public static Date getFirstDayOfYear(Date date) {
		Calendar cal = getCalendar(date);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, 0);
		return cal.getTime();
	}

	/**
	 * 取指定日期所在年的最后一天的日期
	 * 
	 * @param date
	 *            指定的日期
	 * @return 指定日期所在月的最后一天
	 */
	public static Date getLastDayOfYear(Date date) {
		Date nextMonth = addYears(date, 1);
		Date firstDayOfNextYear = getFirstDayOfYear(nextMonth);
		return addDays(firstDayOfNextYear, -1);
	}

	/**
	 * 取指定日期所在周的指定天的日期
	 * 
	 * @param date
	 *            指定的日期
	 * @param day
	 *            指定的天（星期几）
	 * @param firstDay
	 *            一星期的起始天
	 * @return 指定周星期日的日期
	 */
	public static Date getDayInWeek(Date date, int day, int firstDay) {
		Calendar cal = getCalendar(date);
		cal.setFirstDayOfWeek(firstDay);
		cal.set(Calendar.DAY_OF_WEEK, day);
		return cal.getTime();
	}

	/**
	 * 根据Date型的日期，取Calendar型的日期
	 * 
	 * @param date
	 *            Date型的日期
	 * @return Calendar型的日期
	 */
	public static Calendar getCalendar(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	/**
	 * 日期比较（精确到天），date1晚于date2
	 * 
	 * @param date1
	 *            日期1
	 * @param date2
	 *            日期2
	 * @return date1晚于date2，返回true，否则返回false
	 */
	public static boolean later(Date date1, Date date2) {
		boolean result = false;
		if (1 == compare(date1, date2, ACCURACY_DAY)) {
			result = true;
		}
		return result;
	}

	/**
	 * 日期比较（精确到天），date1早于date2
	 * 
	 * @param date1
	 *            日期1
	 * @param date2
	 *            日期2
	 * @return date1早于date2，返回true，否则返回false
	 */
	public static boolean earlier(Date date1, Date date2) {
		boolean result = false;
		if (-1 == compare(date1, date2, ACCURACY_DAY)) {
			result = true;
		}
		return result;
	}

	/**
	 * 日期比较（精确到天），date1等于date2
	 * 
	 * @param date1
	 *            日期1
	 * @param date2
	 *            日期2
	 * @return date1等于date2，返回true，否则返回false
	 */
	public static boolean equal(Date date1, Date date2) {
		boolean result = false;
		if (0 == compare(date1, date2, ACCURACY_DAY)) {
			result = true;
		}
		return result;
	}

	/**
	 * 根据指定规则比较日期，date1晚于date2
	 * 
	 * @param date1
	 *            日期1
	 * @param date2
	 *            日期2
	 * @param accuracy
	 *            日期精度
	 * @return date1晚于date2，返回true，否则返回false
	 */
	public static boolean later(Date date1, Date date2, int accuracy) {
		boolean result = false;
		if (1 == compare(date1, date2, accuracy)) {
			result = true;
		}
		return result;
	}

	/**
	 * 根据指定规则比较日期，date1早于date2
	 * 
	 * @param date1
	 *            日期1
	 * @param date2
	 *            日期2
	 * @param accuracy
	 *            日期精度
	 * @return date1早于date2，返回true，否则返回false
	 */
	public static boolean earlier(Date date1, Date date2, int accuracy) {
		boolean result = false;
		if (-1 == compare(date1, date2, accuracy)) {
			result = true;
		}
		return result;
	}

	/**
	 * 根据指定规则比较日期，date1等于date2
	 * 
	 * @param date1
	 *            日期1
	 * @param date2
	 *            日期2
	 * @param accuracy
	 *            日期精度
	 * @return date1等于date2，返回true，否则返回false
	 */
	public static boolean equal(Date date1, Date date2, int accuracy) {
		boolean result = false;
		if (0 == compare(date1, date2, accuracy)) {
			result = true;
		}
		return result;
	}

	/**
	 * 根据指定规则，比较日期
	 * 
	 * @param date1
	 *            日期1
	 * @param date2
	 *            日期2
	 * @param accuracy
	 *            日期精度
	 * @return int型，date1晚，返回1；date1早，返回-1；相等，返回0
	 */
	public static int compare(Date date1, Date date2, int accuracy) {
		String pattern = DEFAULT_DATE_PATTERN;
		switch (accuracy) {
		case ACCURACY_YEAR:
			pattern = ACCURACY_PATTERN_YEAR;
			break;
		case ACCURACY_MONTH:
			pattern = ACCURACY_PATTERN_MONTH;
			break;
		case ACCURACY_DAY:
			pattern = ACCURACY_PATTERN_DAY;
			break;
		case ACCURACY_HOUR:
			pattern = ACCURACY_PATTERN_HOUR;
			break;
		case ACCURACY_MINUTE:
			pattern = ACCURACY_PATTERN_MINUTE;
			break;
		case ACCURACY_SECOND:
			pattern = ACCURACY_PATTERN_SECOND;
			break;
		default:
			break;
		}
		Date formatedDate1 = transDateFormat(date1, pattern);
		Date formatedDate2 = transDateFormat(date2, pattern);
		return formatedDate1.compareTo(formatedDate2);
	}
	/**
	 * 根据指定规则，比较日期
	 * 
	 * @param date1
	 *            日期1
	 * @param date2
	 *            日期2
	 * @param accuracy
	 *            日期精度
	 * @return int型，date1晚，返回1；date1早，返回-1；相等，返回0
	 * @throws ParseException 
	 */
	public static int compare(String date1, String date2, int accuracy) throws ParseException {
		String pattern = DEFAULT_DATE_PATTERN;
		switch (accuracy) {
		case ACCURACY_YEAR:
			pattern = ACCURACY_PATTERN_YEAR;
			break;
		case ACCURACY_MONTH:
			pattern = ACCURACY_PATTERN_MONTH;
			break;
		case ACCURACY_DAY:
			pattern = ACCURACY_PATTERN_DAY;
			break;
		case ACCURACY_HOUR:
			pattern = ACCURACY_PATTERN_HOUR;
			break;
		case ACCURACY_MINUTE:
			pattern = ACCURACY_PATTERN_MINUTE;
			break;
		case ACCURACY_SECOND:
			pattern = ACCURACY_PATTERN_SECOND;
			break;
		default:
			break;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE);
		Date formatedDate1 = transDateFormat(sdf.parse(date1), pattern);
		Date formatedDate2 = transDateFormat(sdf.parse(date2), pattern);
		return formatedDate1.compareTo(formatedDate2);
	}
	/**
	 * 根据指定规则，转化日期，如只取年、取年月等
	 * 
	 * @param date
	 *            待转化日期
	 * @param pattern
	 *            日期格式
	 * @return 转化后的日期
	 */
	public static Date transDateFormat(Date date, String pattern) {
		String dateStr = getDateStr(date, pattern);
		return parseDate(dateStr, pattern);
	}

	/**
	 * 返回时定时间的年
	 * 
	 * @param date
	 *            日期
	 * @return String型的年
	 */
	public static String getYear(Date date) {
		return getDateStr(date, SINGLE_YEAR);
	}

	/**
	 * 返回时定时间的月
	 * 
	 * @param date
	 *            日期
	 * @return String型的月
	 */
	public static String getMonth(Date date) {
		return getDateStr(date, SINGLE_MONTH);
	}

	/**
	 * 返回时定时间的日
	 * 
	 * @param date
	 *            日期
	 * @return String型的日
	 */
	public static String getDay(Date date) {
		return getDateStr(date, SINGLE_DAY);
	}

	/**
	 * 返回时定时间的小时
	 * 
	 * @param date
	 *            日期
	 * @return String型的小时
	 */
	public static String getHour(Date date) {
		return getDateStr(date, SINGLE_HOUR);
	}

	/**
	 * 返回时定时间的分
	 * 
	 * @param date
	 *            日期
	 * @return String型的分
	 */
	public static String getMinute(Date date) {
		return getDateStr(date, SINGLE_MINUTE);
	}

	/**
	 * 返回时定时间的秒
	 * 
	 * @param date
	 *            日期
	 * @return String型的秒
	 */
	public static String getSecond(Date date) {
		return getDateStr(date, SINGLE_SECOND);
	}

	/**
	 * 将时间日期变量的年份变为指定年, 如果日期不存在，则向后一天，如20102月
	 * 
	 * @param date
	 *            日期时间变量
	 * @param amount
	 *            指定年
	 * @return 修改后的日期变量
	 */
	public static Date setYear(Date date, int amount) {
		Calendar cal = getCalendar(date);
		cal.set(Calendar.YEAR, amount);
		return cal.getTime();
	}

	/**
	 * 将时间日期变量的月份变为指定月
	 * 
	 * @param date
	 *            日期时间变量
	 * @param amount
	 *            指定月
	 * @return 修改后的日期变量
	 */
	public static Date setMonth(Date date, int amount) {
		Calendar cal = getCalendar(date);
		cal.set(Calendar.MONTH, amount - 1);
		return cal.getTime();
	}

	/**
	 * 将时间日期变量的年份变为指定日
	 * 
	 * @param date
	 *            日期时间变量
	 * @param amount
	 *            指定日
	 * @return 修改后的日期变量
	 */
	public static Date setDay(Date date, int amount) {
		Calendar cal = getCalendar(date);
		cal.set(Calendar.DAY_OF_MONTH, amount);
		return cal.getTime();
	}

	/**
	 * 将时间日期变量的小时变为指定时
	 * 
	 * @param date
	 *            日期时间变量
	 * @param amount
	 *            指定时
	 * @return 修改后的日期变量
	 */
	public static Date setHour(Date date, int amount) {
		Calendar cal = getCalendar(date);
		cal.set(Calendar.HOUR_OF_DAY, amount);
		return cal.getTime();
	}

	/**
	 * 将时间日期变量的分钟变为指定分
	 * 
	 * @param date
	 *            日期时间变量
	 * @param amount
	 *            指定分
	 * @return 修改后的日期变量
	 */
	public static Date setMinute(Date date, int amount) {
		Calendar cal = getCalendar(date);
		cal.set(Calendar.MINUTE, amount);
		return cal.getTime();
	}

	/**
	 * 将时间日期变量的秒变为指定秒
	 * 
	 * @param date
	 *            日期时间变量
	 * @param amount
	 *            指定秒
	 * @return 修改后的日期变量
	 */
	public static Date setSecond(Date date, int amount) {
		Calendar cal = getCalendar(date);
		cal.set(Calendar.SECOND, amount);
		return cal.getTime();
	}

	/**
	 * 根据制定单位，计算两个日期之间的天数差
	 * 
	 * @param a
	 *            时间点1
	 * @param b
	 *            时间点2
	 * @return 时间差
	 */
	public static int getDateDistance(Date a, Date b) {
		return getDateDistance(a, b, ACCURACY_DAY);
	}

	/**
	 * 根据制定单位，计算两个日期之间的差
	 * 
	 * @param a
	 *            时间点1
	 * @param b
	 *            时间点2
	 * @param unit
	 *            时间单位
	 * @return 时间差
	 */
	public static int getDateDistance(Date a, Date b, int unit) {
		int result = 0;
		if (null != a && null != b) {
			String pattern = null;
			switch (unit) {
			case ACCURACY_HOUR: // '\003'
				pattern = "yyyyMMddHH";
				break;
			case ACCURACY_MINUTE: // '\002'
				pattern = "yyyyMMddHHmm";
				break;
			case ACCURACY_SECOND: // '\001'
				pattern = "yyyyMMddHHmmss";
				break;
			default:
				pattern = "yyyyMMdd";
			}
			Date startDate = transDateFormat(1 != a.compareTo(b) ? a : b,
					pattern);
			Date endDate = transDateFormat(1 != a.compareTo(b) ? b : a, pattern);
			if (1 <= unit && 4 >= unit) {
				result = getDistanceByUnit(startDate, endDate, unit);
				return result;
			}
			GregorianCalendar startCalendar = new GregorianCalendar();
			startCalendar.setTime(startDate);
			int startYears = startCalendar.get(Calendar.YEAR);
			int startMonths = startCalendar.get(Calendar.MONTH);
			int startDays = startCalendar.get(Calendar.DAY_OF_MONTH);

			GregorianCalendar endCalendar = new GregorianCalendar();
			endCalendar.setTime(endDate);
			int endYears = endCalendar.get(Calendar.YEAR);
			int endMonths = endCalendar.get(Calendar.MONTH);
			int endDays = endCalendar.get(Calendar.DAY_OF_MONTH);

			int yearBetween = endYears - startYears;
			int monthBetween = endMonths - startMonths;
			if (endDays < startDays
					&& endDays != endCalendar.getActualMaximum(Calendar.DATE)) {
				monthBetween--;
			}
			if (ACCURACY_YEAR == unit) {
				if (monthBetween < 0) {
					yearBetween--;
				}
				result = yearBetween;
			}
			if (ACCURACY_MONTH == unit) {
				result = (yearBetween * 12 + monthBetween);
			}
		}
		return result;

	}

	/**
	 * 内部方法，计算时间点的差距
	 * 
	 * @param startDate
	 *            起始时间
	 * @param endDate
	 *            终止时间
	 * @param unit
	 *            时间单位
	 * @return 时间差
	 */
	public static int getDistanceByUnit(Date startDate, Date endDate, int unit) {
		int result = 0;
		long millisecondPerUnit = MILLISECONDS_PER_DAY;
		switch (unit) {
		case ACCURACY_HOUR:
			millisecondPerUnit = MILLISECONDS_PER_HOUR;
			break;
		case ACCURACY_MINUTE:
			millisecondPerUnit = MILLISECONDS_PER_MINUTE;
			break;
		case ACCURACY_SECOND:
			millisecondPerUnit = MILLISECONDS_PER_SECOND;
			break;
		default:
			break;
		}
		long start = startDate.getTime();
		long end = endDate.getTime();
		long distance = end - start;
		result = Integer.valueOf((distance / millisecondPerUnit) + "");
		return result;
	}
	
	/**
	 * 内部方法，计算时间点的差距toLong
	 * 
	 * @param startDate
	 *            起始时间
	 * @param endDate
	 *            终止时间
	 * @param unit
	 *            时间单位
	 * @return 时间差
	 */
	public static long getDistanceByUnit_toLong(Date startDate, Date endDate, int unit) {
		long result = 0;
		long millisecondPerUnit = MILLISECONDS_PER_DAY;
		switch (unit) {
		case ACCURACY_HOUR:
			millisecondPerUnit = MILLISECONDS_PER_HOUR;
			break;
		case ACCURACY_MINUTE:
			millisecondPerUnit = MILLISECONDS_PER_MINUTE;
			break;
		case ACCURACY_SECOND:
			millisecondPerUnit = MILLISECONDS_PER_SECOND;
			break;
		default:
			break;
		}
		long start = startDate.getTime();
		long end = endDate.getTime();
		long distance = end - start;
		result = distance / millisecondPerUnit;
		return result;
	}
	
	 

	/**
	 * 返回指定日期是当年的第几周
	 * 
	 * @param date
	 *            指定日期
	 * @return 周数（从1开始）
	 */
	public static int getWeekOfYear(Date date) {
		return getCalendar(date).get(Calendar.WEEK_OF_YEAR);
	}

	/**
	 * 获取指定日期是星期几
	 * 
	 * @param date
	 *            指定日期
	 * @return 星期日--1; 星期一--2; 星期二--3; 星期三--4; 星期四--5; 星期五--6; 星期六--7;
	 */
	public static int getWeekOfDate(Date date) {
		return getCalendar(date).get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * 判断指定年份日期的年份是否为闰年
	 * 
	 * @param date
	 *            日期
	 * @return 闰年ture，非闰年false
	 */
	public static boolean isLeapYear(Date date) {
		int year = getCalendar(date).get(Calendar.YEAR);
		return isLeapYear(year);
	}

	/**
	 * 判断指定年份日期的年份是否为闰年
	 * 
	 * @param year
	 *            年份数字
	 * @return 闰年ture，非闰年false
	 */
	public static boolean isLeapYear(int year) {
		if ((year % 400) == 0) {
			return true;
		} else if ((year % 4) == 0) {
			if ((year % 100) == 0) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * 按照strFormat格式输出当前时间
	 * 
	 * @param strFormat
	 *            格式
	 * @return 指定格式的当前系统日期
	 */
	public static String getCurrentDate(String strFormat) {
		return getDateStr(getCurrentDate(), strFormat);
	}

	/**
	 * 校验日期数据（校验输入值是否为指定的日期格式）
	 * 
	 * @param strDate
	 *            要校验的日期
	 * @param strFormat
	 *            日期格式
	 * @return true/false （符合/不符合）
	 */
	public static boolean checkDate(String strDate, String strFormat) {
		Date date = null;
		if ((strDate != null) && (strDate.trim().length() != 0)) {
			DateFormat myDateFmt = new SimpleDateFormat(strFormat);
			try {
				date = myDateFmt.parse(strDate);

				if (!strDate.equals(myDateFmt.format(date))) {
					date = null;
					return false;
				}
			} catch (ParseException e) {
				date = null;
				return false;
			}
		}
		return true;
	}
	/**
	 * 
	 * @Description:获取当期系统日期以特定格式显示
	 * @param 
	 * @return 格式为"yyyy-MM-dd HH:mm:ss"的时间
	 * @throws 
	 * @author baolf
	 * @date 2016-7-25
	 */
	public static String getNowTime(){
		Date now = new Date();
		SimpleDateFormat sd = new SimpleDateFormat(DEFAULT_DATE);
		String date = sd.format(now);
		return date;
	}
	/**
	 * 
	 * @Description:获取当期系统两小时后时间   以特定格式显示
	 * @param 
	 * @return 格式为"yyyy-MM-dd HH:mm:ss"的时间
	 * @throws 
	 * @author wwq
	 * @date 2018-08-13
	 */
	public static String get2HoursLater(){
		Date twoHours = new Date(System.currentTimeMillis()+120 * 60 * 1000);
		SimpleDateFormat sd = new SimpleDateFormat(DEFAULT_DATE);
		String date = sd.format(twoHours);
		return date;
	}
	/**
	 * 
	 * @Description:获取当期系统日期以特定格式显示
	 * @param 
	 * @return 格式为"yyyy-MM-dd"的时间
	 * @throws 
	 * @author
	 * @date 2016-7-25
	 */
	public static String getNowTime1(){
		Date now = new Date();
		SimpleDateFormat sd = new SimpleDateFormat(DEFAULT_DATE_PATTERN);
		String date = sd.format(now);
		return date;
	}
	
	/**
	 * 自定义时间格式
	 * @param strFormat
	 * @return
	 */
	public static String getNowTime(String strFormat){
		Date now = new Date();
		SimpleDateFormat sd = new SimpleDateFormat(strFormat);
		String date = sd.format(now);
		return date;
	}
	
    /**
     * 时间串，没有分割符
     * 用于自动生成文件名用
     * @return
     */
	public static synchronized String getFileNameNowTime(){
		Date now = new Date();
		SimpleDateFormat sd = new SimpleDateFormat(DEFAULT_FILE_DATE);
		String date = sd.format(now);
		return date;
	}
	
	/**
	 * 获得几位随机数(正数)
	 * @param num
	 * @return
	 */
	public static String getRandom(int num)
	{
		StringBuffer format = new StringBuffer("0.0");
		for(int ii=2;ii<=num;ii++)
		{
			format.append("0");
		}
		DecimalFormat df = new DecimalFormat(format.toString());
		return df.format(Math.random()).replace("0.","");
		
	}
	public static String getReleaseBatch(){
		Date now = new Date();
		SimpleDateFormat sd = new SimpleDateFormat(DEFAULT_DATE_PATTERN);
		String date = sd.format(now);
		date = date.replace("-", "");
		return date;
	}
//	public static void main(String[] args) {
//	}
	public static String getBeforeNowTime(){
		long time = 1*60*1000+10000;//1分钟
		Date now = new Date(new Date().getTime()-time);
		SimpleDateFormat sd = new SimpleDateFormat(DEFAULT_DATE);
		String date = sd.format(now);
		return date;
	}
	/**
	 * 获取秒数  (1970-01-01 08:00:00)
	 * @Description :
	 * @param : 
	 * @throws ： 
	 * @author ：
	 */
	public static  int getDateInt(String date)
	{		
		return getDistanceByUnit( 
				   parseDate("1970-01-01 08:00:00", DEFAULT_DATE),
				   parseDate(date, DEFAULT_DATE),1) ;
	}
	/**
	 * 判断字符串是否不为空 空返回false 非空返回true
	 *
	 * @param sourceStr
	 * @return
	 */
	public static boolean isNotEmpty(String sourceStr) {

		if (sourceStr == null) {
			return false;
		}
		if (sourceStr.trim().length() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 统计两个时间的时间差
	 * 相差几秒几毫秒
	 */
	public static String getDistanceTime(String str1, String str2) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		Date one;
		Date two;
		long day = 0;//天数差
		long hour = 0;//小时数差
		long min = 0;//分钟数差
		long second=0;//秒数差
		long diff=0 ;//毫秒差
		String result = null;
		try {
			final Calendar c = Calendar.getInstance();
			c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
			one = df.parse(str1);
			c.setTime(one);
			two = df.parse(str2);
			long time1 = one.getTime();
			long time2 = two.getTime();
			diff = time2 - time1;
			day = diff / (24 * 60 * 60 * 1000);
			hour = (diff / (60 * 60 * 1000) - day * 24);
			min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
			second = diff/1000;
			//System.out.println("day="+day+" hour="+hour+" min="+min+" ss="+second%60+" SSS="+diff%1000);
			result=second%60+"秒"+diff%1000+"毫秒";
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return String.valueOf(diff);
	}

	/*public static void main(String[] args) {
		//开始时间
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		String beginTime = df.format(new Date());
		String endTime = df.format(new Date());
		System.out.println("时间差："+getDistanceTime(beginTime,endTime));
	}*/

	/*
	 * 将yyyyMMddHHmmss格式的数据装换成yyyy-MM-dd:HH:mm格式的数据
	 * */
	public static String formatDate(String dateStr) throws NullPointerException {
		//
		StringBuffer dateBuffer = new StringBuffer();
		return dateBuffer.append(dateStr.substring(0, 4)).append("-").append(dateStr.substring(4, 6)).append("-").append(dateStr.substring(6, 8))
				.append(" ").append(dateStr.substring(8, 10)).append(":").append(dateStr.substring(10, 12)).append(":").append(dateStr.substring(12, 14)).toString();
	}
	
}
