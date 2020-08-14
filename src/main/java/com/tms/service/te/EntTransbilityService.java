package com.tms.service.te;

import org.nw.service.IToftService;
import org.nw.vo.pub.AggregatedValueObject;

import com.tms.vo.te.EntTransbilityBVO;

/**
 * 委托单运力信息的维护
 * 
 * @author xuqc
 * @date 2013-8-25 上午09:55:37
 */
public interface EntTransbilityService extends IToftService {
	public void saveTransbility(AggregatedValueObject billVO,String pk_entrust);

}
