package com.tms.service.report;

import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.springframework.stereotype.Service;

import com.tms.service.TMSAbsReportServiceImpl;
import com.tms.vo.base.ExAggCustVO;
import com.tms.vo.cm.ExAggPayDetailVO;
import com.tms.vo.cm.PayDetailVO;

/**
 * 应付明细报表的service,不需要定义接口了，当然也可以定义
 * 
 * @author xuqc
 * @date 2013-7-29 下午05:20:09
 */
@Service
public class PayDetailReportServiceImpl extends TMSAbsReportServiceImpl {
	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggCustVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggPayDetailVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, PayDetailVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, PayDetailVO.PK_PAY_DETAIL);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}
}
