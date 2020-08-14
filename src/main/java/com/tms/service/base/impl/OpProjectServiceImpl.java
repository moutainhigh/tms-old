package com.tms.service.base.impl;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.exception.BusiException;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.springframework.stereotype.Service;

import com.tms.service.base.OpProjectService;
import com.tms.vo.base.ExrateBVO;
import com.tms.vo.base.OpProjectBVO;
import com.tms.vo.base.OpProjectVO;

/**
 * 
 * @author muyun
 *
 */
@Service
public class OpProjectServiceImpl extends AbsToftServiceImpl implements OpProjectService {
	
	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null){
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, OpProjectVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, OpProjectVO.PK_OP_PROJECT);
			billInfo.setParentVO(vo);
			
			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, OpProjectBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, OpProjectBVO.PK_OP_PROJECT);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, OpProjectBVO.PK_OP_PROJECT_B);
			childVO.setAttributeValue(VOTableVO.VOTABLE, OpProjectBVO.TS_OP_PROJECT_B);

			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		orderBy = super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
		if(StringUtils.isBlank(orderBy)) {
			orderBy = " order by code";
		}
		return orderBy;
	}
	
	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeSave(billVO, paramVO);
		OpProjectVO parentVO = (OpProjectVO) billVO.getParentVO();
		if(StringUtils.isBlank(parentVO.getPk_corp())){
			throw new BusiException("公司不能为空");
		}
		
	}

	public Map<String, Object> getHeaderDefaultValues(ParamVO paramVO) {
		Map<String, Object> values = super.getHeaderDefaultValues(paramVO);
		return values;
	}

	public String getCodeFieldCode() {
		return "code";
	}

	
}
