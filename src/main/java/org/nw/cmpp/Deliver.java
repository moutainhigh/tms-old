package org.nw.cmpp;

import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.commerceware.cmpp.OutOfBoundsException;
import com.commerceware.cmpp.cmppe_submit;

/**
 * 短信发送类,继承Thread
 * 
 * @author xiaoma
 * 
 */
public class Deliver {

	static Logger logger = Logger.getLogger(Deliver.class);
	private byte[] msg;// 消息内容
	private byte[][] dst_arr = new byte[100][15]; // 消息接收者
	// private Set dst_arr1 = new HashSet();
	private byte cFrom[]; // 消息落款

	/**
	 * 自费类型，包括0、1、2、3、4、5 0、“短消息类型”为“发送”，对“计费用户号码”不计信息费，此类话单仅用于核减SP对称的信道费
	 * 1、对“计费用户号码”免费 2、对“计费用户号码”按条计信息费 3、对“计费用户号码”按包月收取信息费 4、对“计费用户号码”的信息费封顶
	 * 5、对“计费用户号码”的收费是由SP实现
	 */
	private static byte fee_type = 1;
	/**
	 * 资费代码以分为单位，值范围0-999999
	 */
	private static byte info_fee = 1;
	/**
	 * 协议标识
	 */
	private static byte proto_id = 1;
	/**
	 * 消息模式： xxxxxxx1 是否需要状态报告 xxxxxx1x 是否是控制类消息 xxxxx1xx
	 * 是否是SIM卡操作，此项是v1.1的解决方案，当此位被设置，protocol id 将被设置为0x7f,
	 * 为了向下兼容，此为仍然有效，但建议直接通过设置protocol id的方式进行 xxxx1xxx UDHI
	 * Indicator，用于铃声，图标等类型的消息 1111xxxx 保留
	 */
	private static byte msg_mode = 0;
	/**
	 * 优先级别值范围0-9，优先级9最高
	 */
	private static byte priority = 0;
	/**
	 * 计费用户类型 0：对目的终端MSISDN计费； 1：对源终端MSISDN计费； 2：对SP计费; 3：对指定的计费用户收费
	 */
	private static byte fee_utype = 2;
	/**
	 * 计费用户，如果计费用户类型为3，则本参数必须被设置；如果计费用户类型为其它值，此参数无效
	 */
	// private static byte fee_user[] = new byte[CMPPE_MAX_MSISDN_LEN];
	/**
	 * Submit包的存活有效期
	 */
	// private static byte validate[] = new byte[10];
	// validate[0] =0;
	/**
	 * Submit包的定时发送时间
	 */
	// private static byte schedule[] = new byte[2];
	// schedule[0] =0;
	/**
	 * 接收用户数量取值范围为 1-255
	 */
	private static byte du_count;

	/**
	 * 多个接收人
	 * 
	 * @param msg
	 * @param receiver
	 * @param cFrom
	 */
	public Deliver(String msg, Set receiver, String cFrom) {
		logger.info("初始化发送参数...");
		this.msg = msg.getBytes();
		this.dst_arr = set2ByteArr(receiver);
		this.cFrom = cFrom.getBytes();
		this.du_count = (byte) (receiver.size());
	}

	/**
	 * 只有一个接收人
	 * 
	 * @param msg
	 * @param receiver
	 * @param cFrom
	 */
	public Deliver(String msg, String receiver, String cFrom) {
		logger.info("初始化发送参数...");
		this.msg = msg.getBytes();
		receiver += "\0";
		this.dst_arr[0] = receiver.getBytes();
		this.cFrom = cFrom.getBytes();
		this.du_count = (byte) 1;
	}

	/**
	 * 向网关发送消息
	 * 
	 * @param submit
	 * @return void
	 */
	public boolean sendSubmit() {

		boolean isSuccess = false;
		Receiver rec = new Receiver();
		int count = 0;
		count++;
		cmppe_submit submitStruct = new cmppe_submit();
		try {
			submitStruct.set_icpid(InitParameter.icpID.getBytes());
			submitStruct.set_svctype(InitParameter.svcType.getBytes());
			submitStruct.set_srcaddr(InitParameter.userID.getBytes());
			submitStruct.set_ducount(du_count);
			submitStruct.set_dstaddr(dst_arr);
			submitStruct.set_feetype(fee_type);
			submitStruct.set_infofee(info_fee);
			submitStruct.set_protoid(proto_id);
			submitStruct.set_msgmode(msg_mode);
			submitStruct.set_priority(priority);
			submitStruct.set_feeutype(fee_utype);
			submitStruct.set_msg((byte) 15, msg.length, msg);
			ShortConnect.cmpp.cmpp_submit(ShortConnect.con, submitStruct);
			rec.getMsg();
			// LoginGetWay.cmpp.cmpp_submit(LoginGetWay.con, submitStruct);
			// InitParameter.isDelever = true;
			isSuccess = true;
			logger.info("已成功发送" + du_count + "条信息!");
			ShortConnect.cmpp.cmpp_active_test(ShortConnect.con);
			rec.getMsg();
		} catch(OutOfBoundsException e) {
			logger.warn("短信长度过长!");
			// InitParameter.isDelever = false;
			isSuccess = false;
			logger.warn("发送失败");
		} catch(Exception e) {
			e.printStackTrace();
			// InitParameter.isDelever = false;
			isSuccess = false;
			logger.warn("发送失败");
		}
		return isSuccess;
	}

	public byte[][] set2ByteArr(Set set) {
		byte[][] dst = new byte[100][15];
		Iterator it = set.iterator();
		int i = 0;
		while(it.hasNext()) {
			StringBuffer sb = new StringBuffer((String) it.next());
			sb.append("\0");
			dst[i] = sb.toString().getBytes();
			i++;
		}
		return dst;
	}

	public static void main(String[] args) {
		// Thread t = new Deliver("内容:测试短信","15960841725","市政府短信平台测试");
		// t.start();
	}
}
