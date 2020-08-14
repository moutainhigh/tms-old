package org.nw.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.nw.web.utils.ServletContextHolder;


/**
 * 持有request对象,放入线程中,方便在整个线程执行过程取得该对象
 * 
 * @author xuqc
 * 
 */
public class ServletContextHolderFilter implements Filter {

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;

		ServletContextHolder.setRequest(httpRequest);
		try {
			chain.doFilter(request, response);
		} finally {
			ServletContextHolder.remove();
		}
	}

	public void destroy() {

	}

	public void init(FilterConfig filterConfig) throws ServletException {
	}
}
