package com.tms.service.base;

import java.util.Map;

import org.nw.service.IToftService;
import org.nw.vo.pub.lang.UFDouble;

import com.tms.vo.base.CustomerVO;

/**
 * 客户管理操作接口
 * 
 * @author xuqc
 * @date 2012-7-17 下午11:13:08
 */
public interface CustService extends IToftService {

	public CustomerVO getByCode(String code);

	/**
	 * 根据客户返回客户的发票抬头
	 * 
	 * @param pk_customer
	 * @return
	 */
	public String getCheckHead(String pk_customer);

	/**
	 * 返回客户的名称字符串，多个pk_customer使用|分隔，返回的name也使用|分隔
	 * 
	 * @param pk_customer
	 * @return
	 */
	public String getNameString(String pk_customer);

	public String getDefaultCheckType(String bala_customer);
	
	public String getDefaultCheckCorp(String bala_customer);
	
	public UFDouble getFeeRate(String pk_customer,String pk_trans_type, String start_area, String end_area);
}
