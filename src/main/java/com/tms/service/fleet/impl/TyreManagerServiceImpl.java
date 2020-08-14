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
import com.tms.service.fleet.TyreManagerService;
import com.tms.vo.fleet.TyreManagerVO;

@Service
public class TyreManagerServiceImpl extends TMSAbsBillServiceImpl implements TyreManagerService {

	public String getBillType() {
		return BillTypeConst.TYRE;
	}
	
	protected Integer getConfirmStatus() {
		return BillStatus.TYRE_CONFIRM;
	}
	
	private AggregatedValueObject billInfo;
	
	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, TyreManagerVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, TyreManagerVO.PK_TYRE_MANAGER);
			vo.setAttributeValue(VOTableVO.ITEMCODE, "ts_tyre_manager");
			vo.setAttributeValue(VOTableVO.VOTABLE, "ts_tyre_manager");
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
