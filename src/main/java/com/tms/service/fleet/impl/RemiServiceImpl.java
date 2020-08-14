package com.tms.service.fleet.impl;

import java.util.List;

import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.springframework.stereotype.Service;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.fleet.RemiService;
import com.tms.vo.fleet.ExAggMaintainVO;
import com.tms.vo.fleet.ExAggRemiVO;
import com.tms.vo.fleet.MaintainBVO;
import com.tms.vo.fleet.MaintainVO;
import com.tms.vo.fleet.RemiBVO;
import com.tms.vo.fleet.RemiVO;

@Service
public class RemiServiceImpl extends TMSAbsBillServiceImpl implements RemiService {

	public String getBillType() {
		return BillTypeConst.REM;
	}
	
	protected Integer getConfirmStatus() {
		return BillStatus.REM_CONFIRM;
	}
	
	private AggregatedValueObject billInfo;
	
	public AggregatedValueObject getBillInfo() {
		if (billInfo == null) {
			billInfo = new ExAggRemiVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggRemiVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, RemiVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, RemiVO.PK_REMI);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, ExAggRemiVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, RemiBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, RemiBVO.PK_REMI);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_remi_b");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_remi_b");

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
