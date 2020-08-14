package com.tms.web.report;

import org.nw.web.AbsDynReportController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.report.DailyFinancialReportServiceImpl;

/**
 * 每日财务报表
 * 
 * @author xuqc
 * @date 2015-1-12 下午02:55:12
 */
@Controller
@RequestMapping(value = "/report/df")
public class DailyFinancialReportController extends AbsDynReportController {

	@Autowired
	private DailyFinancialReportServiceImpl dailyFinancialReportServiceImpl;

	public DailyFinancialReportServiceImpl getService() {
		return dailyFinancialReportServiceImpl;
	}

}
