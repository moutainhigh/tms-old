package org.nw.jf.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.nw.basic.util.DateUtils;
import org.nw.web.utils.WebUtils;

/**
 * 系统变量： 登录日期、当前时间、当前日期、当前公司、登录用户
 * 
 * @author xuqc
 * @date 2013-5-3 上午11:43:55
 */
public class SystemVariable {

	/**
	 * 当前日期前i天
	 * 
	 * @param i
	 * @return
	 * @author xuqc
	 * @date 2012-7-13
	 */
	public static String getDaysBefore(int day) {
		Date date = new Date();
		date.setTime(date.getTime() - 1000L * 60L * 60L * 24L * day);
		return DateUtils.formatDate(date, DateUtils.DATEFORMAT_HORIZONTAL);
	}

	/**
	 * 当前月前i月
	 * 
	 * @param month
	 * @return
	 * @author xuqc
	 * @date 2012-7-13
	 */
	public static String getMonthsBefore(int month) {
		Date dt = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - month);
		return DateUtils.formatDate(cal.getTime(), DateUtils.DATEFORMAT_HORIZONTAL);
	}

	/**
	 * 返回当前登录时间的上月的第一天
	 * 
	 * @return
	 */
	public static String _getfirstdayofprevmon() {
		String date = WebUtils.getLoginInfo().getLoginDate();
		Date prevmonthDate = DateUtils.addMonth(DateUtils.parseString(date), -1);
		date = DateUtils.formatDate(prevmonthDate, DateUtils.DATEFORMAT_HORIZONTAL);
		date = date.substring(0, 7);
		date += "-01";
		return date;
	}

	/**
	 * 返回一个月的第一天
	 * 
	 * @return
	 */
	public static String _getfirstdayofmon() {
		String date = DateUtils.getCurrentDate();
		date = date.substring(0, 7);
		date += "-01";
		return date;
	}

	/**
	 * 返回当前月的最后一天
	 * 
	 * @return
	 */
	public static String _getlastmonthdate() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int value = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		cal.set(Calendar.DAY_OF_MONTH, value);
		return DateUtils.formatDate(cal.getTime(), DateUtils.DATEFORMAT_HORIZONTAL);
	}

	/**
	 * 打印模板的系统变量
	 * 
	 * @return
	 */
	public static Map<String, Object> getPrintVariable() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("LOGINDATE", WebUtils.getLoginInfo().getLoginDate());// 登录日期
		map.put("DATE", DateUtils.getCurrentDate());// 当前日期
		map.put("DATETIME", DateUtils.formatDate(new Date(), DateUtils.DATETIME_FORMAT_HORIZONTAL));// 当前日期和时间
		map.put("TIME", DateUtils.formatDate(new Date(), DateUtils.DATETIME_TIMER));
		map.put("USER", WebUtils.getLoginInfo().getUser_name());
		map.put("CORP", WebUtils.getLoginInfo().getCorp_name());
		map.put("DEPT", WebUtils.getLoginInfo().getDept_name());
		return map;
	}

	public static void main(String[] args) {
		System.out.println(DateUtils.formatDate(new Date(), DateUtils.DATETIME_TIMER));
	}
}
