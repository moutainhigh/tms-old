package org.nw.cmpp;

import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * <p>
 * 读取配置文件初始化参数
 * </p>
 * 
 * @author xiaoma
 * 
 */
public class InitParameter {

	static Logger logger = Logger.getLogger(InitParameter.class);
	/**
	 * 网关IP
	 */
	public static String IP;
	/**
	 * 网关端口
	 */
	public static int port;
	/**
	 * 登陆ID
	 */
	public static String loginID;
	/**
	 * 登陆密码
	 */
	public static String password;
	/**
	 * 源终端MSISDN号码 当此条消息是用户A向其它用户点播的，此参数添充用户A的手机号码。源用户可以和目的用户（之一）相同，也可以填空。
	 */
	public static String userID;

	public static String icpID;
	/**
	 * 服务类型，可以是任何可见的ASCII字符
	 */
	public static String svcType;

	// String conFileName = "confile";

	/**
	 * 网关是否连接
	 */
	public static boolean isConnect = false;
	/**
	 * 是否已启动后台接收线程
	 */
	public static boolean isReceive = false;
	/**
	 * 允许连续测试登陆网关的次数
	 */
	public static final int MAX_LOGINGETWAY = 10;
	/**
	 * 重复登陆网关之间的睡眠时间
	 */
	public static final int LOGINGETWAY_SLEEPTIME = 5000;
	/**
	 * 接收线程之间的睡眠时间
	 */
	public static final int RECEIVE_SLEEPTIME = 1000 * 60 * 2;
	/**
	 * 执行定时任务间隔时间
	 */
	public static final int TASK_SLEEPTIME = 5 * 60 * 1000;

	/**
	 * 读取配置文件初始化参数
	 */
	public InitParameter() {
		HashMap hm = new HashMap();
		// hm = Parameter.loadParameter();
		this.IP = (String) hm.get("IP");
		try {
			this.port = Integer.parseInt(hm.get("port").toString());
		} catch(Exception e) {
			port = 7890;
			logger.warn("从数据库中读取端口时转换出错！");
		}
		loginID = (String) hm.get("loginID");
		password = (String) hm.get("password");
		userID = (String) hm.get("userID");
		icpID = (String) hm.get("icpID");
		svcType = (String) hm.get("svcType");

		icpID += "\0";
		userID += "\0";
		svcType += "\0";
		logger.info("初始化登陆参数完成！");

		hm.clear();

		// this.IP="211.143.170.161";
		// this.port=7890;
		// this.loginID="444036";
		// this.password="rtrtr5";
		// this.userID="059212345\0";
		// this.icpID="444036\0";
		// this.svcType="xmsm\0";

		/*
		 * PropertyResourceBundle resourceBundle = (PropertyResourceBundle)
		 * PropertyResourceBundle .getBundle(conFileName); Enumeration enu =
		 * resourceBundle.getKeys(); while (enu.hasMoreElements()) { String
		 * propertyName = enu.nextElement().toString(); if
		 * (propertyName.equals("confile.ip")) { this.IP =
		 * resourceBundle.getString("confile.ip"); } if
		 * (propertyName.equals("confile.port")) { String strport =
		 * resourceBundle.getString("confile.port"); this.port =
		 * Integer.parseInt(strport); } if
		 * (propertyName.equals("confile.loginID")){ this.loginID =
		 * resourceBundle.getString("confile.loginID"); } if
		 * (propertyName.equals("confile.password")){ this.password =
		 * resourceBundle.getString("confile.password"); } if
		 * (propertyName.equals("confile.userID")){ this.userID =
		 * resourceBundle.getString("confile.userID"); } if
		 * (propertyName.equals("confile.icpID")){ this.icpID =
		 * resourceBundle.getString("confile.icpID"); } if
		 * (propertyName.equals("confile.svcType")){ this.svcType =
		 * resourceBundle.getString("confile.svcType"); } }
		 */
	}

	public void clearParameter() {
		this.IP = "";
		this.port = 0;
		this.loginID = "";
		this.password = "";
		this.userID = "";
		this.icpID = "";
		this.svcType = "";
	}

	public void getParameter() {

	}
}
