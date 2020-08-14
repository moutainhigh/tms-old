package org.nw.basic.util;

import org.apache.commons.lang.StringUtils;

/**
 * 数据库字段名到vo属性名的类反射转换工具类
 * 
 * @author xuqc
 * @date 2010-10-29
 */
public class VOUtils{
	/**
	 * 把数据库字段名，转换为po属性名
	 * 
	 * @param colName
	 * @return
	 */
	public static String convertToObjectName(String colName){
		if(StringUtils.isBlank(colName)) return "";
		colName=colName.toLowerCase();
		StringBuffer sb=new StringBuffer();
		boolean flag=false;
		for(int i=0;i<colName.length();i++){
			char c=colName.charAt(i);
			if("_".charAt(0)==c){
				flag=true;
				continue;
			}
			else{
				if(flag){
					// 如果上一个是"_",那么转成大写
					if(c>=97&&c<=122){
						c=(char)(c-32);
					}
				}
				sb.append(c);
				flag=false;
			}
		}
		return sb.toString();
	}
	
	/**
	 * 将tableName转换为类名
	 * 
	 * @param tableName
	 * @return
	 */
	public static String convertToEntityName(String tableName){
		String clazzName=convertToObjectName(tableName);
		String suffix=clazzName.substring(1);
		String prefix=clazzName.substring(0,1);
		return prefix.toUpperCase()+suffix;
	}
	
	/**
	 * hql查询语句中用到的对象别名，
	 * 将对象名称的首字母变为小写
	 * 
	 * @param clazzName
	 * @return
	 */
	public static String convertToAliasName(String clazzName){
		String suffix=clazzName.substring(1);
		String prefix=clazzName.substring(0,1);
		return prefix.toLowerCase()+suffix;
	}
	
	/**
	 * NC自动生成的VO的字段名与数据库的字段名相同，只是全部使用小写
	 * 
	 * @param colName
	 * @return
	 */
	public static String getNCSetterMethodName(String colName){
		String first=colName.substring(0,1);
		return "set"+first.toUpperCase()+colName.substring(1);
	}
	
	/**
	 * NC自动生成的VO的字段名与数据库的字段名相同，只是全部使用小写
	 * 
	 * @param colName
	 * @return
	 */
	public static String getNCGetterMethodName(String colName){
		String first=colName.substring(0,1);
		return "get"+first.toUpperCase()+colName.substring(1);
	}
}
