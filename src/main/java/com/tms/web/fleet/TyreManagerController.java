package com.tms.web.fleet;

import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.fleet.TyreManagerService;

@Controller
@RequestMapping("/fleet/tm")
public class TyreManagerController extends AbsBillController {
	
	@Autowired
	private TyreManagerService tyreManagerService;
	@Override
	public TyreManagerService getService() {
		return tyreManagerService;
	}

}
