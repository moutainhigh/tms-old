package com.tms.web.base;

import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.base.OpProjectService;


/**
 * 用户操作类
 * 
 * @author xuqc
 * @date 2012-6-10 下午03:38:27
 */
@Controller
@RequestMapping(value = "/base/opp")
public class OpProjectController extends AbsToftController {

	@Autowired
	private OpProjectService opProjectService;

	public OpProjectService getService() {
		return opProjectService;
	}
}
