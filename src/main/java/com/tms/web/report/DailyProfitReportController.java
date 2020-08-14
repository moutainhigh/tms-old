package com.tms.web.report;

import org.nw.web.AbsDynReportController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.report.DailyProfitReportServiceImpl;

//yaojiie 2015 11 25 每日利润分析报表
@Controller
@RequestMapping(value = "/report/dp")
public class DailyProfitReportController extends AbsDynReportController {

	@Autowired
	private DailyProfitReportServiceImpl dailyProfitReportServiceImpl;
	
	public DailyProfitReportServiceImpl getService(){
		return dailyProfitReportServiceImpl;
	}
	
	
}