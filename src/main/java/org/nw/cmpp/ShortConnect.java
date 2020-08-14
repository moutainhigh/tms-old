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
public class ShortConnect {

	static Logger logger = Logger.getLogger(ShortConnect.class);
	public static CMPP cmpp = new CMPP();
	public static conn_desc con = new conn_desc();

	public ShortConnect() {
		new InitParameter();
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
			rec.getMsg();
		} catch(Exception e) {
			logger.warn("登录网关失败！");
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
			rec.getMsg();
			logger.info("成功退出网关！");
		} catch(Exception e) {
			logger.warn("退出网关失败！");
		}
	}

}
