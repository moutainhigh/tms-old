package com.tms.service.base;

import java.util.Map;

import org.nw.service.IToftService;
import org.nw.vo.pub.lang.UFDouble;

import com.tms.vo.base.CarrierVO;

/**
 * 承运商处理接口
 * 
 * @author xuqc
 * @date 2012-7-22 下午02:21:17
 */
public interface CarrService extends IToftService {

	public CarrierVO getByCode(String code);

	/**
	 * 根据承运商返回发票抬头
	 * 
	 * @param pk_carrier
	 * @return
	 */
	public String getCheckHead(String pk_carrier);

	
	public String getDefaultCheckType(String pk_carrier);
	
	
	public UFDouble getFeeRate(String pk_carrier,String pk_trans_type, String start_area, String end_area);
}
