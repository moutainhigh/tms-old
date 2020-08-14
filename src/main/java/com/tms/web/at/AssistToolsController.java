package com.tms.web.at;

import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.at.AssistToolsService;

/**
 * 辅助工具操作
 * 
 * @author xuqc
 * @date 2013-6-9 上午09:42:20
 */
@Controller
@RequestMapping(value = "/at/at")
public class AssistToolsController extends AbsToftController {

	@Autowired
	private AssistToolsService assistToolsService;

	public AssistToolsService getService() {
		return assistToolsService;
	}

}
