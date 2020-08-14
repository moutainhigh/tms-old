package com.tms.web.cm;

import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import com.tms.service.cm.InspectionService;

/**
 *  协查
 * @author muyun
 *
 */
@Controller
@RequestMapping(value = "/cm/ins")
public class InspectionController extends AbsToftController {

	@Autowired
	private InspectionService inspectionService;

	public InspectionService getService() {
		return inspectionService;
	}

	
}
