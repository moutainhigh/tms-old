package com.tms.web.kpi;

import org.nw.service.IToftService;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.kpi.KPIIndexService;

@Controller
@RequestMapping(value = "/kpi/kpiIndex")
public class KPIIndexController extends AbsToftController {
	
	@Autowired
	private KPIIndexService  kPIIndexService;

	@Override
	public IToftService getService() {
		return kPIIndexService;
	}

}
