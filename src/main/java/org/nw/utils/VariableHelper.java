package org.nw.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nw.basic.util.DateUtils;
import org.nw.web.utils.WebUtils;

/**
 * 系统变量操作类,可以在模板\门户自定义组件中使用,
 * 
 * @author xuqc
 * @date 2011-5-27
 */
public class VariableHelper {

	static Logger logger = Logger.getLogger(VariableHelper.class);

	/**
	 * 系统变量，目前使用在模板的默认值中.
	 */

	public static Map<String, Object> getVariables() {
		/**
		 * 这里定义的key都使用小写，实际使用的时候不分大小写
		 */
		Map<String, Object> sys_variable = new HashMap<String, Object>();
		if(WebUtils.getLoginInfo() != null) {
			sys_variable.put("#sys_corp#", WebUtils.getLoginInfo().getPk_corp());// 当前登陆公司
			sys_variable.put("#sys_user#", WebUtils.getLoginInfo().getPk_user());// 当前用户
			sys_variable.put("#sys_dept#", WebUtils.getLoginInfo().getPk_dept());// 当前部门
			//sys_variable.put("#sys_corp_with_children#", CorpHelper.getCurrentCorpWithChildren2());// 当前公司及下级公司
			//yaojiie 2015 12 08从sission中获取公司信息。
			sys_variable.put("#sys_corp_with_children#", WebUtils.getLoginInfo().getCurrentCorpWithChildren());// 当前公司及下级公司
			// 2015 11 16 yaojiie 增加客户承运商
			sys_variable.put("#sys_customer#", "'"+WebUtils.getLoginInfo().getPk_customer()+"'");// 当前客户
			sys_variable.put("#sys_carrier#", "'"+WebUtils.getLoginInfo().getPk_carrier()+"'");// 当前承运商
		}

		sys_variable.put("#sys_year#", DateUtils.getYear());// 当前年份
		sys_variable.put("#sys_date#", DateUtils.getCurrentDate());// 当前日期
		// 也支持如下的两个变量
		// #sys_daysbefore_i# #sys_daysafter_i#

		sys_variable.put("#sys_firstdayofprevmonth#", getFirstdayofprevmonth());// 返回当前登录时间的上月的第一天
		sys_variable.put("#sys_firstdayofmonth#", getFirstdayofmonth());// 返回当前月的第一天
		sys_variable.put("#sys_lastdayofmonth#", getLastdayofmonth());// 返回当前月的最后一天
		sys_variable.put("#sys_lastmonthdate#", getLastmonthdate());// 返回上个月的今天
		return sys_variable;
	}

	/**
	 * 返回上个月的今天
	 * 
	 * @return
	 */
	public static String getLastmonthdate() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
		return DateUtils.formatDate(cal.getTime(), DateUtils.DATEFORMAT_HORIZONTAL);
	}

	/**
	 * 返回当前登录时间的上月的第一天
	 * 
	 * @return
	 */
	public static String getFirstdayofprevmonth() {
		String date;
		if(WebUtils.getLoginInfo() != null) {
			date = WebUtils.getLoginInfo().getLoginDate();
		} else {
			date = DateUtils.getCurrentDate();
		}
		Date prevmonthDate = DateUtils.addMonth(DateUtils.parseString(date), -1);
		date = DateUtils.formatDate(prevmonthDate, DateUtils.DATEFORMAT_HORIZONTAL);
		date = date.substring(0, 7);
		date += "-01";
		return date;
	}

	/**
	 * 返回当前月的第一天
	 * 
	 * @return
	 */
	public static String getFirstdayofmonth() {
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
	public static String getLastdayofmonth() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int value = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		cal.set(Calendar.DAY_OF_MONTH, value);
		return DateUtils.formatDate(cal.getTime(), DateUtils.DATEFORMAT_HORIZONTAL);
	}

	/**
	 * 当前日期前i天,返回日期
	 * 
	 * @param day
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
	 * 当前日期后i天,返回日期
	 * 
	 * @param day
	 * @return
	 * @author xuqc
	 * @date 2012-7-13
	 */
	public static String getDaysAfter(int day) {
		Date date = new Date();
		date.setTime(date.getTime() + 1000L * 60L * 60L * 24L * day);
		return DateUtils.formatDate(date, DateUtils.DATEFORMAT_HORIZONTAL);
	}

	/**
	 * 根据key从系统变量中查找对应的值，若查不到，返回自身
	 * 
	 * @param key
	 * @return
	 */
	public static String resolve(String key) {
		if(StringUtils.isBlank(key)) {
			// 这里返回null，null值不会序列化到前台，前台不用调用setValue操作
			return null;
		}

		String _key = key.toLowerCase();
		if(VariableHelper.getVariables().get(_key.trim()) != null) { //
			return VariableHelper.getVariables().get(_key.trim()).toString();
		} else {
			if(_key.startsWith("#sys_daysbefore_")) {// #sys_daysbefore_1#
				int index = _key.lastIndexOf("_");
				int day = 0;
				try {
					day = Integer.parseInt(_key.substring(index + 1, _key.length() - 1));
				} catch(Exception e) {
				}
				return getDaysBefore(day);
			} else if(_key.startsWith("#sys_daysafter_")) {// #sys_daysafter_1#
				int index = _key.lastIndexOf("_");
				int day = 0;
				try {
					day = Integer.parseInt(_key.substring(index + 1, _key.length() - 1));
				} catch(Exception e) {
				}
				return getDaysAfter(day);
			}
		}
		// 如果从系统变量中找不到对应的值，则使用其自身
		return key;
	}

	/**
	 * 识别字符串中的系统变量，格式为：#sys_corp#，以及{var}变量，这种变量从map中替换
	 * 
	 * @param str
	 * @param values
	 * @return
	 */
	public static String resolve(String str, Map<String, Object> values) {
		if(str == null) {
			return null;
		}
		Matcher m = Pattern.compile("\\#\\w+\\#").matcher(str);// 系统变量
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			String key = m.group();
			String value = resolve(key);
			if(value == null) {
				logger.warn("没有定义系统变量，变量名称：" + key);
			}
			m.appendReplacement(sb, value == null ? key : value);
		}
		m.appendTail(sb);
		if(values != null) {
			m = Pattern.compile("\\{\\w+\\}").matcher(sb.toString());// {var}
			sb.setLength(0);
			while(m.find()) {
				String key = m.group();
				key = key.substring(1, key.length() - 1);// 去掉{}的key
				Object value = values.get(key);
				if(value == null) {
					logger.warn("没有匹配到变量{" + key + "}。");
				}
				m.appendReplacement(sb, value == null ? "{" + key + "}" : value.toString());
			}
			m.appendTail(sb);
		}

		return sb.toString();
	}

	public static void main(String[] args) {
		Date prevmonthDate = DateUtils.addMonth(DateUtils.parseString("2011-01-01"), -1);
		String date = DateUtils.formatDate(prevmonthDate, DateUtils.DATEFORMAT_HORIZONTAL);
		date = date.substring(0, 7);
		date += "-01";
		System.out.println(date);
		System.out.println(getLastmonthdate());
	}
}
