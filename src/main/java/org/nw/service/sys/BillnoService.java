package org.nw.service.sys;

import org.nw.service.IToftService;
import org.nw.vo.sys.BillnoRuleVO;


/**
 * 单据号规则、单据号处理
 * 
 * @author xuqc
 * @date 2012-7-7 下午09:47:05
 */
public interface BillnoService extends IToftService {

	/**
	 * 根据单据类型返回单据号规则定义，如果公司没有定义，则返回集团定义的规则
	 * 
	 * @param bill_type
	 * @return
	 */
	public BillnoRuleVO getByBillType(String bill_type);
}
