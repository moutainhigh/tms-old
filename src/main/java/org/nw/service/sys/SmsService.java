package org.nw.service.sys;

import java.util.List;
import java.util.Map;

import org.nw.service.IToftService;
import org.nw.vo.sys.FunVO;
import org.nw.vo.sys.SmsVO;


/**
 * 站内消息处理接口
 * 
 * @author xuqc
 * @date 2013-7-1 下午02:48:28
 */
public interface SmsService extends IToftService {

	/**
	 * 返回最新的5条提醒，按照是否查看，发送时间排序
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getTop5();

	/**
	 * 根据主键查询，并执行公式
	 * 
	 * @return
	 */
	public Map<String, Object> getByPK(String pk_sms);

	/**
	 * 更新成已读
	 * 
	 * @param vo
	 * @return
	 */
	public SmsVO updateReadFlag(String pk_sms);

	/**
	 * 发送站内信，多个发送人在vo的receiver字段，使用逗号分隔，如果是发给所有人(公告)，那么为空
	 * 
	 * @param vo
	 */
	public void doSend(SmsVO vo,String[] pk_attas);

	/**
	 * 返回当前登陆用户的总消息数
	 * 
	 * @return
	 */
	public Integer getCount();
	
	/**
	 * 跟根据 单据的pk和对应的模板，获取对应的单据vbillnos
	 * @author XIA
	 * @param billIds
	 * @param funVO
	 * @return
	 */
	public String getVbillnosByFunVOAndbillids(String billIds, FunVO funVO);
}
