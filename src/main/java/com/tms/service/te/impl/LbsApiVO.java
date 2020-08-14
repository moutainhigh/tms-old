package com.tms.service.te.impl;

import java.util.Map;

/**
 * 
 * @author xuqc
 * @date 2015-2-7 下午02:22:23
 */
public class LbsApiVO {

	String host;
	String uid;
	String pwd;
	Map<String, String> methodMap;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Map<String, String> getMethodMap() {
		return methodMap;
	}

	public void setMethodMap(Map<String, String> methodMap) {
		this.methodMap = methodMap;
	}

}
