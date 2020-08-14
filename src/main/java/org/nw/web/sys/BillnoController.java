package org.nw.web.sys;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.service.IToftService;
import org.nw.service.sys.BillnoService;
import org.nw.service.sys.FunService;
import org.nw.vo.TreeVO;
import org.nw.vo.sys.BillnoRuleVO;
import org.nw.web.AbsToftController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * 单据号规则、单据号
 * 
 * @author xuqc
 * @date 2012-6-10 下午03:38:27
 */
@Controller
@RequestMapping(value = "/billno")
public class BillnoController extends AbsToftController {

	@Autowired
	private BillnoService billnoService;

	@Autowired
	private FunService funService;

	public IToftService getService() {
		// 这里事务使用spring自动管理，service必须从springbean管理器中取得
		return billnoService;
	}

	public String getTreePkField() {
		return "bill_type";
	}

	/**
	 * 返回单据类型树，读取nw_fun中的所有单据类型
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getBillTypeTree.json")
	@ResponseBody
	public List<TreeVO> getBillTypeTree(HttpServletRequest request, HttpServletResponse response) {
		return funService.getBillTypeTree();
	}

	@RequestMapping(value = "/checkBillnoRule.json")
	@ResponseBody
	public Map<String, Object> checkBillnoRule(HttpServletRequest request, HttpServletResponse response) {
		String bill_type = request.getParameter("bill_type");
		if(StringUtils.isBlank(bill_type)) {
			throw new RuntimeException("请先选择单据类型！");
		}
		BillnoRuleVO ruleVO = billnoService.getByBillType(bill_type);
		if(ruleVO != null) {
			if(ruleVO.getPk_corp().equals(WebUtils.getLoginInfo().getPk_corp())) {
				// throw new RuntimeException("单据规则已经存在，不能新增，只能修改！");
				return this.genAjaxResponse(true, null, 'N');
			}
		}
		return genAjaxResponse(true, null, 'Y');
	}
}
