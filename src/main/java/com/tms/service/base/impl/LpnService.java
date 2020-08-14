package com.tms.service.base.impl;

import org.nw.service.impl.AbsBaseDataServiceImpl;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.springframework.stereotype.Service;

import com.tms.vo.base.LpnVO;

/**
 * lpn
 * 
 * @author xuqc
 * @date 2013-12-29 上午11:10:57
 */
@Service
public class LpnService extends AbsBaseDataServiceImpl {

	AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, LpnVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, LpnVO.PK_LPN);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

}
