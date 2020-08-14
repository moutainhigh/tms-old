package com.tms.web.cm;

import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.cm.ExpenseTypeService;

/**
 * 费用类型操作
 * 
 * @author xuqc
 * @date 2012-8-23 上午10:43:46
 */
@Controller
@RequestMapping(value = "/cm/et")
public class ExpenseTypeController extends AbsToftController {

	@Autowired
	private ExpenseTypeService expenseTypeService;

	public ExpenseTypeService getService() {
		return expenseTypeService;
	}

}
