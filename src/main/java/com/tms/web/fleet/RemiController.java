package com.tms.web.fleet;

import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.fleet.RemiService;

//yaojiie 2015 12 16 报账管理controller
@Controller
@RequestMapping("/fleet/remi")
public class RemiController extends AbsBillController {

	@Autowired
	private RemiService remiService;
	@Override
	public RemiService getService() {
		return remiService;
	}
	

}
