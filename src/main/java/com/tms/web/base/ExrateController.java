package com.tms.web.base;

import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.base.ExrateService;

@Controller
@RequestMapping(value = "/base/exr")
public class ExrateController extends AbsToftController {

	@Autowired
	private ExrateService exrateService;

	public ExrateService getService() {
		return exrateService;
	}

}
