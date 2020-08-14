package com.tms.service.tp.impl;

import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.springframework.stereotype.Service;

import com.tms.service.tp.SegPackService;
import com.tms.vo.tp.SegPackBVO;

/**
 * 
 * @author xuqc
 * @date 2012-8-31 下午05:05:25
 */
@Service
public class SegPackServiceImpl extends AbsToftServiceImpl implements SegPackService {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, SegPackBVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, SegPackBVO.PK_SEGMENT);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

}
