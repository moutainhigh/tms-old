package com.tms.service.report;

import java.util.List;
import java.util.Map;

import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tms.service.TMSAbsReportServiceImpl;
import com.tms.service.base.CustService;
import com.tms.vo.base.ExAggCustVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExAggEntrustVO;

/**
 * 委托单报表的service,不需要定义接口了，当然也可以定义
 * 
 * @author xuqc
 * @date 2013-7-29 下午05:20:09
 */
@Service
public class EntrustReportServiceImpl extends TMSAbsReportServiceImpl {
	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggCustVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggEntrustVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, EntrustVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, EntrustVO.PK_ENTRUST);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	@Autowired
	private CustService custService;

	/**
	 * 数据库中存储的客户名称PK使用|分隔，这里需要对数据特殊处理
	 */
	protected void processAfterExecFormula(List<Map<String, Object>> list, ParamVO paramVO, String orderBy) {
		super.processAfterExecFormula(list, paramVO, orderBy);
		for(Map<String, Object> map : list) {
			Object pk_customer = map.get("pk_customer");
			if(pk_customer != null) {
				map.put("cust_name", custService.getNameString(pk_customer.toString()));
			}
		}
	}
}
