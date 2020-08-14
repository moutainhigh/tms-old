package org.nw.vo.pub.lang;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class UFDate implements java.io.Serializable, Comparable {
	static final long serialVersionUID = -1037968151602108293L;

	private String value = null;

	private static final long millisPerDay = 24 * 60 * 60 * 1000;

	private static final int LRUSIZE = 500;

	private static class LRUMap<K, V> extends LinkedHashMap<K, V> {

		private static final long serialVersionUID = 1L;

		public LRUMap(int initSize) {
			super(initSize, 1f, true);
		}

		protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
			if (size() > LRUSIZE)
				return true;
			else
				return false;
		}
	}

	// 512 is fix the bug of jdk to avoid transfer
	private final static Map<Object, UFDate> allUsedDate = new LRUMap<Object, UFDate>(
			512);

	private static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	private transient Long currentLong = null;

	/**
	 * UFDate 构造子注释。
	 */
	public UFDate() {
		this(new Date());
	}

	/**
	 * 以从1970年1月1日0时0分0秒到现在的毫秒数来构造日期
	 * 
	 * @param m
	 *            long
	 */
	public UFDate(long m) {
		Calendar cal = Calendar.getInstance(TimeZone
				.getTimeZone("Asia/Shanghai"));
		cal.setTimeInMillis(m);
		value = toDateString(cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
	}

	/**
	 * 功能说明：用yyyy-MM-dd形式的字符串构造日期类型
	 */
	public UFDate(String strDate) {
		this(strDate, true);
	}

	/**
	 * 功能说明：如果解析，用yyyy-MM-dd形式的字符串构造日期类型 如果不解析，可能是yyyy，yyyy-MM 创建日期：(2001-9-18
	 * 10:03:53)
	 * 
	 * @param strDate
	 *            java.lang.String
	 * @param isParse
	 *            boolean
	 */
	public UFDate(String strDate, boolean isParse) {
		if (isParse) {
			value = internalParse(strDate);
		} else {
			if (strDate == null || strDate.trim().length() != 10) {
				throw new IllegalArgumentException("invalid UFDate:" + strDate);
			}
			value = strDate.trim();
		}
	}

	/**
	 * /////////////////////////////////////////////////////////
	 * 功能说明：用java.sql.Date类型构造UF日期类型
	 * 
	 * @param date
	 *            java.sql.Date
	 */
	public UFDate(java.sql.Date date) {
		this((java.util.Date) date);
	}

	/**
	 * 功能说明：用java.util.Date类型构造日期类型
	 */
	public UFDate(java.util.Date date) {
		value = toDateString(date);
	}

	/**
	 * 比较日期先后，对象日期在参数日期之后为true
	 */
	public boolean after(UFDate when) {
		return this.compareTo(when) > 0;
	}

	/**
	 * 比较日期先后，对象日期在参数日期之前为true
	 */
	public boolean before(UFDate when) {
		return this.compareTo(when) < 0;
	}

	/**
	 * 克隆日期兑对象。
	 * 
	 * @return nc.vo.pub.lang.UFDate
	 */
	public Object clone() {
		return new UFDate(value);
	}

	/**
	 * 返回日期先后： 大于0 ---为参数之后日期 等于0 ---和参数为同一天 小于0 ---为参数之前日期
	 */
	public int compareTo(UFDate when) {
		return compareTo(when.getMillis());
	}

	/**
	 * 返回日期先后： 大于0 ---为参数之后日期 等于0 ---和参数为同一天 小于0 ---为参数之前日期
	 */
	private int compareTo(Long whenLong) {
		long retl = this.getMillis() - whenLong;
		if (retl == 0)
			return 0;
		else
			return retl > 0 ? 1 : -1;
	}

	/**
	 * 比较日期先后，true为同一天
	 */
	public boolean equals(Object o) {
		if ((o != null) && (o instanceof UFDate)) {
			return this.getMillis() == ((UFDate) o).getMillis();
		}
		return false;
	}

	public static UFDate getDate(long d) {

		return getDate(d, false);
	}

	public static UFDate getDate(String strDate) {
		if (strDate == null || strDate.trim().length() == 0)
			return null;
		return getDate(strDate, true);
	}

	public static UFDate getDate(Date date) {
		String strDate = toDateString(date);
		return getDate(strDate, false);
	}

	public static UFDate getDate(String date, boolean check) {
		return getDate((Object) date, check);
	}

	public static UFDate getDate(Long date) {
		return getDate((Object) date, false);
	}

	private static UFDate getDate(Object date, boolean check) {
		if (date instanceof Long || date instanceof String) {
			//for performance
			if (rwl.readLock().tryLock()) {
				try {
					UFDate o = (UFDate) allUsedDate.get(date);
					if (o == null) {
						UFDate n = toUFDate(date, check);
						rwl.readLock().unlock();
						rwl.writeLock().lock();
						try {
							o = allUsedDate.get(date);
							if (o == null) {
								o = n;
								allUsedDate.put(date, o);
							}
						} finally {
							rwl.readLock().lock();
							rwl.writeLock().unlock();
						}
					}
					return o;
				} finally {
					rwl.readLock().unlock();
				}
			} else {
				return toUFDate(date, check);
			}
		} else {
			throw new IllegalArgumentException(
					"expect long or string parameter as the first parameter");
		}
	}

	private static UFDate toUFDate(Object date, boolean check) {
		if (date instanceof String)
			return new UFDate((String) date, check);
		else
			return new UFDate((Long) date);
	}

	/**
	 * 返回天数后的日期。
	 * 
	 * @param days
	 *            int
	 */
	public UFDate getDateAfter(int days) {
		long l = getMillis() + millisPerDay * days;
		Date date = new Date(l);
		return getDate(date);

	}

	/**
	 * 返回天数前的日期。
	 * 
	 * @param days
	 *            int
	 */
	public UFDate getDateBefore(int days) {
		return getDateAfter(-days);
	}

	/**
	 * 在此处插入方法说明。 创建日期：(00-7-10 14:54:26)
	 * 
	 * @return int
	 */
	public int getDay() {
		return Integer.parseInt(value.substring(8, 10));
	}

	/**
	 * 返回某一日期距今天数，负数表示在今天之后
	 * 
	 * @return int
	 * @param when
	 *            UFDate
	 */
	public int getDaysAfter(UFDate when) {
		int days = 0;
		if (when != null) {
			days = (int) ((this.getMillis() - when.getMillis()) / millisPerDay);
		}
		return days;
	}

	/**
	 * 返回后一日期距前一日期之后后的天数
	 * 
	 * @return int
	 * @param begin
	 *            UFDate
	 * @param end
	 *            UFDate
	 */
	public static int getDaysBetween(UFDate begin, UFDate end) {
		int days = 0;
		if (begin != null && end != null) {
			days = (int) ((end.getMillis() - begin.getMillis()) / millisPerDay);
		}
		return days;
	}

	public int getDaysMonth() {
		return getDaysMonth(getYear(), getMonth());
	}

	public static int getDaysMonth(int year, int month) {
		switch (month) {
		case 1:
			return 31;
		case 2:
			if (isLeapYear(year))
				return 29;
			else
				return 28;
		case 3:
			return 31;
		case 4:
			return 30;
		case 5:
			return 31;
		case 6:
			return 30;
		case 7:
			return 31;
		case 8:
			return 31;
		case 9:
			return 30;
		case 10:
			return 31;
		case 11:
			return 30;
		case 12:
			return 31;
		default:
			return 30;
		}
	}

	public String getEnMonth() {
		switch (getMonth()) {
		case 1:
			return "Jan";
		case 2:
			return "Feb";
		case 3:
			return "Mar";
		case 4:
			return "Apr";
		case 5:
			return "May";
		case 6:
			return "Jun";
		case 7:
			return "Jul";
		case 8:
			return "Aug";
		case 9:
			return "Sep";
		case 10:
			return "Oct";
		case 11:
			return "Nov";
		case 12:
			return "Dec";
		}
		return null;
	}

	/**
	 * 在此处插入方法说明。 创建日期：(00-12-18 20:57:29)
	 * 
	 * @return java.lang.String
	 */
	public String getEnWeek() {
		switch (getWeek()) {
		case 0:
			return "Sun";
		case 1:
			return "Mon";
		case 2:
			return "Tue";
		case 3:
			return "Wed";
		case 4:
			return "Thu";
		case 5:
			return "Fri";
		case 6:
			return "Sat";
		}
		return null;
	}

	/**
	 * 在此处插入方法说明。 创建日期：(00-7-10 14:53:44)
	 * 
	 * @return int
	 */
	public int getMonth() {
		return Integer.parseInt(value.substring(5, 7));
	}

	public String getStrDay() {
		return value.substring(8, 10);
	}

	public String getStrMonth() {
		return value.substring(5, 7);
	}

	/**
	 * 在此处插入方法说明。 创建日期：(00-12-18 20:49:02)
	 * 
	 * 原理 1980-1-6是星期日
	 * 
	 * @return int Sunday-Monday-Saturday 0-6
	 */
	public int getWeek() {
		int days = getDaysAfter(new UFDate("1980-01-06"));
		int week = days % 7;
		if (week < 0)
			week += 7;
		return week;
	}

	/**
	 * 获得年的数值。 创建日期：(00-7-10 14:52:13)
	 * 
	 * @return int
	 */
	public int getYear() {
		return Integer.parseInt(value.substring(0, 4));
	}

	/**
	 * 是否闰年。
	 * 
	 * @return boolean
	 */
	public boolean isLeapYear() {
		return isLeapYear(getYear());
	}

	/**
	 * 是否闰年。
	 * 
	 * @return boolean
	 * @param year
	 *            int
	 */
	public static boolean isLeapYear(int year) {
		if ((year % 4 == 0) && (year % 100 != 0 || year % 400 == 0))
			return true;
		else
			return false;
	}

	public String toString() {
		return value == null ? "" : value;
	}

	public int compareTo(Object o) {
		if (o instanceof UFDate)
			return compareTo((UFDate) o);
		else if (o instanceof UFDateTime)
			return compareTo(((UFDateTime) o).getMillis());
		else
			throw new IllegalArgumentException();
	}

	/**
	 * 得到毫秒数。
	 * <p>
	 * 创建日期：(2006-1-11 10:24:00)
	 * 
	 * @return long
	 */
	public long getMillis() {
		if (currentLong == null) {
			currentLong = new java.util.GregorianCalendar(getYear(),
					getMonth() - 1, getDay()).getTimeInMillis();

		}
		return currentLong;

	}

	/**
	 * 返加当前日期在一年内的周数。
	 * 
	 * @return
	 */
	public int getWeekOfYear() {
		GregorianCalendar calendar = new GregorianCalendar(getYear(),
				getMonth(), getDay());
		return calendar.get(Calendar.WEEK_OF_YEAR);
	}

	/**
	 * 转换为{@link java.util.Date}。
	 * 
	 * @return
	 */
	public Date toDate() {
		return new Date(getMillis());
	}

	/*
	 * fangj 进行序列化增加修改 （非 Javadoc）
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return value == null ? 17 : value.hashCode();
	}

	private static String internalParse(String sDate) {

		if (sDate == null)
			throw new IllegalArgumentException("invalid UFDate: " + sDate);

		sDate = sDate.trim();
		String[] tokens = new String[3];

		StringTokenizer st = new StringTokenizer(sDate, "-/.");

		if (st.countTokens() != 3) {
			throw new IllegalArgumentException("invalid UFDate: " + sDate);
		}

		int i = 0;
		while (st.hasMoreTokens()) {
			tokens[i++] = st.nextToken().trim();
		}

		try {
			int year = Integer.parseInt(tokens[0]);
			int month = Integer.parseInt(tokens[1]);
			if (month < 1 || month > 12)
				throw new IllegalArgumentException("invalid UFDate: " + sDate);
			int day = Integer.parseInt(tokens[2]);

			int MONTH_LENGTH[] = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30,
					31 };
			int LEAP_MONTH_LENGTH[] = { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31,
					30, 31 };
			int daymax = isLeapYear(year) ? LEAP_MONTH_LENGTH[month - 1]
					: MONTH_LENGTH[month - 1];

			if (day < 1 || day > daymax)
				throw new IllegalArgumentException("invalid ufdate: " + sDate);

			String strYear = tokens[0];
			for (int j = strYear.length(); j < 4; j++) {
				if (j == 3) {
					strYear = "2" + strYear;
				} else {
					strYear = "0" + strYear;
				}
			}

			String strMonth = String.valueOf(month);
			if (strMonth.length() < 2)
				strMonth = "0" + strMonth;
			String strDay = String.valueOf(day);
			if (strDay.length() < 2)
				strDay = "0" + strDay;
			return strYear + "-" + strMonth + "-" + strDay;
		} catch (Throwable thr) {
			if (thr instanceof IllegalArgumentException) {
				throw (IllegalArgumentException) thr;
			} else {
				throw new IllegalArgumentException("invalid ufdate: " + sDate);
			}
		}

	}

	private static String toDateString(int year, int month, int day) {
		String strYear = String.valueOf(year);
		for (int j = strYear.length(); j < 4; j++)
			strYear = "0" + strYear;
		String strMonth = String.valueOf(month);
		if (strMonth.length() < 2)
			strMonth = "0" + strMonth;
		String strDay = String.valueOf(day);
		if (strDay.length() < 2)
			strDay = "0" + strDay;
		return strYear + "-" + strMonth + "-" + strDay;

	}

	private static String toDateString(Date dt) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		return toDateString(cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
	}

	/**
	 * 如果字符串的日期能转换成有效日期串－－－转换。 /n 创建日期：(2001-5-28 13:28:29) UIBean使用
	 * 
	 * @return java.lang.String
	 * @param sDate
	 *            java.lang.String
	 */
	public static String getValidUFDateString(String sDate) {
		return internalParse(sDate);
	}

}