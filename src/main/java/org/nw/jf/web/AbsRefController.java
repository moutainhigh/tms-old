package org.nw.jf.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.nw.web.AbsBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * 参照域基础controller
 * 
 * @author xuqc
 * @date 2011-1-19
 */
@Controller
public abstract class AbsRefController extends AbsBaseController {

	/**
	 * 是否加入封存数据
	 * 
	 * @param request
	 * @return
	 */
	public boolean getSealflag(HttpServletRequest request) {
		String sealflag = request.getParameter("sealflag");
		return "true".equals(sealflag);
	}

	/**
	 * 参照通常需要设置额外的查询条件，该查询条件必须是正确的sql查询条件
	 * 修改名称，从getWhereClause-->getExtendCond.因为whereClause会让人误解已包含where关键字
	 * 
	 * @param request
	 * @return
	 */
	public String getExtendCond(HttpServletRequest request) {
		// 在返回之前执行sql安全校验 TODO
		return request.getParameter("_cond");
	}

	/**
	 * 当鼠标离开参照域，根据code返回pk值，并设置到pk域中 必须在子类重写
	 * 
	 * @param code
	 * @return
	 */
	@RequestMapping(value = "/getByCode.do")
	@ResponseBody
	public abstract Map<String, Object> getByCode(String code);

	/**
	 * 当鼠标聚焦参照域时，根据pk域的pk值返回code值，并设置到参照域中 必须在子类重写
	 * 
	 * @param pk
	 * @return
	 */
	@RequestMapping(value = "/getByPk.do")
	@ResponseBody
	public abstract Map<String, Object> getByPk(String pk);

}
