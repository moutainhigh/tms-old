package com.tms.web.fleet;

import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.fleet.TollService;

//yaojiie 2015 12 16 路桥费管理
@Controller
@RequestMapping(value = "/fleet/toll")
public class TollController extends AbsBillController {

	@Autowired
	private TollService tollService;

	public TollService getService() {
		return tollService;
	}

}
