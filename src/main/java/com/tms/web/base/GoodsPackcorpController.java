package com.tms.web.base;

import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.base.GoodsPackcorpService;

/**
 * 货品包装单位类型
 * 
 * @author xuqc
 * @date 2012-7-22 下午02:24:59
 */
@Controller
@RequestMapping(value = "/base/packcorp")
public class GoodsPackcorpController extends AbsToftController {

	@Autowired
	private GoodsPackcorpService goodsPackcorpService;

	public GoodsPackcorpService getService() {
		return goodsPackcorpService;
	}

}
