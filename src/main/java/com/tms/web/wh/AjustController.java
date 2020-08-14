package com.tms.web.wh;

import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.wh.AjustService;

/**
 * 库内调整
 * 
 * @author xuqc
 * @date 2014-3-29 下午01:05:25
 */
@Controller
@RequestMapping(value = "/wh/ajust")
public class AjustController extends AbsBillController {

	@Autowired
	private AjustService ajustService;

	public AjustService getService() {
		return ajustService;
	}

}
