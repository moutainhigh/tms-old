package org.nw.web.sys;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.service.sys.CodeRuleService;
import org.nw.vo.TreeVO;
import org.nw.vo.sys.CodeRuleVO;
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
@RequestMapping(value = "/coderule")
public class CodeRuleController extends AbsToftController {

	@Autowired
	private CodeRuleService codeRuleService;

	public CodeRuleService getService() {
		return codeRuleService;
	}

	public String getTreePkField() {
		return "fun_code";
	}

	/**
	 * 返回单据类型树，读取nw_fun中的所有单据类型
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getFunCodeTree.json")
	@ResponseBody
	public List<TreeVO> getFunCodeTree(HttpServletRequest request, HttpServletResponse response) {
		return codeRuleService.getFunCodeTree();
	}

	@RequestMapping(value = "/checkCodeRule.json")
	@ResponseBody
	public Map<String, Object> checkBillnoRule(HttpServletRequest request, HttpServletResponse response) {
		String fun_code = request.getParameter("fun_code");
		if(StringUtils.isBlank(fun_code)) {
			throw new RuntimeException("请先选择节点！");
		}
		CodeRuleVO ruleVO = codeRuleService.getByFunCode(fun_code);
		if(ruleVO != null) {
			if(ruleVO.getPk_corp().equals(WebUtils.getLoginInfo().getPk_corp())) {
				// throw new RuntimeException("单据规则已经存在，不能新增，只能修改！");
				return this.genAjaxResponse(true, null, 'N');
			}
		}
		return genAjaxResponse(true, null, 'Y');
	}
}
