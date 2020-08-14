package com.tms.web.fleet;

import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.fleet.ViolationService;

@Controller
@RequestMapping("/fleet/vio")
public class ViolationController extends AbsBillController {
	
	@Autowired
	private ViolationService violationService;
	@Override
	public ViolationService getService() {
		return violationService;
	}

}
