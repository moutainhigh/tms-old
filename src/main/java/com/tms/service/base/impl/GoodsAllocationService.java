package com.tms.service.base.impl;

import org.nw.service.impl.AbsBaseDataServiceImpl;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.springframework.stereotype.Service;

import com.tms.vo.base.GoodsAllocationVO;

/**
 * 货位
 * 
 * @author xuqc
 * @date 2013-12-29 上午11:09:24
 */
@Service
public class GoodsAllocationService extends AbsBaseDataServiceImpl {

	AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, GoodsAllocationVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, GoodsAllocationVO.PK_GOODS_ALLOCATION);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	public String getCodeFieldCode() {
		return GoodsAllocationVO.CODE;
	}
}
