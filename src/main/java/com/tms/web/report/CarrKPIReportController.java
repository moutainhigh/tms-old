package com.tms.web.report;

import org.nw.web.AbsReportController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.report.CarrKPIReportServiceImpl;

@Controller
@RequestMapping(value = "/report/carrKpi")
public class CarrKPIReportController extends AbsReportController {

	@Autowired
	private CarrKPIReportServiceImpl carrKpiReportServiceImpl;

	public CarrKPIReportServiceImpl getService() {
		return carrKpiReportServiceImpl;
	}
}
