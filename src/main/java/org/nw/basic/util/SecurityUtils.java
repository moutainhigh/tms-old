package org.nw.basic.util;

import org.nw.web.utils.WebUtils;

/**
 * 提供对系统安全的一些验证
 * 
 * @author wangxf
 * @date 2011-4-19
 */

public class SecurityUtils{
	/**
	 * 验证字符串中是否包含单引号
	 * 2011-4-19
	 */
	public static boolean containsSinglequotes(String s){
		if(s != null && s.contains("'")){
			return true;
		}
		return false;
	}
	
	/**
	 * 验证字符串种是否包含括号(小括号)
	 * 2011-4-19
	 */
	public static boolean containsBracket(String s){
		if(s != null && (s.contains("(") || s.contains(")"))){
			return true;
		}
		return false;
	}
	
	/**
	 * 如果包含单引号，抛出非法参数异常
	 * 2011-4-19
	 */
	public static void checkSqlInjection(String... args){
		if(args != null){
			for(String arg:args){
				if(containsSinglequotes(arg)){
					if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
						throw new IllegalArgumentException("参数中不允许包含单引号!");
					}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
						throw new IllegalArgumentException("Parameter is not allowed to contain single quotes!");
					}
				}
			}
		}
	}
	
	/**
	 * 将arg中的特殊字符转成相应的转义字符
	 * 
	 * @param arg
	 */
	public static String escape(String arg){
		if(StringUtils.isNotBlank(arg)){
			arg = arg.replace("<", "&lt;");
			arg = arg.replace(">", "&gt;");
			// arg = arg.replace("&", "&amp;");
			// arg = arg.replace("\"", "&quot;");// 不替换，页面提交的是json字符串已经包含了“
		}
		return arg;
	}
	
	/**
	 * 将arg中的特殊字符转成相应的转义字符
	 * 
	 * @param arg
	 */
	public static String unescape(String arg){
		if(StringUtils.isNotBlank(arg)){
			arg = arg.replace("&lt;", "<");
			arg = arg.replace("&gt;", ">");
			// arg = arg.replace("&amp;", "&");
			// arg = arg.replace("\"", "&quot;");// 不替换，页面提交的是json字符串已经包含了“
		}
		return arg;
	}
	
	/**
	 * 防xss攻击，将特殊字符转成相应的转义字符
	 * 
	 * @param args
	 */
	public static void checkXss(String... args){
		if(args != null){
			for(String arg:args){
				arg = escape(arg);
			}
		}
	}
}
