package com.tms.web.report;

import org.nw.web.AbsReportController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.report.EntrustReportServiceImpl;

/**
 * 委托单报表
 * 
 * @author xuqc
 * @date 2013-7-29 下午01:32:32
 */
@Controller
@RequestMapping(value = "/report/ent")
public class EntrustReportController extends AbsReportController {

	@Autowired
	private EntrustReportServiceImpl entrustReportServiceImpl;

	public EntrustReportServiceImpl getService() {
		return entrustReportServiceImpl;
	}

}
