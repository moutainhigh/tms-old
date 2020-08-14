package com.tms.web.base;

import org.nw.web.AbsToftController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.base.AssignRuleService;

@Controller
@RequestMapping(value = "/base/ar")
public class AssignRuleController extends AbsToftController {

	@Autowired
	private AssignRuleService assignRuleService;

	public AssignRuleService getService() {
		return assignRuleService;
	}

}
