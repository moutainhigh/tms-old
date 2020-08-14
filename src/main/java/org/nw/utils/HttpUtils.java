package org.nw.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.nw.exception.ApiException;
import org.nw.exception.BusiException;
import org.nw.service.api.AuthenticationService;
import org.nw.web.utils.SpringContextHolder;

/**
 * http请求工具类
 * 
 * @author xuqc
 * @date 2012-8-2 下午02:35:13
 */
public class HttpUtils {

	/**
	 * 发送一个http请求到url，并返回值
	 * 
	 * @param url
	 * @return
	 * @author xuqc
	 * @date 2012-8-2
	 * 
	 */
	public static String get(String url) throws Exception {
		HttpURLConnection conn = null;
		String result = null; // 从
		try {
			URL callUrl = new URL(url);
			conn = (HttpURLConnection) callUrl.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();

			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			java.lang.StringBuffer stringBuffer = new java.lang.StringBuffer();

			String lines = null;
			while((lines = reader.readLine()) != null) {
				stringBuffer.append(lines);
			}

			reader.close();
			result = stringBuffer.toString();
		} finally {
			if(conn != null) {
				conn.disconnect();
			}
		}
		return result;
	}

	/**
	 * 发送post请求
	 * 
	 * @param url
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static String post(String url, Map<String, Object> paramMap) throws Exception {
		HttpURLConnection conn = null;
		String result = null; // 从
		try {
			URL callUrl = new URL(url);
			conn = (HttpURLConnection) callUrl.openConnection();
			// 设置是否向connection输出，因为这个是post请求，参数要放在
			// http正文内，因此需要设为true
			conn.setDoOutput(true);
			// Read from the connection. Default is true.
			conn.setDoInput(true);
			// Set the post method. Default is GET
			conn.setRequestMethod("POST");
			// Post cannot use caches
			// Post 请求不能使用缓存
			conn.setUseCaches(false);
			// 配置本次连接的Content-type，配置为application/x-www-form-urlencoded的
			// 意思是正文是urlencoded编码过的form参数，下面我们可以看到我们对正文内容使用URLEncoder.encode
			// 进行编码
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.connect();
			// 参数
			// The URL-encoded contend
			// 正文，正文内容其实跟get的URL中'?'后的参数字符串一致
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			if(paramMap != null) {
				String content = "";
				for(String key : paramMap.keySet()) {
					Object value = paramMap.get(key);
					if(value == null) {
						value = "";
					}
					if(value instanceof Collection) {// 支持集合参数
						Collection<String> c = (Collection<String>) value;
						Iterator<String> it = c.iterator();
						while(it.hasNext()) {
							content += key + "=" + URLEncoder.encode(it.next(), "UTF-8");
							content += "&";
						}
					} else {
						content += key + "=" + URLEncoder.encode(String.valueOf(value), "UTF-8");
						content += "&";
					}
				}
				if(content.length() > 0) {
					content = content.substring(0, content.length() - 1);
				}
				out.writeBytes(content);
			}
			out.flush();
			out.close();

			// 读取返回值,并设置编码
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			java.lang.StringBuffer stringBuffer = new java.lang.StringBuffer();

			String lines = null;
			while((lines = reader.readLine()) != null) {
				stringBuffer.append(lines);
			}

			reader.close();
			result = stringBuffer.toString();
		} finally {
			if(conn != null) {
				conn.disconnect();
			}
		}
		return result;
	}
	
	/**
	 * 用户名 密码验证
	 * @author XIA
	 * @param uid
	 * @param pwd
	 * @return authError
	 * @throws ApiException
	 */
	public static String authentication(String uid, String pwd) throws ApiException {
		AuthenticationService authenticationService = SpringContextHolder.getBean("authenticationService");
		if (authenticationService == null) {
			throw new BusiException("验证服务没有启动，服务ID:AuthenticationService");
		}
		String authError = authenticationService.auth(uid, pwd);
		return authError;
	}
	
	
}
