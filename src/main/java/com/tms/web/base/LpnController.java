package com.tms.web.base;

import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.base.impl.LpnService;

/**
 * lpn
 * 
 * @author xuqc
 * @date 2013-12-29 上午11:21:37
 */
@Controller
@RequestMapping(value = "/base/lpn")
public class LpnController extends AbsToftController {

	@Autowired
	private LpnService lpnService;

	public LpnService getService() {
		return lpnService;
	}

}
