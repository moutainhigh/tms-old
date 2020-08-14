package org.nw.web.filter;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nw.constants.Constants;
import org.nw.web.utils.WebUtils;
import org.nw.web.vo.LoginInfo;

/**
 * TMS客户端过滤器，登陆检查等
 * 
 * @author fangw
 * @date 2010-7-31
 */
public class NWClientFilter implements Filter {
	private static final Log log = LogFactory.getLog(NWClientFilter.class);
	private static String[] pageNotNeedLoginAry = null;

	public void init(FilterConfig filterConfig) throws ServletException {
		String pageNotNeedLogin = filterConfig.getInitParameter("pageNotNeedLogin");
		if(pageNotNeedLogin != null) {
			pageNotNeedLoginAry = pageNotNeedLogin.split("\\|");
		}
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	@SuppressWarnings("rawtypes")
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
			ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		// int serverPort = request.getServerPort();
		// String serverName = "http://" + request.getServerName() + ":" +
		// serverPort;
		// if(serverPort == 80) {
		// serverName = "http://" + request.getServerName();
		// }
		String contextPath = request.getContextPath();
		String servletPath = request.getServletPath();
		// String service = serverName + contextPath + servletPath;
		String service = contextPath + servletPath;

		StringBuffer sb = new StringBuffer(service);
		// 请求参数
		Map paramMap = request.getParameterMap();
		Set keySet = paramMap.keySet();
		if(keySet.size() > 0) {
			sb.append("?");
		}
		for(Object key : keySet) {
			Object valueObj = paramMap.get(key);
			if(valueObj != null) {
				String[] values = (String[]) valueObj;
				for(int i = 0; i < values.length; i++) {
					sb.append(key);
					sb.append("=");
					sb.append(values[i]);
					sb.append("&");
				}
			}
		}
		if(keySet.size() > 0) {
			service = sb.substring(0, sb.length() - 1);
		}

		log.debug("service=" + service);

		// 跳过用户自定义忽略的页面
		if(pageNotNeedLoginAry != null) {
			for(String pageNotNeedLogin : pageNotNeedLoginAry) {
				if(servletPath.startsWith(pageNotNeedLogin)) {
					log.debug("不需要登陆检查的页面.servletPath:" + servletPath);
					chain.doFilter(req, resp);
					return;
				}
			}
		}

		// 登陆过滤
		LoginInfo loginInfo = WebUtils.getLoginInfo();
		if(loginInfo == null) {
			// 用户没有登陆
			response.sendRedirect(request.getContextPath() + "/login.html?service="
					+ URLEncoder.encode(service, Constants.DEFAULT_CHARSET));
			return;
		}

		// 权限过滤
		chain.doFilter(req, resp);
	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
	}

}
