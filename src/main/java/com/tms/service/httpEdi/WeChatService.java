package com.tms.service.httpEdi;

import java.util.List;
import java.util.Map;

import org.nw.service.IBillService;

/**
 * 
 * @author XIA
 * @date 2016 6 13 
 */
public interface WeChatService extends IBillService {

	/**
	 * 根据条件查询发货单信息
	 * 
	 * @param cond
	 * @return 
	 */
	public List<Map<String,Object>> searchOrders(String cond);
	
	/**
	 * 根据条件查询发货单数量
	 * 
	 * @param corp_codes
	 * @param cust_codes
	 * @return
	 */
	public List<Map<String, Object>> searchOrdersCount(String corp_codes, String cust_codes);


	
}
