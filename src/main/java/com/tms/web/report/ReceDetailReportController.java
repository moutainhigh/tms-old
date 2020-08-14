package com.tms.web.report;

import org.nw.web.AbsReportController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.report.ReceDetailReportServiceImpl;

/**
 * 应收明细财务报表
 * 
 * @author xuqc
 * @date 2013-7-29 下午01:32:32
 */
@Controller
@RequestMapping(value = "/report/rd")
public class ReceDetailReportController extends AbsReportController {

	@Autowired
	private ReceDetailReportServiceImpl receDetailReportServiceImpl;

	public ReceDetailReportServiceImpl getService() {
		return receDetailReportServiceImpl;
	}

}
