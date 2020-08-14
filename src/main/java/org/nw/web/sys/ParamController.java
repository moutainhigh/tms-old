package org.nw.web.sys;

import org.nw.service.IToftService;
import org.nw.service.sys.ParamService;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 系统参数
 * 
 * @author xuqc
 * @date 2012-6-10 下午03:38:27
 */
@Controller
@RequestMapping(value = "/param")
public class ParamController extends AbsToftController {

	@Autowired
	private ParamService paramService;

	public IToftService getService() {
		return paramService;
	}

}
