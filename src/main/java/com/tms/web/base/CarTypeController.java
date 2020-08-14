package com.tms.web.base;

import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.base.CarTypeService;

/**
 * 车辆类型
 * 
 * @author xuqc
 * @date 2012-7-22 下午02:24:59
 */
@Controller
@RequestMapping(value = "/base/cartype")
public class CarTypeController extends AbsToftController {

	@Autowired
	private CarTypeService carTypeService;

	public CarTypeService getService() {
		return carTypeService;
	}

}
