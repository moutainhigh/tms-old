package org.nw.cmpp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.commerceware.cmpp.CMPP;
import com.commerceware.cmpp.DeliverFailException;
import com.commerceware.cmpp.UnknownPackException;
import com.commerceware.cmpp.cmppe_deliver_result;
import com.commerceware.cmpp.cmppe_login_result;
import com.commerceware.cmpp.cmppe_result;
import com.commerceware.cmpp.cmppe_submit_result;

/**
 * <p>
 * 短信接收类
 * </p>
 * 
 * @author xiaoma
 * 
 */
public class Receiver{

	static Logger logger = Logger.getLogger(Receiver.class);
	cmppe_result msgresult = null;

	public Receiver() {

	}

	/**
     * 
     *
     */
	public void getMsg() {
		// System.out.println("LoginGetWay_con:"+LoginGetWay.con);
		try {
			// LoginGetWay.cmpp.cmpp_active_test(LoginGetWay.con); //测试网关是否连接
			msgresult = ShortConnect.cmpp.readResPack(ShortConnect.con);
			switch(msgresult.pack_id){
			case CMPP.CMPPE_NACK_RESP:
				logger.info("Get Nack Pack！");
				break;
			case CMPP.CMPPE_LOGIN_RESP: // 登陆连接后的返回信息
				cmppe_login_result cl;
				cl = (cmppe_login_result) msgresult;
				logger.info("从网关接收登陆的返回信息: STAT = " + cl.stat);
				if(cl.stat == 0) {
					logger.info("登陆移动网关成功！");
					InitParameter.isConnect = true;
				} else {
					logger.info("登录网关失败！");
					InitParameter.isConnect = false;
				}
				break;
			case CMPP.CMPPE_LOGOUT_RESP: // 退出连接后的返回信息
				logger.info("从网关接收退出的返回信息: STAT = " + msgresult.stat);
				break;
			case CMPP.CMPPE_SUBMIT_RESP: // 通过网站发送短信到手机后网关的返回信息
				// cmppe_submit_result sr = (cmppe_submit_result)msgresult;
				// logger.info("从网关接收发送完短信的返回信息：STAT = " + msgresult.stat);
				// cmppe_submit_result submitResult =
				// (cmppe_submit_result)msgresult;
				// break;
				cmppe_submit_result sr = (cmppe_submit_result) msgresult;
				logger.info("从网关接收发送完短信的返回信息：STAT = " + sr.stat);
				break;
			case CMPP.CMPPE_DELIVER: // 用户通过手机提交短信后的网关的返回信息
				cmppe_deliver_result deliverResult = (cmppe_deliver_result) msgresult;
				logger.info("从网关接收收到短信的返回信息：STAT = " + deliverResult.status_rpt);
				ShortConnect.cmpp.cmpp_send_deliver_resp(ShortConnect.con, deliverResult.seq, deliverResult.stat);
				Date d = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat();
				sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String done_time = sdf.format(d);

				int status_rpt = deliverResult.status_rpt;
				int data_coding = deliverResult.data_coding;
				int pack_id = deliverResult.pack_id;
				String src_addr = new String(deliverResult.src_addr);
				logger.info("从" + src_addr + "收到一条短信");
				String dst_addr = new String(deliverResult.dst_addr);
				// String short_msg = new String(deliverResult.short_msg);
				int len = deliverResult.short_msg.length;
				byte[] b = new byte[len - 1];
				System.arraycopy(deliverResult.short_msg, 0, b, 0, len - 1);
				String short_msg = "";
				if(data_coding == 8)// 如果编码格式为ucs2，就转换成普通的string
				{
					try {
						// short_msg = new
						// String(deliverResult.short_msg,"unicodebigunmarked");
						short_msg = new String(b, "ISO-8859-1");
						short_msg = new String(short_msg.getBytes("ISO-8859-1"), "iso-10646-ucs-2");
					} catch(UnsupportedEncodingException e) {
						logger.warn("short_msg UnsupportedEncodingException");
						// e.printStackTrace();
					}
				}

				if(!short_msg.trim().equals("")) {

				}
				break;
			case CMPP.CMPPE_CANCEL_RESP: // 取消后的返回信息
				logger.info("从网关接收取消短信发送的返回信息：STAT = " + msgresult.stat);
				break;
			case CMPP.CMPPE_ACTIVE_RESP:// SP发送网关测试的返回信息
				logger.info("从网关接收SP测试链路的返回信息：STAT = " + msgresult.stat);
				if(msgresult.stat != 0) {
					logger.info("当前网关无连接,系统将重新连接！");
					InitParameter.isConnect = false;
					ShortConnect sc = new ShortConnect();
					sc.login();
				}
			default:
				// logger.info("error pack!");
				break;
			}
		} catch(NullPointerException e) {
			// logger.warn("当前无返回信息！");
		} catch(UnknownPackException e) {
			// logger.warn("UnknownPackException！" + e);
		} catch(IOException e) {
			// logger.warn("IOException！" + e);
			// return;
		} catch(DeliverFailException e) {
			// InitParameter.isDelever = false;
			// logger.warn("接收失败！" + e);
		}
	}

