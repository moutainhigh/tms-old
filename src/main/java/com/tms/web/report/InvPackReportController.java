package com.tms.web.report;

import org.nw.web.AbsReportController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.report.InvPackReportServiceImpl;

/**
 * 货品明细表,改节点不需要分配给用户，但是用户可以通过每日财务报表钻取到
 * 
 * @author xuqc
 * @date 2015-1-12 下午02:55:12
 */
@Controller
@RequestMapping(value = "/common/report/invPack")
public class InvPackReportController extends AbsReportController {

	@Autowired
	private InvPackReportServiceImpl invPackReportServiceImpl;

	public InvPackReportServiceImpl getService() {
		return invPackReportServiceImpl;
	}
}
