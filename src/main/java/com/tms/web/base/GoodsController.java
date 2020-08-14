package com.tms.web.base;

import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.base.GoodsService;

/**
 * 货品类型
 * 
 * @author xuqc
 * @date 2012-7-22 下午02:24:59
 */
@Controller
@RequestMapping(value = "/base/goods")
public class GoodsController extends AbsToftController {

	@Autowired
	private GoodsService goodsService;

	public GoodsService getService() {
		return goodsService;
	}

}
