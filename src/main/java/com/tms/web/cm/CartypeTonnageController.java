package com.tms.web.cm;

import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.cm.CartypeTonnageService;

/**
 * 车型吨位换算
 * 
 * @author xuqc
 * @date 2012-8-23 上午10:43:46
 */
@Controller
@RequestMapping(value = "/cm/cartypetonnage")
public class CartypeTonnageController extends AbsToftController {

	@Autowired
	private CartypeTonnageService cartypeTonnageService;

	public CartypeTonnageService getService() {
		return cartypeTonnageService;
	}

}
