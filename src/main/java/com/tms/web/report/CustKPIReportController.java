package com.tms.web.report;

import org.nw.web.AbsReportController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.report.CustKPIReportServiceImpl;

@Controller
@RequestMapping(value = "/report/custKpi")
public class CustKPIReportController extends AbsReportController {

	@Autowired
	private CustKPIReportServiceImpl custKpiReportServiceImpl;

	public CustKPIReportServiceImpl getService() {
		return custKpiReportServiceImpl;
	}
}