	/**
	 * 重载
	 * 
	 */
	public void readPa() {
		// System.out.println("ShortLogin.cmpp:"+ShortLogin.cmpp);
		// System.out.println("ShortLogin.con:"+ShortLogin.con);
		try {
			msgresult = LongConnect.cmpp.readResPack(LongConnect.con);
			switch(msgresult.pack_id){
			case CMPP.CMPPE_NACK_RESP:
				logger.info("Get Nack Pack！");
				break;
			case CMPP.CMPPE_LOGIN_RESP: // 登陆连接后的返回信息
				cmppe_login_result cl;
				cl = (cmppe_login_result) msgresult;
				logger.info("从网关接收登陆的返回信息: STAT = " + cl.stat);
				if(cl.stat == 0) {
					logger.info("登陆移动网关成功！");
					InitParameter.isConnect = true;
				} else {
					logger.info("登录网关失败！");
					InitParameter.isConnect = false;
				}
				break;
			case CMPP.CMPPE_LOGOUT_RESP: // 退出连接后的返回信息
				logger.info("从网关接收退出的返回信息: STAT = " + msgresult.stat);
				break;
			case CMPP.CMPPE_SUBMIT_RESP: // 通过网站发送短信到手机后网关的返回信息
				// cmppe_submit_result sr = (cmppe_submit_result)msgresult;
				// logger.info("从网关接收发送完短信的返回信息：STAT = " + msgresult.stat);
				// cmppe_submit_result submitResult =
				// (cmppe_submit_result)msgresult;
				// break;
				cmppe_submit_result sr = (cmppe_submit_result) msgresult;
				logger.info("从网关接收发送完短信的返回信息：STAT = " + sr.stat);
				break;
			case CMPP.CMPPE_DELIVER: // 用户通过手机提交短信后的网关的返回信息
				cmppe_deliver_result deliverResult = (cmppe_deliver_result) msgresult;
				logger.info("从网关接收收到短信的返回信息：STAT = " + deliverResult.status_rpt);
				LongConnect.cmpp.cmpp_send_deliver_resp(LongConnect.con, deliverResult.seq, deliverResult.stat);
				Date d = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat();
				sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String done_time = sdf.format(d);

				int status_rpt = deliverResult.status_rpt;
				int data_coding = deliverResult.data_coding;
				int pack_id = deliverResult.pack_id;
				String src_addr = new String(deliverResult.src_addr);
				logger.info("从" + src_addr + "收到一条短信");
				String dst_addr = new String(deliverResult.dst_addr);
				// String short_msg = new String(deliverResult.short_msg);
				int len = deliverResult.short_msg.length;
				byte[] b = new byte[len - 1];
				System.arraycopy(deliverResult.short_msg, 0, b, 0, len - 1);
				String short_msg = "";
				if(data_coding == 8)// 如果编码格式为ucs2，就转换成普通的string
				{
					try {
						// short_msg = new
						// String(deliverResult.short_msg,"unicodebigunmarked");
						short_msg = new String(b, "ISO-8859-1");
						short_msg = new String(short_msg.getBytes("ISO-8859-1"), "iso-10646-ucs-2");
					} catch(UnsupportedEncodingException e) {
						logger.warn("short_msg UnsupportedEncodingException");
						// e.printStackTrace();
					}
				}

				if(!short_msg.trim().equals("")) {

				}
				break;
			case CMPP.CMPPE_CANCEL_RESP: // 取消后的返回信息
				logger.info("从网关接收取消短信发送的返回信息：STAT = " + msgresult.stat);
				break;
			case CMPP.CMPPE_ACTIVE_RESP:// SP发送网关测试的返回信息
				logger.info("从网关接收SP测试链路的返回信息：STAT = " + msgresult.stat);
				if(msgresult.stat != 0) {
					logger.info("当前网关无连接,系统将重新连接！");
					InitParameter.isConnect = false;
					LongConnect lc = new LongConnect();
					lc.login();
				}
			default:
				logger.info("error pack!");
				break;
			}
		} catch(NullPointerException e) {
			// logger.warn("当前无返回信息！");
		} catch(UnknownPackException e) {
			// logger.warn("UnknownPackException！" + e);
		} catch(IOException e) {
			// logger.warn("IOException！" + e);
			// return;
		} catch(DeliverFailException e) {
			// InitParameter.isDelever = false;
			// logger.warn("接收失败！" + e);
		}
	}

	public void closeReceive() {
		InitParameter.isReceive = false;
		logger.info("已停止接收线程！");
	}
}
