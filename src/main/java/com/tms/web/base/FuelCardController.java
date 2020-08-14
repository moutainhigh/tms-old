package com.tms.web.base;

import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.base.FuelCardService;

//yaojiie 2015 12 16 加油卡管理
@Controller
@RequestMapping(value = "/base/fuelCard")
public class FuelCardController extends AbsToftController {

	@Autowired
	private FuelCardService fuelCardService;

	public FuelCardService getService() {
		return fuelCardService;
	}

}
