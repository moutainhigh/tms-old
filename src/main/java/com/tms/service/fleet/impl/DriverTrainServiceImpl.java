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
import com.tms.service.fleet.DriverTrainService;
import com.tms.vo.fleet.DriverTrainVO;

@Service
public class DriverTrainServiceImpl extends TMSAbsBillServiceImpl implements DriverTrainService {

	public String getBillType() {
		return BillTypeConst.SJPX;
	}
	
	protected Integer getConfirmStatus() {
		return BillStatus.SJPX_CONFIRM;
	}
	private AggregatedValueObject billInfo;
	
	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, DriverTrainVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, DriverTrainVO.PK_DRIVER_TRAIN);
			vo.setAttributeValue(VOTableVO.ITEMCODE, "ts_driver_train");
			vo.setAttributeValue(VOTableVO.VOTABLE, "ts_driver_train");
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
