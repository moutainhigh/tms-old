package com.tms.web.wh;

import org.nw.web.AbsReportController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.wh.PickQueryService;

/**
 * 交易查询
 * 
 * @author xuqc
 * @date 2013-7-29 下午01:32:32
 */
@Controller
@RequestMapping(value = "/wh/pick_query")
public class PickQueryController extends AbsReportController {

	public PickQueryService getService() {
		return new PickQueryService();
	}

}
