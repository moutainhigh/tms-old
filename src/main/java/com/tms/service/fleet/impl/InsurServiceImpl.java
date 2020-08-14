package com.tms.service.fleet.impl;

import java.util.List;

import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.springframework.stereotype.Service;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.fleet.InsurService;
import com.tms.vo.fleet.ExAggInsurVO;
import com.tms.vo.fleet.InsurBVO;
import com.tms.vo.fleet.InsurVO;

//yaojiie 2015 12 16 路桥费管理
@Service
public class InsurServiceImpl extends TMSAbsBillServiceImpl implements InsurService {

	public String getBillType() {
		return BillTypeConst.INS;
	}
	
	protected Integer getConfirmStatus() {
		return BillStatus.INS_CONFIRM;
	}
	
	private AggregatedValueObject billInfo;
	
	public AggregatedValueObject getBillInfo() {
		if (billInfo == null) {
			billInfo = new ExAggInsurVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggInsurVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, InsurVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, InsurVO.PK_INSUR);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, ExAggInsurVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, InsurBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, InsurBVO.PK_INSUR);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_insur_b");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_insur_b");

			CircularlyAccessibleValueObject[] childrenVO = { childVO};
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}
	
	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for (BillTempletBVO fieldVO : fieldVOs) {
			if (fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if (fieldVO.getItemkey().equals("vbillstatus")) {
					fieldVO.setBeforeRenderer("vbillstatusBeforeRenderer");
				}
			}
		}
		return templetVO;
	}

	
}
