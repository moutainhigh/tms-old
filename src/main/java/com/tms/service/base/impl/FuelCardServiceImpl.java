package com.tms.service.base.impl;

import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.springframework.stereotype.Service;

import com.tms.service.base.FuelCardService;
import com.tms.vo.base.CarrRateVO;
import com.tms.vo.base.CarrierVO;
import com.tms.vo.base.ExAggCustVO;
import com.tms.vo.base.ExAggFuelCardVO;
import com.tms.vo.base.FuelCardBVO;
import com.tms.vo.base.FuelCardVO;

/**
 * 辅助工具基础信息
 * 
 * @author xuqc
 * @date 2013-6-9 上午09:44:11
 */
@Service
public class FuelCardServiceImpl extends AbsToftServiceImpl implements FuelCardService {
	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggFuelCardVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggFuelCardVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, FuelCardVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, FuelCardVO.PK_FUELCARD);
			billInfo.setParentVO(vo);

			VOTableVO childVO1 = new VOTableVO();
			childVO1.setAttributeValue(VOTableVO.BILLVO, ExAggFuelCardVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.HEADITEMVO, FuelCardBVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.PKFIELD, FuelCardBVO.PK_FUELCARD);
			childVO1.setAttributeValue(VOTableVO.ITEMCODE, "ts_fuelcard_b");
			childVO1.setAttributeValue(VOTableVO.VOTABLE, "ts_fuelcard_b");

			CircularlyAccessibleValueObject[] childrenVO = { childVO1 };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public String getCodeFieldCode() {
		return "fuelcard_code";
	}

}
