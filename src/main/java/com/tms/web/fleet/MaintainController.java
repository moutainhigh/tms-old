package com.tms.web.fleet;

import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.fleet.MaintainService;

//yaojiie 2015 12 16 保养管理
@Controller
@RequestMapping(value = "/fleet/maintain")
public class MaintainController extends AbsBillController {

	@Autowired
	private MaintainService maintainService;

	public MaintainService getService() {
		return maintainService;
	}

}
