package com.tms.web.report;

import org.nw.web.AbsReportController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.report.EmpKPIReportServiceImpl;

@Controller
@RequestMapping(value = "/report/empKpi")
public class EmpKPIReportController extends AbsReportController {

	@Autowired
	private EmpKPIReportServiceImpl empKpiReportServiceImpl;

	public EmpKPIReportServiceImpl getService() {
		return empKpiReportServiceImpl;
	}
}
