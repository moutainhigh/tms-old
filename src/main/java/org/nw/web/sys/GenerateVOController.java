package org.nw.web.sys;

import org.nw.service.IToftService;
import org.nw.web.AbsToftController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * 根据数据库生成VO
 * 
 * @author xuqc
 * @date 2013-7-27 下午04:12:56
 */
@Controller
@RequestMapping(value = "/gvo")
public class GenerateVOController extends AbsToftController {

	public IToftService getService() {
		return null;
	}

}
