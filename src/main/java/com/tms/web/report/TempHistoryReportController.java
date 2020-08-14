package com.tms.web.report;

import org.nw.web.AbsReportController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.report.TempHistoryReportServiceImpl;

/**
 * 温度历史明细
 * @author XIA
 *
 */
@Controller
@RequestMapping(value = "/report/th")
public class TempHistoryReportController extends AbsReportController {

	@Autowired
	private TempHistoryReportServiceImpl tempHistoryReportServiceImpl;

	public TempHistoryReportServiceImpl getService() {
		return tempHistoryReportServiceImpl;
	}

}
