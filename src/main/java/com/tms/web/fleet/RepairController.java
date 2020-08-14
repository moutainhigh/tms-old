package com.tms.web.fleet;

import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.fleet.RepairService;

//yaojiie 2015 12 16 维修费用管理controller
@Controller
@RequestMapping("/fleet/repair")
public class RepairController extends AbsBillController {

	@Autowired
	private RepairService repairService;
	@Override
	public RepairService getService() {
		return repairService;
	}
	

}
