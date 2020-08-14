package com.tms.web.fleet;

import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.fleet.AnnualService;

@Controller
@RequestMapping("/fleet/ann")
public class AnnualController extends AbsBillController {
	
	@Autowired
	private AnnualService annualService;
	@Override
	public AnnualService getService() {
		return annualService;
	}

}
