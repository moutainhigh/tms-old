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

/**
 * 欧瑞莲 测试报表
 * @author XIA
 *
 */
@Service
public class OriflameReportServiceImpl extends TMSAbsReportServiceImpl {
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
	
	
	
	
	
}
