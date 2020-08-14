package com.tms.web.fleet;

import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.fleet.RefuelService;

//yaojiie 2015 12 16 加油管理controller
@Controller
@RequestMapping("/fleet/refuel")
public class RefuelController extends AbsBillController {
	
	@Autowired
	private RefuelService refuelService;
	@Override
	public RefuelService getService() {
		return refuelService;
	}

}
