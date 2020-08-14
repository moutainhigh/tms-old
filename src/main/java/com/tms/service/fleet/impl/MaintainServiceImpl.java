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
import com.tms.service.fleet.MaintainService;
import com.tms.vo.fleet.ExAggMaintainVO;
import com.tms.vo.fleet.MaintainBVO;
import com.tms.vo.fleet.MaintainVO;

//yaojiie 2015 12 16 保养管理
@Service
public class MaintainServiceImpl extends TMSAbsBillServiceImpl implements MaintainService {

	public String getBillType() {
		return BillTypeConst.MAT;
	}
	
	protected Integer getConfirmStatus() {
		return BillStatus.MAT_CONFIRM;
	}
	
	private AggregatedValueObject billInfo;
	
	public AggregatedValueObject getBillInfo() {
		if (billInfo == null) {
			billInfo = new ExAggMaintainVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggMaintainVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, MaintainVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, MaintainVO.PK_MAINTAIN);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, ExAggMaintainVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, MaintainBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, MaintainBVO.PK_MAINTAIN);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_maintain_b");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_maintain_b");

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
