package org.nw.vo;

import java.io.Serializable;
import java.util.Map;

/**
 * 联查按钮vo
 * 
 * @author xuqc
 * @date 2015-1-12 下午11:33:14
 */
public class LinkButtonVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String text;// 联查的链接
	private String url;// 如果直接定义了url，那么直接使用<a>标签在新窗口打开,支持{param}的参数格式，这个param会从上下文读取
	private String fun_code;
	private Map<String, Object> paramMap;// 参数集合，key作为打开url的参数key，value将作为key从上下文读取对应的值

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getFun_code() {
		return fun_code;
	}

	public void setFun_code(String fun_code) {
		this.fun_code = fun_code;
	}

	public Map<String, Object> getParamMap() {
		return paramMap;
	}

	public void setParamMap(Map<String, Object> paramMap) {
		this.paramMap = paramMap;
	}

}
