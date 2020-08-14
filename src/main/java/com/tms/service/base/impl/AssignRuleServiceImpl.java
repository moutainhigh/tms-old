package com.tms.service.base.impl;

import java.util.List;

import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.springframework.stereotype.Service;

import com.tms.service.base.AssignRuleService;
import com.tms.vo.base.AssignRuleBVO;
import com.tms.vo.base.AssignRuleVO;
import com.tms.vo.base.ExAggAssignRuleVO;
import com.tms.vo.base.ExAggFuelCardVO;

@Service
public class AssignRuleServiceImpl extends AbsToftServiceImpl implements AssignRuleService {
	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggFuelCardVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggAssignRuleVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, AssignRuleVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, AssignRuleVO.PK_ASSIGN_RULE);
			billInfo.setParentVO(vo);

			VOTableVO childVO1 = new VOTableVO();
			childVO1.setAttributeValue(VOTableVO.BILLVO, ExAggAssignRuleVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.HEADITEMVO, AssignRuleBVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.PKFIELD, AssignRuleBVO.PK_ASSIGN_RULE);
			childVO1.setAttributeValue(VOTableVO.ITEMCODE, "ts_assign_rule_b");
			childVO1.setAttributeValue(VOTableVO.VOTABLE, "ts_assign_rule_b");

			CircularlyAccessibleValueObject[] childrenVO = { childVO1 };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public String getCodeFieldCode() {
		return AssignRuleVO.CODE;
	}
	
	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[1]) {
				if(fieldVO.getItemkey().equals("start_addr_type")) {
					// 第一个参数类型
					fieldVO.setUserdefine1("afterEditStart_addr_type(record)");
				} else if(fieldVO.getItemkey().equals("end_addr_type")) {
					// 第二个参数类型
					fieldVO.setUserdefine1("afterEditEnd_addr_type(record)");
				}
			}
		}
		return templetVO;
	}

}
