package com.tms.web.fleet;

import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.fleet.DriverTrainService;

@Controller
@RequestMapping("/fleet/dt")
public class DriverTrainController extends AbsBillController {
	
	@Autowired
	private DriverTrainService driverTrainService;
	@Override
	public DriverTrainService getService() {
		return driverTrainService;
	}

}
