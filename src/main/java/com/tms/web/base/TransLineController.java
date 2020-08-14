package com.tms.web.base;

import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.base.TransLineService;

/**
 * 运输线路
 * 
 * @author xuqc
 * @date 2012-7-22 下午10:57:25
 */
@Controller
@RequestMapping(value = "/base/transline")
public class TransLineController extends AbsToftController {

	@Autowired
	private TransLineService transLineService;

	public TransLineService getService() {
		return transLineService;
	}

}
