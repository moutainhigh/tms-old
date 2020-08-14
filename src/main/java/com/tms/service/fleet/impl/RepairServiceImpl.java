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
import com.tms.service.fleet.RepairService;
import com.tms.vo.fleet.ExAggRepairVO;
import com.tms.vo.fleet.RepairBVO;
import com.tms.vo.fleet.RepairVO;

//yaojiie 2015 12 16 路桥费管理
@Service
public class RepairServiceImpl extends TMSAbsBillServiceImpl implements RepairService {

	public String getBillType() {
		return BillTypeConst.REP;
	}

	protected Integer getConfirmStatus() {
		return BillStatus.REP_CONFIRM;
	}
	private AggregatedValueObject billInfo;
	
	public AggregatedValueObject getBillInfo() {
		if (billInfo == null) {
			billInfo = new ExAggRepairVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggRepairVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, RepairVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, RepairVO.PK_REPAIR);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, ExAggRepairVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, RepairBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, RepairBVO.PK_REPAIR);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_repair_b");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_repair_b");

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
