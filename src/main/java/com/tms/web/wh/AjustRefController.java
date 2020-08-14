package com.tms.web.wh;

import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.wh.AjustRefService;

/**
 * 新建调整单是参照库存生成的
 * 
 * @author xuqc
 * @date 2014-3-29 下午01:04:40
 */
@Controller
@RequestMapping(value = "/wh/ajustref")
public class AjustRefController extends AbsBillController {

	@Autowired
	private AjustRefService ajustRefService;

	public AjustRefService getService() {
		return ajustRefService;
	}

}
