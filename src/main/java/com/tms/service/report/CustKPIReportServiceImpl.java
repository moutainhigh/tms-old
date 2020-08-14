package com.tms.service.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Session;

import org.nw.jf.vo.UiReportTempletVO;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.ReportVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.sys.ReportTempletBVO;
import org.springframework.stereotype.Service;

import com.tms.service.TMSAbsReportServiceImpl;
import com.tms.vo.te.ExpAccidentVO;

/*KPI客户报表总览*/
@Service
public class CustKPIReportServiceImpl extends TMSAbsReportServiceImpl {
	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO parentVO = new VOTableVO();
			parentVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			parentVO.setAttributeValue(VOTableVO.HEADITEMVO, ExpAccidentVO.class.getName());
			parentVO.setAttributeValue(VOTableVO.PKFIELD, "pk_exp_accident");
			parentVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_exp_accident");
			parentVO.setAttributeValue(VOTableVO.VOTABLE, "ts_exp_accident");
			billInfo.setParentVO(parentVO);
		}
		return billInfo;
	}
	
	public UiReportTempletVO getReportTempletVO(String templateID) {
		UiReportTempletVO templetVO = super.getReportTempletVO(templateID);
		List<ReportTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(ReportTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getItemkey().equals("pk_carrier")) {
				// 承运商信息
				fieldVO.setRenderer("carrierRenderer");
				break;
			}
		}
		return templetVO;
	}

	public boolean addCorrelateQuery(UiReportTempletVO templetVO, ParamVO paramVO) {
		return false;
	}
	
	@SuppressWarnings("unchecked")
	protected void processReportAfterExecFormula(ReportVO reportVO, ParamVO paramVO){
		super.processReportAfterExecFormula(reportVO, paramVO);
		List<Map<String, Object>> list = reportVO.getPageVO().getItems();
		if(list == null || list.size() == 0) {
			return;
		}
		list.clear();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("pk_carrier", "南粤物流");
		map.put("req_deli_date", "2016-03-07 14:16:43");
		map.put("req_arri_date", "2016-03-08 14:16:43");
		map.put("deli", "0.917");
		map.put("arri", "0.893");
		map.put("qswz", "0.698");
		map.put("hdwz", "0.223");
		map.put("hwps", "0.618");
		map.put("hwds", "0.558");
		map.put("hdjs", "0.912");
		map.put("khts", "0.236");
		list.add(map);
	}
	
}
