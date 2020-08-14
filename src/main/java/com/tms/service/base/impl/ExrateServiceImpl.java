package com.tms.service.base.impl;

import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.VOStatus;
import org.springframework.stereotype.Service;

import com.tms.service.base.ExrateService;
import com.tms.vo.base.ExrateBVO;
import com.tms.vo.base.ExrateVO;

/**
 * 汇率
 * @author muyun
 *
 */
@Service
public class ExrateServiceImpl extends AbsToftServiceImpl implements ExrateService {
	private AggregatedValueObject billInfo;

	/**
	 * 单表结构
	 */
	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, ExrateVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, ExrateVO.PK_EXTRATE);
			billInfo.setParentVO(vo);
			
			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, ExrateBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, ExrateBVO.PK_EXTRATE);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, ExrateBVO.TS_EXTRATE_B);
			childVO.setAttributeValue(VOTableVO.VOTABLE, ExrateBVO.TS_EXTRATE_B);

			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
			
		}
		return billInfo;
	}

	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeSave(billVO, paramVO);
		ExrateVO parentVO = (ExrateVO) billVO.getParentVO();
		if(parentVO.getStatus() != VOStatus.NEW){
			return;
		}
		String exrate_year = parentVO.getRate_year();
		ExrateBVO[] childrenVO = new ExrateBVO[12];
		for(int i=1; i<=12; i++){
			ExrateBVO childVO = new ExrateBVO();
			childVO.setStatus(VOStatus.NEW);
			String exrate_month = "";
			if(i<10){
				exrate_month = exrate_year + "-0" + i;
			}else{
				exrate_month = exrate_year + "-" + i;
			}
			childVO.setExrate_month(exrate_month);
			childrenVO[i-1] = childVO;
		}
		billVO.setChildrenVO(childrenVO);
	}


}
