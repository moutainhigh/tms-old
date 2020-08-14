package org.nw.web.sys;

import org.nw.service.sys.SystemLogService;
import org.nw.web.AbsToftController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * 
 * @author xuqc
 * @date 2012-8-8 下午02:40:08
 */
@Controller
@RequestMapping(value = "/systemlog")
public class SystemLogController extends AbsToftController {

	private SystemLogService systemLogService;

	public SystemLogService getService() {
		return systemLogService;
	}

}
