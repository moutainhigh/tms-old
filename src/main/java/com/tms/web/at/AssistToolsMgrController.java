package com.tms.web.at;

import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.at.AssistToolsMgrService;

/**
 * 辅助工具管理操作
 * 
 * @author xuqc
 * @date 2013-6-9 上午09:42:20
 */
@Controller
@RequestMapping(value = "/at/atm")
public class AssistToolsMgrController extends AbsToftController {

	@Autowired
	private AssistToolsMgrService assistToolsMgrService;

	public AssistToolsMgrService getService() {
		return assistToolsMgrService;
	}

}
