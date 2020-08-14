package com.tms.web.base;

import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.base.TransTypeService;

/**
 * 运输方式
 * 
 * @author xuqc
 * @date 2012-7-22 下午10:51:56
 */
@Controller
@RequestMapping(value = "/base/transtype")
public class TransTypeController extends AbsToftController {

	@Autowired
	private TransTypeService transTypeService;

	public TransTypeService getService() {
		return transTypeService;
	}
}
