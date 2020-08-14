package com.tms.web.base;

import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.base.GoodsTypeService;

/**
 * 货品类型
 * 
 * @author xuqc
 * @date 2012-7-22 下午02:24:59
 */
@Controller
@RequestMapping(value = "/base/goodstype")
public class GoodsTypeController extends AbsToftController {

	@Autowired
	private GoodsTypeService goodsTypeService;

	public GoodsTypeService getService() {
		return goodsTypeService;
	}

}
