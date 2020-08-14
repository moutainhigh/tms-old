package org.nw.web.sys;

import org.nw.service.sys.ReportTempletService;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * 报表模板初始化
 * 
 * @author xuqc
 * @date 2013-7-28 下午11:51:47
 */
@Controller
@RequestMapping(value = "/rt")
public class ReportTempletController extends AbsToftController {

	@Autowired
	private ReportTempletService reportTempletService;

	public ReportTempletService getService() {
		return reportTempletService;
	}

}
