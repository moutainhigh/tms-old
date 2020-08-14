/**
 * 
 */
package com.tms.service.inv;

import java.util.Map;

import org.nw.service.IBillService;
import org.nw.vo.ParamVO;

/**
 * 订单批次管理
 * 
 * @author xuqc
 * @Date 2015年6月9日 下午9:04:09
 *
 */
public interface OrderlotService extends IBillService {

	/**
	 * 当删除发货单后，重算金额
	 * 
	 * @param paramVO
	 * @param lot
	 * @param invoice_vbillnoAry
	 * @return
	 */
	public Map<String, Object> doRecompute(ParamVO paramVO, String lot, String[] invoice_vbillnoAry);
	
	public void setGoodsInfo(Map<String, Object> retMap);
}
