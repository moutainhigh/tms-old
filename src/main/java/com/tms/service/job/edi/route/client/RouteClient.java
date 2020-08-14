package com.tms.service.job.edi.route.client;

/**
 * java调用C#动态库的入口
 * 
 * @author songf
 * @Date 2015年12月11日 下午23:48:28
 *
 */
public class RouteClient {

	/**
	 * 第三方webService服务的方法 
	 * 
	 * @author songf
	 * @Date 2015年12月11日 下午23:48:28
	 *
	 */
	public native String SendMessage(String message, String commandName);
	
	
	static {     
		System.loadLibrary("CRouteClient");     
	}  

	  public static void main(String[] args) {     
	  }    

}
