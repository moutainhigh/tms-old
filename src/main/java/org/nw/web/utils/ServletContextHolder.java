package org.nw.web.utils;

import javax.servlet.http.HttpServletRequest;

/**
 * web的上下文holder类
 * 
 * @author fangw
 */
public class ServletContextHolder {
	private static ThreadLocal<HttpServletRequest> requestLocal = new ThreadLocal<HttpServletRequest>();

	public static HttpServletRequest getRequest() {
		return requestLocal.get();
	}

	public static void setRequest(HttpServletRequest request) {
		requestLocal.set(request);
	}

	public static void remove() {
		requestLocal.remove();
	}
}
