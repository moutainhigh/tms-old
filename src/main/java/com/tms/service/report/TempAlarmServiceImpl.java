package com.tms.service.report;


import java.util.List;

import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiReportTempletVO;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.sys.ReportTempletBVO;
import org.springframework.stereotype.Service;

import com.tms.service.TMSAbsReportServiceImpl;
import com.tms.vo.te.EntLotTrackingHisBVO;

/**
 * 温度报警报表
 * @author XIA
 */
@Service
public class TempAlarmServiceImpl extends TMSAbsReportServiceImpl {
	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, EntLotTrackingHisBVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, "pk_ent_lot_track_his_b");
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}
	
	
	public UiReportTempletVO getReportTempletVO(String templateID) {
		UiReportTempletVO templetVO = super.getReportTempletVO(templateID);
		List<ReportTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(ReportTempletBVO fieldVO : fieldVOs) {
			if (fieldVO.getItemkey().equals("speed_status")) {
				fieldVO.setBeforeRenderer("speed_statusBeforeRenderer");
			}else if (fieldVO.getItemkey().equals("temp_status")) {
				fieldVO.setBeforeRenderer("temp_statusBeforeRenderer");
			}
		}
		return templetVO;
	}
	
}
