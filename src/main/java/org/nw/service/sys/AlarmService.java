package org.nw.service.sys;

import java.util.List;
import java.util.Map;

import org.nw.service.IToftService;
import org.nw.vo.pub.AggregatedValueObject;

/**
 * 预警信息处理接口
 * 
 * @author xuqc
 * @date 2013-9-19 上午10:29:48
 */
public interface AlarmService extends IToftService {

	/**
	 * 返回最近的5条未处理的预警，有些字段需要执行公式
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getTop5();

	/**
	 * 发送预警信息
	 * 
	 * @param billVO
	 *            单据VO
	 * @param paramVO
	 * @param receiver_man
	 *            接收人
	 */
	public void sendAlarm(AggregatedValueObject billVO, String billtype, String funCode, String[] receiver_man);

	/**
	 * 删除预警信息，如用户对单据进行反提交操作,<BR/>
	 * XXX 有些业务不能使用该方法，如用户将该代办发给多个人的时候
	 * 
	 * @param pk_bill
	 * @param sender_man
	 */
	public void deleteAlarm(String pk_bill, String sender_man);

	/**
	 * 删除预警信息,如取消分配给该用户任务的时候 <BR/>
	 * 理论上这个方法只会定位到一条记录，所以调用该方法不会有问题<BR/>
	 * XXX 但是有些业务可以考虑使用org.nw.service.sys.AlarmService .deleteAlarm(String,
	 * String)方法
	 * 
	 * @param pk_bill
	 * @param receiver_man
	 */
	public void deleteAlarmByReceiver_man(String pk_bill, String receiver_man);

	/**
	 * 处理预警，如用户对单据进行审核，需要将预警信息置为已处理， 注意：用户提交单据后，可能会将信息发给不同的接收人，但是处理人可能一个就够了
	 * 
	 * @param pk_bill
	 * @param receiver_man
	 *            这个预警的接收人
	 * @param deal_man
	 *            这个预警的处理人
	 */
	public void dealAlarm(String pk_bill, String[] receiver_man);

}
