package com.tms.web.base;

import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.base.ETCService;

@Controller
@RequestMapping(value = "/base/etc")
public class ETCController extends AbsToftController {

	@Autowired
	private ETCService eTCService;

	public ETCService getService() {
		return eTCService;
	}

}
