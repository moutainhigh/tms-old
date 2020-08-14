package org.nw.cmpp;

import org.apache.log4j.Logger;

import com.commerceware.cmpp.CMPP;
import com.commerceware.cmpp.conn_desc;

/**
 * 登陆网关
 * 
 * @author xiaoma
 * 
 */
public class LongConnect extends Thread {

	static Logger logger = Logger.getLogger(LongConnect.class);
	public static CMPP cmpp = new CMPP();;
	public static conn_desc con = new conn_desc();

	public LongConnect() {
		new InitParameter();
	}

	/**
	 * 连接网关线程
	 */
	public void run() {
		int count = 0; // 当前网关登陆次数

		while(!InitParameter.isConnect) {
			count++;
			login();// 返回是否登陆成功的标志
			if(!InitParameter.isConnect) { // 若没有登陆成功,则线程sleep 3s
				try {
					Thread.sleep(InitParameter.LOGINGETWAY_SLEEPTIME);
				} catch(InterruptedException e) {
					logger.warn("线程被终止,请重新启动tomcat试试！");
				}
			}

			if(!InitParameter.isConnect && count == InitParameter.MAX_LOGINGETWAY) {
				logger.info("超过尝试登陆次数,请检查.或重新启动tomcat试试！");
				break;
			}
		}
	}

	/**
	 * 登陆网关
	 * 
	 * @return boolean
	 */
	public synchronized void login() {
		try {
			logger.info("正在登陆移动网关,请稍候...");
			cmpp.cmpp_connect_to_ismg(InitParameter.IP, InitParameter.port, con);
			cmpp.cmpp_login(con, InitParameter.loginID, InitParameter.password, (byte) 2, 0X12,
					(int) System.currentTimeMillis());
			/*
			 * result = cmpp.readResPack(con);
			 * if(result.stat==CMPP.CMPPE_RSP_SUCCESS) {
			 * logger.info("登陆移动网关成功！"); InitParameter.isConnect = true; } else{
			 * logger.info("登录网关失败！"); InitParameter.isConnect = false; }
			 */
			Receiver rec = new Receiver();
			rec.readPa();
			InitParameter.isConnect = true;
		} catch(Exception e) {
			logger.warn("登录网关失败！");
			InitParameter.isConnect = false;
		}
	}

	/**
	 * 退出网关
	 * 
	 * @return void
	 */
	public void logout() {
		try {
			cmpp.cmpp_logout(con);
			Receiver rec = new Receiver();
			rec.readPa();
			InitParameter.isConnect = false;
			logger.info("成功退出网关！");
		} catch(Exception e) {
			logger.warn("退出网关失败！");
		}
	}

}
