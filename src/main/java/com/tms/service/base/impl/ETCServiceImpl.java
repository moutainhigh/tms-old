package com.tms.service.base.impl;

import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.springframework.stereotype.Service;

import com.tms.service.base.ETCService;
import com.tms.vo.base.ETCBVO;
import com.tms.vo.base.ETCVO;

/**
 * ETC
 * @author muyun
 *
 */
@Service
public class ETCServiceImpl extends AbsToftServiceImpl implements ETCService {
	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, ETCVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, ETCVO.PK_ETC);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, ETCBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, ETCBVO.PK_ETC);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, ETCBVO.TS_ETC_B);
			childVO.setAttributeValue(VOTableVO.VOTABLE, ETCBVO.TS_ETC_B);

			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public String getCodeFieldCode() {
		return "etc_code";
	}




}
