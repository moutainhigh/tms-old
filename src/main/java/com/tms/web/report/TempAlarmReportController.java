package com.tms.web.report;

import org.nw.web.AbsReportController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.report.TempAlarmServiceImpl;

/**
 * 温度报警报表
 * @author XIA
 */
@Controller
@RequestMapping(value = "/report/ta")
public class TempAlarmReportController extends AbsReportController {

	@Autowired
	private TempAlarmServiceImpl tempAlarmServiceImpl;

	public TempAlarmServiceImpl getService() {
		return tempAlarmServiceImpl;
	}

}
