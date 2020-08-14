package com.tms.web.fleet;

import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.fleet.InsurService;

//yaojiie 2015 12 16 保险管理
@Controller
@RequestMapping(value = "/fleet/insur")
public class InsurController extends AbsBillController {

	@Autowired
	private InsurService insurService;

	public InsurService getService() {
		return insurService;
	}

}
