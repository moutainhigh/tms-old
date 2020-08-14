package com.tms.service.fleet.impl;

import java.util.List;

import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.springframework.stereotype.Service;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.fleet.AnnualService;
import com.tms.vo.fleet.AnnualVO;

@Service
public class AnnualServiceImpl extends TMSAbsBillServiceImpl implements AnnualService {

	public String getBillType() {
		return BillTypeConst.ANN;
	}
	
	protected Integer getConfirmStatus() {
		return BillStatus.ANN_CONFIRM;
	}
	
	private AggregatedValueObject billInfo;
	
	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, AnnualVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, AnnualVO.PK_ANNUAL);
			vo.setAttributeValue(VOTableVO.ITEMCODE, "ts_annual");
			vo.setAttributeValue(VOTableVO.VOTABLE, "ts_annual");
			billInfo.setParentVO(vo);
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
