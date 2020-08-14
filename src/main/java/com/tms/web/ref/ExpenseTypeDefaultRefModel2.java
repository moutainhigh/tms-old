package com.tms.web.ref;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.constants.ContractConst;

/**
 * 费用类型参照，默认值是“应收、应收应付”,创建一个ExpenseTypeDefaultRefModel2，默认值是"应付、应收应付"
 * 
 * @author xuqc
 * @date 2012-7-2 下午06:03:15
 */
@Controller
@RequestMapping(value = "/ref/common/et2")
public class ExpenseTypeDefaultRefModel2 extends ExpenseTypeDefaultRefModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6333613384391521488L;

	public String getExtendCond(HttpServletRequest request) {
		String where = request.getParameter("_cond");
		if(StringUtils.isBlank(where)) {
			where = " expense_type in (1,2) ";
		}
		String contract_type = request.getParameter("type");
		if(StringUtils.isNotBlank(contract_type)) {
			if(ContractConst.CARRIER == Integer.parseInt(contract_type)) {
				// 承运商
				where = "expense_type in (1,2)";
			} else {
				where = "expense_type in (0,2)";
			}
		}
		return where;
	}

}
