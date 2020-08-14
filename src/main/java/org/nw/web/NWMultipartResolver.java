package org.nw.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.commons.CommonsMultipartResolver;

/**
 * 避免MultipartResolver和ServletFileUpload冲突的问题
 * 整个问题产生的原因是Spring框架先调用了MultipartResolver 来处理http multi-part的请求。这里http
 * multipart的请求已经消耗掉。后面又交给ServletFileUpload ，那么ServletFileUpload
 * 就获取不到相应的multi-part请求。
 * 
 * @author xuqc
 * @date 2013-7-3 下午02:45:43
 */
public class NWMultipartResolver extends CommonsMultipartResolver {
	private String excludeUrls;
	private String[] excludeUrlArray;

	public String getExcludeUrls() {
		return excludeUrls;
	}

	public void setExcludeUrls(String excludeUrls) {
		this.excludeUrls = excludeUrls;
		this.excludeUrlArray = excludeUrls.split(",");
	}

	public boolean isMultipart(HttpServletRequest request) {
		for(String url : excludeUrlArray) {
			if(request.getRequestURI().contains(url)) {
				return false;
			}
		}
		return super.isMultipart(request);
	}
}
