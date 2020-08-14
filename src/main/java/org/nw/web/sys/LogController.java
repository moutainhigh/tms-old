package org.nw.web.sys;

import org.nw.service.IToftService;
import org.nw.service.sys.LogService;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * 系统日志
 * 
 * @author xuqc
 * @date 2012-6-10 下午03:38:27
 */
@Controller
@RequestMapping(value = "/log")
public class LogController extends AbsToftController {

	@Autowired
	private LogService logService;

	public IToftService getService() {
		// 这里事务使用spring自动管理，service必须从springbean管理器中取得
		return logService;
	}

}
