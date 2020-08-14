package com.tms.web.report;

import org.nw.web.AbsReportController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.report.InvoiceReportServiceImpl;

/**
 * 发货单报表
 * 
 * @author xuqc
 * @date 2013-7-29 下午01:32:32
 */
@Controller
@RequestMapping(value = "/report/inv")
public class InvoiceReportController extends AbsReportController {

	@Autowired
	private InvoiceReportServiceImpl invoiceReportServiceImpl;

	public InvoiceReportServiceImpl getService() {
		return invoiceReportServiceImpl;
	}

}
