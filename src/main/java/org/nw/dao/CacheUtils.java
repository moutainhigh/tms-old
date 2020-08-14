package org.nw.dao;

import org.nw.Global;

/**
 * 缓存工具类<br>
 * 2011-4-9
 * 
 * @author fangw
 */
public class CacheUtils {

	/**
	 * 是否使用缓存<br>
	 * 
	 * @return
	 */
	public static boolean isUseCache() {
		return !Boolean.valueOf(Global.getPropertyValue("debug"));
	}

	/**
	 * 拼装cache的key<br>
	 * 支持参数是数组，如果有多级，会自动递归获取
	 */
	public static String getCacheKey(Object... args) {
		StringBuilder sb = new StringBuilder();// "K"
		for(Object arg : args) {
			if(arg != null) {
				if(arg instanceof Object[]) {
					for(Object o : (Object[]) arg) {
						if(o instanceof Object[]) {
							sb.append(getCacheKey(o));// 递归获取key
						} else {
							sb.append("_");
							sb.append(o);
						}
					}
				} else {
					sb.append("_");
					sb.append(arg);
				}
			}
		}
		return sb.toString();
	}

}
