package com.tms.web.base;

import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.base.DriverService;

/**
 * 司机
 * 
 * @author xuqc
 * @date 2012-7-22 下午02:24:59
 */
@Controller
@RequestMapping(value = "/base/driver")
public class DriverController extends AbsToftController {

	@Autowired
	private DriverService driverService;

	public DriverService getService() {
		return driverService;
	}

	protected String getUploadField() {
		return "photo";
	}

}
