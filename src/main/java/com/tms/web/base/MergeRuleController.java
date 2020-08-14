package com.tms.web.base;

import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.base.MergeRuleService;

@Controller
@RequestMapping(value = "/base/mr")
public class MergeRuleController extends AbsToftController {

	@Autowired
	private MergeRuleService mergeRuleService;

	public MergeRuleService getService() {
		return mergeRuleService;
	}

}
