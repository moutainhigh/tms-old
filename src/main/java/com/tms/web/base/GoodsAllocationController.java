package com.tms.web.base;

import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.base.impl.GoodsAllocationService;

/**
 * 货位
 * 
 * @author xuqc
 * @date 2013-12-29 上午11:20:27
 */
@Controller
@RequestMapping(value = "/base/ga")
public class GoodsAllocationController extends AbsToftController {

	@Autowired
	private GoodsAllocationService goodsAllocationService;

	public GoodsAllocationService getService() {
		return goodsAllocationService;
	}

}
